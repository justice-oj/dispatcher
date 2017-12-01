package plus.justice.dispatcher.workers.impl;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@PropertySource("classpath:config-${spring.profiles.active}.properties")
public class JavaWorker extends WorkerAbstract {
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

    // empty policy file
    private String policyFile;

    @Autowired
    public JavaWorker(TestCaseRepository testCaseRepository, ProblemRepository problemRepository) {
        super(testCaseRepository, problemRepository);
    }

    public void save() throws IOException {
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

    public TaskResult compile() throws IOException {
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

    public TaskResult run() throws IOException {
        TaskResult run = new TaskResult();

        CommandLine cmd = new CommandLine(java);
        cmd.addArgument("-Djava.security.manager");
        cmd.addArgument("-Djava.security.policy==" + policyFile);
        cmd.addArgument("-Xmx" + problem.getMemoryLimit() + "m");
        cmd.addArgument("-classpath");
        cmd.addArgument(cwd);
        cmd.addArgument(fileName);
        logger.info("Sandbox cmd:\t" + cmd.toString());

        long cost = 0;
        for (TestCase testCase : testCaseRepository.findByProblemId(submission.getProblemId())) {
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
            cost += System.nanoTime() - startTime;

            String o = stdout.toString().trim();
            if (!o.equals(testCase.getOutput())) {
                run.setStatus(Submission.STATUS_WA);
                run.setInput(testCase.getInput());
                run.setOutput(o.length() > outputMaxLength ? o.substring(0, outputMaxLength) + "..." : o);
                run.setExpected(testCase.getOutput());
                return run;
            }
        }

        run.setRuntime(cost / 1000000);
        run.setMemory(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024));
        run.setStatus(Submission.STATUS_AC);
        return run;
    }
}