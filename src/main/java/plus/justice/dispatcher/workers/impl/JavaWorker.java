package plus.justice.dispatcher.workers.impl;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
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
    private int OK = 0;
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

        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(tmpDir);
        executor.setWatchdog(new ExecuteWatchdog(10000));

        TaskResult compile = new TaskResult();
        if (executor.execute(cmd) == OK) {
            compile.setStatus(Submission.STATUS_AC);
        } else {
            compile.setStatus(Submission.STATUS_CE);
            compile.setError("Compile error");
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
            executor.execute(cmd);

            if (!stdout.toString().equals(testCase.getOutput())) {
                run.setStatus(Submission.STATUS_WA);
                run.setError(testCase.getInput() + " | " + testCase.getOutput() + " | " + stdout.toString());
                return run;
            }
            System.out.println(testCase.getInput() + "... OK!");
        }

        run.setMemory(90);
        run.setRuntime(102);
        run.setStatus(Submission.STATUS_AC);
        return run;
    }

    private void clean() throws IOException {
        FileUtils.deleteDirectory(tmpDir);
    }
}