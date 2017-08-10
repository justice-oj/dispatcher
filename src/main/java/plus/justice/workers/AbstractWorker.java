package plus.justice.workers;

import plus.justice.models.database.Submission;
import plus.justice.models.sandbox.TaskResult;

import java.io.IOException;

public abstract class AbstractWorker implements IWorker {
    protected int OK = 0;
    // TODO: pattern with spring
    protected String codeDir = "/var/log/justice/code";

    public TaskResult work(Submission submission) throws IOException {
        save(submission);

        TaskResult result = compile(submission);
        if (result.getStatus() != OK) {
            clean();
            return result;
        }

        result = run(submission);
        clean();
        return result;
    }

    public abstract void save(Submission submission) throws IOException;

    public abstract TaskResult compile(Submission submission) throws IOException;

    public abstract TaskResult run(Submission submission) throws IOException;

    public abstract void clean() throws IOException;
}