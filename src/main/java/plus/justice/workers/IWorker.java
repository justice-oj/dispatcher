package plus.justice.workers;

import plus.justice.models.database.Submission;
import plus.justice.models.sandbox.TaskResult;

import java.io.IOException;

public interface IWorker {
    /**
     * do the whole work
     */
    TaskResult work(Submission submission) throws IOException;


    /**
     * save codes and save to disk
     */
    void save(Submission submission) throws IOException;


    /**
     * compile codes, for languages like python just return
     */
    TaskResult compile(Submission submission) throws IOException;


    /**
     * run in sandbox
     *
     * @return TaskResult
     */
    TaskResult run(Submission submission) throws IOException;


    /**
     * clean up
     */
    void clean() throws IOException;
}