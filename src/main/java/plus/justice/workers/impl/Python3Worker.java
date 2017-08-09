package plus.justice.workers.impl;

import org.slf4j.Logger;
import plus.justice.models.database.Submission;
import plus.justice.workers.IWorker;

public class Python3Worker implements IWorker {
    private Submission submission;
    private Logger logger;

    public Python3Worker(Submission submission, Logger logger) {
        this.submission = submission;
        this.logger = logger;
    }

    @Override
    public void concatenate() {
        logger.info("Python3 Worker concatenate submission #" + submission.getId());
    }

    @Override
    public void compile() {}

    @Override
    public String run() {
        return "{\"status\": 3, \"error\": \"error message here\"}";
    }
}