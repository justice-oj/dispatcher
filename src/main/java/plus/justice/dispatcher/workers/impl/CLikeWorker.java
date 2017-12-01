package plus.justice.dispatcher.workers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.database.TestCase;
import plus.justice.dispatcher.models.sandbox.TaskResult;
import plus.justice.dispatcher.repositories.ProblemRepository;
import plus.justice.dispatcher.repositories.TestCaseRepository;
import plus.justice.dispatcher.workers.WorkerAbstract;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@PropertySource("classpath:config-${spring.profiles.active}.properties")
public class CLikeWorker extends WorkerAbstract {
    @Value("${justice.judger.code.tmp.filename}")
    private String fileName;

    @Value("${justice.judger.code.tmp.basedir}")
    private String baseDir;

    @Value("${justice.judger.clike.compiler.executable}")
    private String compiler;

    @Value("${justice.judger.clike.container.executable}")
    private String container;

    @Value("${justice.judger.output.maxlength}")
    private Integer outputMaxLength;

    @Value("${justice.judger.watchdog.timeout}")
    private Integer watchdogTimeout;

    private String realCompiler;
    private String suffix;
    private String std;

    private String getRealCompiler() {
        return realCompiler;
    }

    public void setRealCompiler(String realCompiler) {
        this.realCompiler = realCompiler;
    }

    private String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    private String getStd() {
        return std;
    }

    public void setStd(String std) {
        this.std = std;
    }

    @Autowired
    public CLikeWorker(TestCaseRepository testCaseRepository, ProblemRepository problemRepository) {
        super(testCaseRepository, problemRepository);
    }

    public void save() throws IOException {
        cwd = baseDir + File.separator + submission.getId();
        Files.createDirectories(Paths.get(cwd));
        Files.write(Paths.get(cwd + File.separator + fileName + this.getSuffix()), submission.getCode().getBytes());
    }

    public TaskResult compile() throws IOException {
        CommandLine cmd = new CommandLine(compiler);
        cmd.addArgument("-basedir=" + cwd);
        cmd.addArgument("-compiler=" + this.getRealCompiler());
        cmd.addArgument("-filename=" + fileName + this.getSuffix());
        cmd.addArgument("-timeout=" + watchdogTimeout);
        cmd.addArgument("-std=" + this.getStd());
        logger.info(cmd.toString());

        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(new PumpStreamHandler(null, stderr, null));
        executor.execute(cmd);

        TaskResult compile = new TaskResult();
        if (stderr.toString().length() > 0) {
            compile.setStatus(Submission.STATUS_CE);
            compile.setError("Compile error");
            logger.warn(stderr.toString());
        } else {
            compile.setStatus(OK);
        }
        return compile;
    }

    public TaskResult run() throws IOException {
        TaskResult run = new TaskResult();
        Long runtime = 0L, memory = 0L, counter = 0L;

        for (TestCase testCase : testCaseRepository.findByProblemId(submission.getProblemId())) {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            DefaultExecutor executor = new DefaultExecutor();
            executor.setWorkingDirectory(new File(cwd));
            executor.setStreamHandler(new PumpStreamHandler(stdout, null, null));

            CommandLine cmd = new CommandLine(container);
            cmd.addArgument("-basedir=" + cwd);
            cmd.addArgument("-input=" + testCase.getInput());
            cmd.addArgument("-expected=" + testCase.getOutput());
            cmd.addArgument("-timeout=" + problem.getRuntimeLimit());
            cmd.addArgument("-memory=" + problem.getMemoryLimit() * 2);
            logger.info(cmd.toString());

            try {
                executor.execute(cmd);
            } catch (final Exception e) {
                run.setStatus(Submission.STATUS_RE);
                run.setError(e.getMessage());
                return run;
            }

            ObjectMapper mapper = new ObjectMapper();
            TaskResult taskResult = mapper.readValue(stdout.toString(), TaskResult.class);

            if (taskResult.getStatus() != Submission.STATUS_AC) {
                // stdout limit
                String o = stdout.toString().trim();
                run.setOutput(o.length() > outputMaxLength ? o.substring(0, outputMaxLength) + "..." : o);
                return taskResult;
            }

            // taskResult.getStatus() == Submission.STATUS_AC with memory limit exceeded.
            if (taskResult.getMemory() > problem.getMemoryLimit()) {
                run.setStatus(Submission.STATUS_MLE);
                run.setError("Memory Limit Exceeded");
                return taskResult;
            }

            runtime += taskResult.getRuntime();
            memory += taskResult.getMemory();
            counter++;
        }

        run.setRuntime(runtime / counter);
        run.setMemory(memory / counter);
        run.setStatus(Submission.STATUS_AC);
        return run;
    }
}