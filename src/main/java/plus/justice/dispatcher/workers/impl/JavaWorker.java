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

    // empty policy file
    private String policyFile;

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

        // save code
        Files.write(Paths.get(cwd + File.separator + fileName + ".java"), submission.getCode().getBytes());
        logger.info("Save code:\t" + cwd + File.separator + fileName + ".java");

        // save empty policy file
        policyFile = cwd + File.separator + "policy";
        Files.createFile(Paths.get(policyFile));

        logger.info("Create policy file:\t" + policyFile);
    }

    private TaskResult compile() throws IOException {
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

        TaskResult compile = new TaskResult();
        try {
            executor.execute(cmd);
            compile.setStatus(OK);
            logger.info("Compile OK");
        } catch (final Exception e) {
            if (watchdog.killedProcess()) {
                compile.setStatus(Submission.STATUS_CE);
                compile.setError("Compile Time Exceeded");
                logger.warn("Compile Time Exceeded:\t" + e.getMessage());
            } else {
                compile.setStatus(Submission.STATUS_CE);
                compile.setError("Compile error");
                logger.warn("Compile error:\t" + e.getMessage());
            }
            logger.warn(stderr.toString());
        }

        return compile;
    }

    private TaskResult run() throws IOException {
        TaskResult run = new TaskResult();

        CommandLine cmd = new CommandLine(java);
        cmd.addArgument("-Djava.security.manager");
        cmd.addArgument("-Djava.security.policy==" + policyFile);
        cmd.addArgument("-Xmx" + problem.getMemoryLimit() + "m");
        cmd.addArgument("-classpath");
        cmd.addArgument(cwd);
        cmd.addArgument(fileName);
        logger.info("Sandbox cmd:\t" + cmd.toString());

        long startTime = System.nanoTime();
        for (TestCase testCase : testCaseRepository.findByProblemId(submission.getProblemId())) {
            ByteArrayInputStream stdin = new ByteArrayInputStream(testCase.getInput().getBytes());
            ByteArrayOutputStream stdout = new ByteArrayOutputStream(), stderr = new ByteArrayOutputStream();
            ExecuteWatchdog watchdog = new ExecuteWatchdog(problem.getRuntimeLimit());

            DefaultExecutor executor = new DefaultExecutor();
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
                logger.warn("Runtime error:\t" + e.toString());
                return run;
            }

            String o = stdout.toString().trim();
            if (!o.equals(testCase.getOutput())) {
                run.setStatus(Submission.STATUS_WA);
                run.setInput(testCase.getInput());
                run.setOutput(o.length() > outputMaxLength ? o.substring(0, outputMaxLength) + "..." : o);
                run.setExpected(testCase.getOutput());
                return run;
            }
        }

        // TODO 如何更准确地收集 Runtime 和 Memory？
        run.setRuntime((System.nanoTime() - startTime) / 1000000);
        run.setMemory(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024));
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