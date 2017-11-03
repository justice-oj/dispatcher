package plus.justice.dispatcher.workers;

import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.sandbox.TaskResult;

import java.io.IOException;

public interface IWorker {
    TaskResult work(Submission submission) throws IOException;

    void save() throws IOException;

    TaskResult compile() throws IOException;

    TaskResult run() throws IOException;

    void clean();
}
