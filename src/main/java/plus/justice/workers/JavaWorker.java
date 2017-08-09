package plus.justice.workers;

import org.slf4j.Logger;
import plus.justice.models.Submission;

public class JavaWorker implements Worker {
    private Submission submission;
    private Logger logger;

    public JavaWorker(Submission submission, Logger logger) {
        this.submission = submission;
        this.logger = logger;
    }

    @Override
    public void concatenate() {
        logger.info("JavaWorker concatenate submission #" + submission.getId());
    }

    @Override
    public void compile() {
        logger.info("JavaWorker compile submission #" + submission.getId());
    }

    @Override
    public String run() {
        return "{\"runtime\": 12, \"memory\": 34, \"status\": 0}";
    }
}