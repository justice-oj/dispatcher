package plus.justice.dispatcher.workers;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import plus.justice.dispatcher.models.database.Problem;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.sandbox.TaskResult;
import plus.justice.dispatcher.repositories.ProblemRepository;
import plus.justice.dispatcher.repositories.TestCaseRepository;

import java.io.File;
import java.io.IOException;

@Component
public abstract class WorkerAbstract implements IWorker {
    // current submission
    protected Submission submission;

    // related problem
    protected Problem problem;

    // current working directory
    protected String cwd;

    // tmp taskResult exitCode, 0 stands for OK
    protected int OK = 0;

    protected TestCaseRepository testCaseRepository;
    private ProblemRepository problemRepository;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public WorkerAbstract(TestCaseRepository testCaseRepository, ProblemRepository problemRepository) {
        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    public TaskResult work(Submission submission) throws IOException {
        this.submission = submission;
        this.problem = problemRepository.findOne(submission.getProblemId());

        this.save();
        TaskResult result = this.compile();
        if (result.getStatus() == OK) result = this.run();
        this.clean();
        return result;
    }

    public abstract void save() throws IOException;

    public abstract TaskResult compile() throws IOException;

    public abstract TaskResult run() throws IOException;

    public void clean() {
        try {
            FileUtils.deleteDirectory(new File(cwd));
        } catch (IOException e) {
            logger.warn("Remove dir:\t" + cwd + " failed");
        }
    }
}
