package plus.justice.workers.impl;

import org.slf4j.Logger;
import plus.justice.models.database.Submission;
import plus.justice.workers.IWorker;

public class JavaWorker implements IWorker {
    private Submission submission;
    private Logger logger;

    public JavaWorker(Submission submission, Logger logger) {
        this.submission = submission;
        this.logger = logger;
    }

    @Override
    public void concatenate() {
        logger.info("Java Worker concatenate submission #" + submission.getId());
    }

    @Override
    public void compile() {
        logger.info("Java Worker compile submission #" + submission.getId());
    }

    @Override
    public String run() {
        return "{\"runtime\": 12, \"memory\": 34, \"status\": 0}";
    }
}