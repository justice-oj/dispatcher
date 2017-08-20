package plus.justice.dispatcher.workers.impl;

import org.apache.commons.exec.*;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@PropertySource("classpath:config-${spring.profiles.active}.properties")
public class JavaWorker {
    @Value("${justice.judger.code.tmp.filename}")
    private String fileName;

    @Value("${justice.judger.code.tmp.basedir}")
    private String baseDir;

    @Value("${justice.judger.javac.executable}")
    private String javac;

    @Value("${justice.judger.java.executable}")
    private String java;

    @Value("${justice.judger.java.policy.file}")
    private String policyFile;

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
    public JavaWorker(TestCaseRepository testCaseRepository, ProblemRepository problemRepository) {
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
        Files.write(Paths.get(cwd + File.separator + fileName + ".java"), submission.getCode().getBytes());
    }

    private TaskResult compile() throws IOException {
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(cwd));
        executor.setStreamHandler(new PumpStreamHandler(null, stderr, null));
        executor.setWatchdog(new ExecuteWatchdog(watchdogTimeout));

        CommandLine cmd = new CommandLine(javac);
        // force using English for error message
        cmd.addArgument("-J-Duser.language=en");
        cmd.addArgument(fileName + ".java");
        logger.info("compiler cmd: " + cmd.toString());

        TaskResult compile = new TaskResult();
        try {
            executor.execute(cmd);
            compile.setStatus(OK);
            logger.info("compiler OK");
        } catch (ExecuteException e) {
            compile.setStatus(Submission.STATUS_CE);
            compile.setError(stderr.toString());
            logger.warn("\ncompile error:\t" + e.getMessage());
        }

        return compile;
    }

    private TaskResult run() throws IOException {
        TaskResult run = new TaskResult();

        CommandLine cmd = new CommandLine(java);
        cmd.addArgument("-Djava.security.manager");
        cmd.addArgument("-Djava.security.policy==" + new File(policyFile).getPath());
        cmd.addArgument("-Xmx" + problem.getMemoryLimit() + "m");
        cmd.addArgument(fileName);
        logger.info("sandbox cmd: " + cmd.toString());

        long startTime = System.nanoTime();
        for (TestCase testCase : testCaseRepository.findByProblemId(submission.getProblemId())) {
            ByteArrayInputStream stdin = new ByteArrayInputStream(testCase.getInput().getBytes());
            ByteArrayOutputStream stdout = new ByteArrayOutputStream(), stderr = new ByteArrayOutputStream();
            DefaultExecutor executor = new DefaultExecutor();
            ExecuteWatchdog watchdog = new ExecuteWatchdog(problem.getRuntimeLimit());

            executor.setWorkingDirectory(new File(cwd));
            executor.setStreamHandler(new PumpStreamHandler(stdout, stderr, stdin));
            executor.setWatchdog(watchdog);

            try {
                executor.execute(cmd);
            } catch (final Exception e) {
                if (watchdog.killedProcess()) {
                    run.setStatus(Submission.STATUS_TLE);
                    run.setError("Time Limit Exceeded");
                } else {
                    run.setStatus(Submission.STATUS_RE);
                    run.setError(stderr.toString());
                }
                logger.warn("\nruntime error:\t" + e.toString());
                return run;
            }

            if (!stdout.toString().trim().equals(testCase.getOutput())) {
                run.setStatus(Submission.STATUS_WA);
                run.setInput(testCase.getInput());
                run.setOutput(stdout.toString().trim());
                run.setExpected(testCase.getOutput());
                return run;
            }
        }

        run.setRuntime((System.nanoTime() - startTime) / 1000000);
        run.setMemory(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024));
        run.setStatus(Submission.STATUS_AC);
        return run;
    }

    private void clean() throws IOException {
        FileUtils.deleteDirectory(new File(cwd));
    }
}