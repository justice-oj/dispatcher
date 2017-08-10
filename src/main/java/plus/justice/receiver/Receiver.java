package plus.justice.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import plus.justice.models.amqp.QueueMessage;
import plus.justice.models.database.Submission;
import plus.justice.models.sandbox.TaskResult;
import plus.justice.repositories.ProblemRepository;
import plus.justice.repositories.SubmissionRepository;
import plus.justice.support.WorkerFactory;
import plus.justice.workers.IWorker;

import java.io.IOException;

@Component
@PropertySource("classpath:config.properties")
public class Receiver {
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final WorkerFactory factory;

    @Autowired
    public Receiver(
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository,
            WorkerFactory factory
    ) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.factory = factory;
    }

    @RabbitListener(queues = "${justice.rabbitmq.queue.name}")
    public void handleMessage(QueueMessage message) {
        Submission submission = submissionRepository.findOne(message.getId());
        logger.info("Submission:" + submission.toString());

        IWorker worker = factory.createWorker(submission.getLanguage());
        try {
            TaskResult taskResult = worker.work(submission);
            logger.info("Sandbox returns: " + taskResult.toString());

            if (taskResult.getStatus() == Submission.STATUS_AC) {
                submission.setStatus(taskResult.getStatus());
                submission.setRuntime(taskResult.getRuntime());
                submission.setMemory(taskResult.getMemory());
            } else {
                submission.setStatus(taskResult.getStatus());
            }
            submissionRepository.save(submission);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}