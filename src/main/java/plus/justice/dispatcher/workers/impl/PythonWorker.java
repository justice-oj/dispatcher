package plus.justice.dispatcher.workers.impl;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;

@Component
public class PythonWorker {
    @Value("${justice.judger.code.tmp.filename}")
    private String fileName;

    @Value("${justice.judger.code.tmp.basedir}")
    private String baseDir;

    @Value("${justice.judger.compiler.executable}")
    private String compiler;

    @Value("${justice.judger.gpp.executable}")
    private String gpp;

    @Value("${justice.judger.watchdog.timeout}")
    private Integer watchdogTimeout;

    // current submission
    private Submission submission;

    // current working directory
    private String cwd;

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public PythonWorker(TestCaseRepository testCaseRepository, ProblemRepository problemRepository) {
        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    public TaskResult work(Submission submission) throws IOException {
        this.submission = submission;

        save();
        TaskResult result = run();
        clean();
        return result;
    }

    private void save() throws IOException {
        cwd = baseDir + File.separator + submission.getId();
        Files.createDirectories(Paths.get(cwd));
        Files.write(Paths.get(cwd + File.separator + fileName + ".py"), submission.getCode().getBytes());
    }

    private TaskResult run() throws IOException {
        Problem problem = problemRepository.findOne(submission.getProblemId());

        TaskResult run = new TaskResult();
        CommandLine cmd = new CommandLine(cwd + File.separator + fileName);

        List<TestCase> testCases = testCaseRepository.findByProblemId(submission.getProblemId());
        long startTime = System.nanoTime();
        for (TestCase testCase : testCases) {
            ByteArrayInputStream stdin = new ByteArrayInputStream(testCase.getInput().getBytes());
            ByteArrayOutputStream stdout = new ByteArrayOutputStream(), stderr = new ByteArrayOutputStream();
            DefaultExecutor executor = new DefaultExecutor();
            ExecuteWatchdog watchdog = new ExecuteWatchdog(watchdogTimeout);

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

        // nano to milli
        run.setRuntime((System.nanoTime() - startTime) / 1000000);
        // Byte to MB
        run.setMemory(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024));
        run.setStatus(Submission.STATUS_AC);
        return run;
    }

    private void clean() throws IOException {
        //FileUtils.deleteDirectory(new File(cwd));
    }
}