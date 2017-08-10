package plus.justice.support;

import org.springframework.stereotype.Component;
import plus.justice.models.database.Submission;
import plus.justice.workers.AbstractWorker;
import plus.justice.workers.impl.JavaWorker;

@Component
public class WorkerFactory {
    public AbstractWorker createWorker(int language) {
        AbstractWorker worker;
        switch (language) {
            case Submission.LANGUAGE_JAVA:
                worker = new JavaWorker();
                break;
            default:
                throw new RuntimeException("Language not supported yet");
        }
        return worker;
    }
}