package plus.justice.dispatcher.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import plus.justice.dispatcher.models.amqp.QueueMessage;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.repositories.ProblemRepository;
import plus.justice.dispatcher.repositories.SubmissionRepository;
import plus.justice.dispatcher.repositories.TestCaseRepository;
import plus.justice.dispatcher.workers.impl.CLikeWorker;
import plus.justice.dispatcher.workers.impl.JavaWorker;

@Component
@PropertySource("classpath:config-${spring.profiles.active}.properties")
public class Receiver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final Context context;
    private final CLikeWorker cLikeWorker;
    private final JavaWorker javaWorker;

    @Value("${justice.judger.c.compiler}")
    private String cRealCompiler;

    @Value("${justice.judger.cpp.compiler}")
    private String cppRealCompiler;

    @Value("${justice.judger.c.std}")
    private String cStd;

    @Value("${justice.judger.cpp.std}")
    private String cppStd;

    @Value("${justice.judger.c.suffix}")
    private String cSuffix;

    @Value("${justice.judger.cpp.suffix}")
    private String cppSuffix;

    @Autowired
    public Receiver(
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository,
            Context context,
            CLikeWorker cLikeWorker,
            JavaWorker javaWorker
    ) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.context = context;
        this.cLikeWorker = cLikeWorker;
        this.javaWorker = javaWorker;
    }

    @RabbitListener(queues = "${justice.rabbitmq.queue.name}")
    public void handleMessage(QueueMessage message) {
        context.setSubmission(submissionRepository.findById(message.getId()).orElse(null));
        logger.info("Submission: #" + context.getSubmission().getId());
        context.setProblem(problemRepository.findById(context.getSubmission().getProblemId()).orElse(null));
        logger.info("Problem: #" + context.getProblem().getId());
        context.setTestCases(testCaseRepository.findByProblemId(context.getProblem().getId()));

        if (context.getSubmission().getLanguage().equals(Submission.LANGUAGE_C)) {
            cLikeWorker.setRealCompiler(cRealCompiler);
            cLikeWorker.setStd(cStd);
            cLikeWorker.setSuffix(cSuffix);
            context.setWorker(cLikeWorker);
            logger.info("Context set cWorker.");
        } else if (context.getSubmission().getLanguage().equals(Submission.LANGUAGE_CPP)) {
            cLikeWorker.setRealCompiler(cppRealCompiler);
            cLikeWorker.setStd(cppStd);
            cLikeWorker.setSuffix(cppSuffix);
            context.setWorker(cLikeWorker);
            logger.info("Context set cppWorker.");
        } else if (context.getSubmission().getLanguage().equals(Submission.LANGUAGE_JAVA)) {
            context.setWorker(javaWorker);
            logger.info("Context set javaWorker.");
        } else {
            logger.error("Language type not supported: " + context.getSubmission().getLanguage());
            return;
        }

        try {
            context.process();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            submissionRepository.save(context.getSubmission());
        }
    }
}