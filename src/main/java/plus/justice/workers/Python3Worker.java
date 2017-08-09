package plus.justice.workers;

import org.slf4j.Logger;
import plus.justice.models.Submission;

public class Python3Worker implements Worker {
    private Submission submission;
    private Logger logger;

    public Python3Worker(Submission submission, Logger logger) {
        this.submission = submission;
        this.logger = logger;
    }

    @Override
    public void concatenate() {
        logger.info("Python 3 Worker concatenate submission #" + submission.getId());
    }

    @Override
    public void compile() {}

    @Override
    public String run() {
        return "{\"status\": 3, \"error\": \"error message here\"}";
    }
}