package plus.justice.support;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import plus.justice.models.Submission;
import plus.justice.workers.*;

@Component
public class WorkerFactory {
    public Worker createWorker(Submission submission, Logger logger) {
        Worker worker;
        switch (submission.getLanguage()) {
            case Submission.LANGUAGE_C:
                worker = new CWorker(submission, logger);
                break;
            case Submission.LANGUAGE_CPP:
                worker = new CppWorker(submission, logger);
                break;
            case Submission.LANGUAGE_PYTHON2:
                worker = new Python2Worker(submission, logger);
                break;
            case Submission.LANGUAGE_PYTHON3:
                worker = new Python3Worker(submission, logger);
                break;
            case Submission.LANGUAGE_JAVA:
                worker = new JavaWorker(submission, logger);
                break;
            default:
                throw new RuntimeException("Language not supported yet");
        }
        return worker;
    }
}