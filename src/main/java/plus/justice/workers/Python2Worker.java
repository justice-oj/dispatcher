package plus.justice.workers;

import org.slf4j.Logger;
import plus.justice.models.Submission;

public class Python2Worker implements Worker {
    private Submission submission;
    private Logger logger;

    public Python2Worker(Submission submission, Logger logger) {
        this.submission = submission;
        this.logger = logger;
    }

    @Override
    public void concatenate() {
        logger.info("Python 2 Worker concatenate submission #" + submission.getId());
    }

    @Override
    public void compile() {}

    @Override
    public String run() {
        return "{\"runtime\": 12, \"memory\": 34, \"status\": 0}";
    }
}