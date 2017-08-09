package plus.justice.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import plus.justice.models.QueueMessage;
import plus.justice.models.Submission;
import plus.justice.repositories.SubmissionRepository;

@Component
@PropertySource("classpath:config.properties")
public class Receiver {
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    private final SubmissionRepository submissionRepository;

    @Autowired
    public Receiver(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @RabbitListener(queues = "${justice.rabbitmq.queue.name}")
    public void handleMessage(QueueMessage message) throws InterruptedException {
        logger.info("Message: " + message.toString());

        Submission submission = submissionRepository.findOne(message.getId());
        logger.info("Submission: " + submission.toString());

        submission.setStatus(0);
        submission.setRuntime(123);
        submission.setMemory(456);
        submissionRepository.save(submission);
    }
}