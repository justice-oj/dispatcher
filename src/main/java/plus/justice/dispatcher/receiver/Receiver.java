package plus.justice.dispatcher.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import plus.justice.dispatcher.models.amqp.QueueMessage;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.sandbox.TaskResult;
import plus.justice.dispatcher.repositories.SubmissionRepository;
import plus.justice.dispatcher.workers.impl.JavaWorker;

import java.io.IOException;

@Component
@PropertySource("classpath:config.properties")
public class Receiver {
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
    private final SubmissionRepository submissionRepository;
    private final JavaWorker javaWorker;

    @Autowired
    public Receiver(
            SubmissionRepository submissionRepository,
            JavaWorker javaWorker
    ) {
        this.submissionRepository = submissionRepository;
        this.javaWorker = javaWorker;
    }

    @RabbitListener(queues = "${justice.rabbitmq.queue.name}")
    public void handleMessage(QueueMessage message) {
        Submission submission = submissionRepository.findOne(message.getId());
        logger.info("Submission:" + submission.toString());

        try {
            TaskResult taskResult = javaWorker.work(submission);
            logger.info("Sandbox returns: " + taskResult.toString());

            submission.setStatus(taskResult.getStatus());
            if (taskResult.getStatus() == Submission.STATUS_AC) {
                submission.setRuntime(taskResult.getRuntime());
                submission.setMemory(taskResult.getMemory());
            } else if (taskResult.getStatus() == Submission.STATUS_WA) {
                submission.setInput(taskResult.getInput());
                submission.setOutput(taskResult.getOutput());
                submission.setExpected(taskResult.getExpected());
            } else {
                submission.setError(taskResult.getError());
            }
            submissionRepository.save(submission);
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }
}