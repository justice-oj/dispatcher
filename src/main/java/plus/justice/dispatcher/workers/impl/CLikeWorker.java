package plus.justice.dispatcher.workers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import plus.justice.dispatcher.models.database.Problem;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.database.TestCase;
import plus.justice.dispatcher.models.sandbox.TaskResult;
import plus.justice.dispatcher.workers.IWorker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Data
@Service
@PropertySource("classpath:config-${spring.profiles.active}.properties")
public class CLikeWorker implements IWorker {
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String realCompiler;
    private String std;
    private String suffix;

    public void save(String cwd, Submission submission) throws IOException {
        Files.write(Paths.get(cwd + File.separator + fileName + "." + suffix), submission.getCode().getBytes());
    }

    public void compile(String cwd, Submission submission) throws RuntimeException {
        CommandLine cmd = new CommandLine(compiler);
        cmd.addArgument("-basedir=" + cwd);
        cmd.addArgument("-compiler=" + realCompiler);
        cmd.addArgument("-filename=" + fileName + "." + suffix);
        cmd.addArgument("-timeout=" + watchdogTimeout);
        cmd.addArgument("-std=" + std);
        logger.info(cmd.toString());

        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(new PumpStreamHandler(null, stderr, null));
        try {
            executor.execute(cmd);
            if (stderr.toString().length() > 0) {
                submission.setStatus(Submission.STATUS_CE);
                submission.setError("Compile error");
                logger.warn(stderr.toString());
                throw new RuntimeException("Sandbox Aborted.");
            }
            logger.info("Compile OK");
        } catch (IOException e) {
            logger.warn("Compile error:\t" + e.getMessage());
            throw new RuntimeException("An Error Occurred.");
        }
    }

    public void run(String cwd, Problem problem, List<TestCase> testCases, Submission submission) throws RuntimeException {
        long runtime = 0L, memory = 0L, counter = 0L;

        for (TestCase testCase : testCases) {
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
            } catch (IOException e) {
                submission.setStatus(Submission.STATUS_RE);
                submission.setError("Runtime Error.");
                logger.info(e.getMessage());
                throw new RuntimeException("Runtime Error.");
            }

            try {
                ObjectMapper mapper = new ObjectMapper();
                TaskResult taskResult = mapper.readValue(stdout.toString(), TaskResult.class);
                logger.info(stdout.toString());

                if (taskResult.getStatus() != Submission.STATUS_AC) {
                    submission.setStatus(taskResult.getStatus());
                    submission.setError(taskResult.getError());
                    submission.setInput(taskResult.getInput());
                    submission.setOutput(taskResult.getOutput());
                    submission.setExpected(taskResult.getExpected());
                    throw new RuntimeException("Wrong Answer.");
                }

                // taskResult.getStatus() == Submission.STATUS_AC with memory limit exceeded.
                if (taskResult.getMemory() > problem.getMemoryLimit()) {
                    submission.setStatus(Submission.STATUS_MLE);
                    submission.setError("Memory Limit Exceeded");
                    throw new RuntimeException("Memory Limit Exceeded.");
                }

                runtime += taskResult.getRuntime();
                memory += taskResult.getMemory();
                counter++;
            } catch (IOException e) {
                submission.setStatus(Submission.STATUS_RE);
                submission.setError("Runtime Error.");
                logger.warn(e.getMessage());
                throw new RuntimeException("Execution Aborted.");
            }
        }

        long averageRuntime = counter == 0 ? 0 : runtime / counter, averageMemory = counter == 0 ? 0 : memory / counter;
        submission.setRuntime(averageRuntime);
        submission.setMemory(averageMemory);
        submission.setStatus(Submission.STATUS_AC);
    }
}