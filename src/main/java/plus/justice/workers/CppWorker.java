package plus.justice.workers;

import org.slf4j.Logger;
import plus.justice.models.Submission;

public class CppWorker implements Worker {
    private Submission submission;
    private Logger logger;

    public CppWorker(Submission submission, Logger logger) {
        this.submission = submission;
        this.logger = logger;
    }

    @Override
    public void concatenate() {
        logger.info("C++Worker concatenate submission #" + submission.getId());
    }

    @Override
    public void compile() {
        logger.info("C++Worker compile submission #" + submission.getId());
    }

    @Override
    public String run() {
        return "{\"status\": 3, \"error\": \"error message here\"}";
    }
}