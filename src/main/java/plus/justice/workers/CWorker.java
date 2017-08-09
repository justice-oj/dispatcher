package plus.justice.workers;

import org.slf4j.Logger;
import plus.justice.models.Submission;

public class CWorker implements Worker {
    private Submission submission;
    private Logger logger;

    public CWorker(Submission submission, Logger logger) {
        this.submission = submission;
        this.logger = logger;
    }

    @Override
    public void concatenate() {
        logger.info("CWorker concatenate submission #" + submission.getId());
    }

    @Override
    public void compile() {
        logger.info("CWorker compile submission #" + submission.getId());
    }

    @Override
    public String run() {
        return "{\"status\": 3, \"error\": \"error message here\"}";
    }
}