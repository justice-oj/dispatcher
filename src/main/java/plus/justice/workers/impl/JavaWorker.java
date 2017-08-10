package plus.justice.workers.impl;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import plus.justice.models.database.Submission;
import plus.justice.models.sandbox.TaskResult;
import plus.justice.workers.AbstractWorker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JavaWorker extends AbstractWorker {
    private File tmpDir;
    private String tmpFileName;

    @Override
    public void save(Submission submission) throws IOException {
        String tmpDirName = codeDir + "/" + submission.getId();

        tmpDir = new File(tmpDirName);
        tmpFileName = tmpDirName + "/Main.java";

        Files.createDirectories(Paths.get(tmpDirName));
        Files.write(Paths.get(tmpFileName), submission.getCode().getBytes());
    }

    @Override
    public TaskResult compile(Submission submission) throws IOException {
        CommandLine cmd = new CommandLine("javac");
        cmd.addArgument(tmpFileName);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(tmpDir);
        executor.setWatchdog(new ExecuteWatchdog(60000));

        TaskResult compile = new TaskResult();
        if (executor.execute(cmd) == OK) {
            compile.setStatus(Submission.STATUS_AC);
        } else {
            compile.setStatus(Submission.STATUS_CE);
            compile.setError("Compile error");
        }
        return compile;
    }

    @Override
    public TaskResult run(Submission submission) throws IOException {
        TaskResult run = new TaskResult();

        CommandLine cmd = new CommandLine("java");
        cmd.addArgument("Main");

        ByteArrayInputStream stdin = new ByteArrayInputStream("07:05:45PM".getBytes());
        ByteArrayOutputStream stdout = new ByteArrayOutputStream(), stderr = new ByteArrayOutputStream();

        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(tmpDir);
        executor.setWatchdog(new ExecuteWatchdog(10000));
        executor.setStreamHandler(new PumpStreamHandler(stdout, stderr, stdin));
        executor.execute(cmd);

        if (stdout.toString().equals("19:05:45")) {
            run.setMemory(90);
            run.setRuntime(102);
            run.setStatus(Submission.STATUS_AC);
        } else {
            run.setStatus(Submission.STATUS_WA);
            run.setError("should be 19:05:45");
        }
        return run;
    }

    @Override
    public void clean() throws IOException {
        FileUtils.deleteDirectory(tmpDir);
    }
}