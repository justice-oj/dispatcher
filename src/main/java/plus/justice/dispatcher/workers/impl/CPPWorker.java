package plus.justice.dispatcher.workers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import plus.justice.dispatcher.models.database.Problem;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.database.TestCase;
import plus.justice.dispatcher.models.sandbox.TaskResult;
import plus.justice.dispatcher.repositories.ProblemRepository;
import plus.justice.dispatcher.repositories.TestCaseRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@PropertySource("classpath:config-${spring.profiles.active}.properties")
public class CPPWorker {
    @Value("${justice.judger.code.tmp.filename}")
    private String fileName;

    @Value("${justice.judger.code.tmp.basedir}")
    private String baseDir;

    @Value("${justice.judger.cppcompiler.executable}")
    private String compiler;

    @Value("${justice.judger.cppcontainer.executable}")
    private String container;

    @Value("${justice.judger.gpp.executable}")
    private String gpp;

    @Value("${justice.judger.output.maxlength}")
    private Integer outputMaxLength;

    @Value("${justice.judger.watchdog.timeout}")
    private Integer watchdogTimeout;

    // current submission
    private Submission submission;

    // related problem
    private Problem problem;

    // current working directory
    private String cwd;

    // tmp taskResult exitCode, 0 stands for OK
    private final int OK = 0;

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public CPPWorker(TestCaseRepository testCaseRepository, ProblemRepository problemRepository) {
        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    public TaskResult work(Submission submission) throws IOException {
        this.submission = submission;
        this.problem = problemRepository.findOne(submission.getProblemId());

        save();
        TaskResult result = compile();
        if (result.getStatus() != OK) {
            clean();
            return result;
        }

        result = run();
        clean();
        return result;
    }

    private void save() throws IOException {
        cwd = baseDir + File.separator + submission.getId();
        Files.createDirectories(Paths.get(cwd));
        Files.write(Paths.get(cwd + File.separator + fileName + ".cpp"), submission.getCode().getBytes());
    }

    private TaskResult compile() throws IOException {
        CommandLine cmd = new CommandLine(compiler);
        cmd.addArgument("-basedir=" + cwd);
        cmd.addArgument("-compiler=" + gpp);
        cmd.addArgument("-filename=" + fileName + ".cpp");
        cmd.addArgument("-timeout=" + watchdogTimeout);
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

    private TaskResult run() throws IOException {
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

            if (taskResult.getStatus() != Submission.STATUS_AC) return taskResult;

            runtime += taskResult.getRuntime();
            memory += taskResult.getMemory();
            counter++;
        }

        run.setRuntime(runtime / counter);
        run.setMemory(memory / counter);
        run.setStatus(Submission.STATUS_AC);
        return run;
    }

    private void clean() {
        try {
            FileUtils.deleteDirectory(new File(cwd));
        } catch (IOException e) {
            logger.warn("Remove dir:\t" + cwd + " failed");
        }
    }
}