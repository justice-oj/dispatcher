package plus.justice.dispatcher.workers.impl;

import org.apache.commons.exec.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.database.TestCase;
import plus.justice.dispatcher.models.sandbox.TaskResult;
import plus.justice.dispatcher.repositories.TestCaseRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class JavaWorker {
    private File tmpDir;
    private String tmpFileName;
    private final int OK = 0;
    private final TestCaseRepository testCaseRepository;

    @Autowired
    public JavaWorker(TestCaseRepository testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
    }

    public TaskResult work(Submission submission) throws IOException {
        save(submission);

        TaskResult result = compile();
        if (result.getStatus() != OK) {
            clean();
            return result;
        }

        result = run();
        clean();
        return result;
    }

    private void save(Submission submission) throws IOException {
        String tmpDirName = "/var/log/justice/code/" + submission.getId();

        tmpDir = new File(tmpDirName);
        tmpFileName = tmpDirName + "/Main.java";

        Files.createDirectories(Paths.get(tmpDirName));
        Files.write(Paths.get(tmpFileName), submission.getCode().getBytes());
    }

    private TaskResult compile() throws IOException {
        CommandLine cmd = new CommandLine("javac");
        cmd.addArgument(tmpFileName);

        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(tmpDir);
        executor.setStreamHandler(new PumpStreamHandler(null, stderr, null));
        executor.setWatchdog(new ExecuteWatchdog(10000));

        TaskResult compile = new TaskResult();
        try {
            executor.execute(cmd);
            compile.setStatus(OK);
        } catch (ExecuteException e) {
            compile.setStatus(Submission.STATUS_CE);
            compile.setError(stderr.toString());
        }
        return compile;
    }

    private TaskResult run() throws IOException {
        TaskResult run = new TaskResult();

        CommandLine cmd = new CommandLine("java");
        cmd.addArgument("Main");

        List<TestCase> testCases = testCaseRepository.findByProblemId(1L);
        for (TestCase testCase : testCases) {
            ByteArrayInputStream stdin = new ByteArrayInputStream(testCase.getInput().getBytes());
            ByteArrayOutputStream stdout = new ByteArrayOutputStream(), stderr = new ByteArrayOutputStream();
            DefaultExecutor executor = new DefaultExecutor();
            executor.setWorkingDirectory(tmpDir);
            executor.setStreamHandler(new PumpStreamHandler(stdout, stderr, stdin));
            executor.setWatchdog(new ExecuteWatchdog(10000));

            try {
                executor.execute(cmd);
            } catch (Exception e) {
                run.setStatus(Submission.STATUS_RE);
                run.setError(stderr.toString());
                return run;
            }

            if (!stdout.toString().equals(testCase.getOutput())) {
                run.setStatus(Submission.STATUS_WA);
                run.setInput(testCase.getInput());
                run.setOutput(stdout.toString());
                run.setExpected(testCase.getOutput());
                return run;
            }
        }

        run.setMemory(101);
        run.setRuntime(108);
        run.setStatus(Submission.STATUS_AC);
        return run;
    }

    private void clean() throws IOException {
        FileUtils.deleteDirectory(tmpDir);
    }
}