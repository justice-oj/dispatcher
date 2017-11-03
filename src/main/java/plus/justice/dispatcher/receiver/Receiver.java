package plus.justice.dispatcher.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import plus.justice.dispatcher.models.amqp.QueueMessage;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.sandbox.TaskResult;
import plus.justice.dispatcher.repositories.SubmissionRepository;
import plus.justice.dispatcher.workers.impl.CLikeWorker;
import plus.justice.dispatcher.workers.impl.JavaWorker;

@Component
public class Receiver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SubmissionRepository submissionRepository;
    private final JavaWorker javaWorker;
    private final CLikeWorker cLikeWorker;

    @Autowired
    public Receiver(
            SubmissionRepository submissionRepository,
            JavaWorker javaWorker,
            CLikeWorker cLikeWorker
    ) {
        this.submissionRepository = submissionRepository;
        this.javaWorker = javaWorker;
        this.cLikeWorker = cLikeWorker;
    }

    @RabbitListener(queues = "${justice.rabbitmq.queue.name}")
    public void handleMessage(QueueMessage message) {
        try {
            Submission submission = submissionRepository.findOne(message.getId());
            if (submission == null) {
                logger.error("Submission #" + message.getId() + " not found.");
                return;
            }
            logger.info("Submission:" + submission.toString());

            TaskResult taskResult;
            if (submission.getLanguage().equals(Submission.LANGUAGE_C)) {
                cLikeWorker.setSuffix(".c");
                cLikeWorker.setStd("gnu11");
                cLikeWorker.setRealCompiler("/usr/bin/gcc");
                taskResult = cLikeWorker.work(submission);
            } else if (submission.getLanguage().equals(Submission.LANGUAGE_CPP)) {
                cLikeWorker.setSuffix(".cpp");
                cLikeWorker.setStd("gnu++14");
                cLikeWorker.setRealCompiler("/usr/bin/g++");
                taskResult = cLikeWorker.work(submission);
            } else if (submission.getLanguage().equals(Submission.LANGUAGE_JAVA)) {
                taskResult = javaWorker.work(submission);
            } else {
                logger.error("Language not supported: " + submission.getLanguage());
                return;
            }
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
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}