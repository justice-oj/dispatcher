package plus.justice.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import plus.justice.models.QueueMessage;
import plus.justice.models.Submission;
import plus.justice.models.TaskResult;
import plus.justice.repositories.SubmissionRepository;
import plus.justice.support.WorkerFactory;
import plus.justice.workers.Worker;

import java.io.IOException;

@Component
@PropertySource("classpath:config.properties")
public class Receiver {
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
    private final SubmissionRepository submissionRepository;
    private final WorkerFactory factory;
    private final ObjectMapper mapper;

    @Autowired
    public Receiver(SubmissionRepository submissionRepository, WorkerFactory factory, ObjectMapper mapper) {
        this.submissionRepository = submissionRepository;
        this.factory = factory;
        this.mapper = mapper;
    }

    @RabbitListener(queues = "${justice.rabbitmq.queue.name}")
    public void handleMessage(QueueMessage message) throws InterruptedException, IOException {
        Submission submission = submissionRepository.findOne(message.getId());
        logger.info("Message:" + message.toString());
        logger.info("Submission:" + submission.toString());

        Worker worker = factory.createWorker(submission, logger);
        worker.concatenate();
        worker.compile();
        TaskResult taskResult = mapper.readValue(worker.run(), TaskResult.class);
        logger.info("Sandbox returns: " + taskResult.toString());

        if (taskResult.getStatus() == Submission.STATUS_AC) {
            submission.setStatus(taskResult.getStatus());
            submission.setRuntime(taskResult.getRuntime());
            submission.setMemory(taskResult.getMemory());
        } else {
            submission.setStatus(taskResult.getStatus());
        }
        submissionRepository.save(submission);
    }
}