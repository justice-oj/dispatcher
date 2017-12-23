package plus.justice.dispatcher.workers.impl;

import lombok.Data;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import plus.justice.dispatcher.models.database.Problem;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.database.TestCase;
import plus.justice.dispatcher.workers.IWorker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Data
@Service
@PropertySource("classpath:config-${spring.profiles.active}.properties")
public class JavaWorker implements IWorker {
    @Value("${justice.judger.code.tmp.filename}")
    private String fileName;

    @Value("${justice.judger.javac.executable}")
    private String javac;

    @Value("${justice.judger.java.executable}")
    private String java;

    @Value("${justice.judger.output.maxlength}")
    private Integer outputMaxLength;

    @Value("${justice.judger.watchdog.timeout}")
    private Integer watchdogTimeout;

    // empty policy file path
    private String policyFile;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void save(String cwd, Submission submission) throws IOException {
        // save code
        Files.write(Paths.get(cwd + File.separator + fileName + ".java"), submission.getCode().getBytes());
        logger.info("Save code:\t" + cwd + File.separator + fileName + ".java");

        // save empty policy file under the same directory
        policyFile = cwd + File.separator + "policy";
        Files.createFile(Paths.get(policyFile));
        logger.info("Create policy file:\t" + policyFile);
    }

    public void compile(String cwd, Submission submission) throws RuntimeException {
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(watchdogTimeout);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(cwd));
        executor.setStreamHandler(new PumpStreamHandler(null, stderr, null));
        executor.setWatchdog(watchdog);

        CommandLine cmd = new CommandLine(javac);
        cmd.addArgument("-J-Duser.language=en");  // force using English
        cmd.addArgument("-classpath");
        cmd.addArgument(cwd);
        cmd.addArgument(fileName + ".java");
        logger.info("Compiler cmd:\t" + cmd.toString());

        try {
            executor.execute(cmd);
            logger.info("Compile OK");
        } catch (IOException e) {
            if (watchdog.killedProcess()) {
                submission.setStatus(Submission.STATUS_CE);
                submission.setError("Compile Time Exceeded");
                logger.warn("Compile Time Exceeded:\t" + e.getMessage());
            } else {
                submission.setStatus(Submission.STATUS_CE);
                submission.setError("Compile error");
                logger.warn("Compile error:\t" + e.getMessage());
            }
            logger.warn(stderr.toString());
            throw new RuntimeException("Compile Aborted.");
        }
    }

    public void run(String cwd, Problem problem, List<TestCase> testCases, Submission submission) throws RuntimeException {
        CommandLine cmd = new CommandLine(java);
        cmd.addArgument("-Djava.security.manager");
        cmd.addArgument("-Djava.security.policy==" + policyFile);
        cmd.addArgument("-Xmx" + problem.getMemoryLimit() + "m");
        cmd.addArgument("-classpath");
        cmd.addArgument(cwd);
        cmd.addArgument(fileName);
        logger.info("Sandbox cmd:\t" + cmd.toString());

        long cost = 0;
        for (TestCase testCase : testCases) {
            ByteArrayInputStream stdin = new ByteArrayInputStream(testCase.getInput().getBytes());
            ByteArrayOutputStream stdout = new ByteArrayOutputStream(), stderr = new ByteArrayOutputStream();
            ExecuteWatchdog watchdog = new ExecuteWatchdog(problem.getRuntimeLimit());

            DefaultExecutor executor = new DefaultExecutor();
            executor.setWorkingDirectory(new File(cwd));
            executor.setStreamHandler(new PumpStreamHandler(stdout, stderr, stdin));
            executor.setWatchdog(watchdog);

            long startTime = System.nanoTime();
            try {
                executor.execute(cmd);
            } catch (IOException e) {
                if (watchdog.killedProcess()) {
                    submission.setStatus(Submission.STATUS_TLE);
                    submission.setError("Time Limit Exceeded");
                } else {
                    submission.setStatus(Submission.STATUS_RE);
                    submission.setError(stderr.toString());
                }
                logger.warn("Runtime error:\t" + e.toString());
                throw new RuntimeException("Execution Aborted.");
            }
            cost += System.nanoTime() - startTime;

            String o = stdout.toString().trim();
            if (!o.equals(testCase.getOutput())) {
                submission.setStatus(Submission.STATUS_WA);
                submission.setInput(testCase.getInput());
                submission.setOutput(o.length() > outputMaxLength ? o.substring(0, outputMaxLength) + "..." : o);
                submission.setExpected(testCase.getOutput());
                logger.warn("Input: " + testCase.getInput());
                logger.warn("Output: " + o.substring(0, Math.min(outputMaxLength, o.length())));
                logger.warn("Expected: " + testCase.getOutput());
                throw new RuntimeException("Wrong Answer.");
            }
        }

        submission.setRuntime(cost / 1000000);
        submission.setMemory(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024));
        submission.setStatus(Submission.STATUS_AC);
    }
}