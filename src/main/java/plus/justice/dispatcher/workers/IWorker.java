package plus.justice.dispatcher.workers;

import plus.justice.dispatcher.models.database.Problem;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.database.TestCase;

import java.io.IOException;
import java.util.List;

public interface IWorker {
    void save(String cwd, Submission submission) throws IOException;

    void compile(String cwd, Submission submission) throws RuntimeException;

    void run(String cwd, Problem problem, List<TestCase> testCases, Submission submission) throws RuntimeException;
}
