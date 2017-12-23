package plus.justice.dispatcher.receiver;

import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import plus.justice.dispatcher.models.database.Problem;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.database.TestCase;
import plus.justice.dispatcher.workers.IWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@Data
@PropertySource("classpath:config-${spring.profiles.active}.properties")
public class Context {
    @Value("${justice.judger.code.tmp.basedir}")
    private String baseDir;

    // current worker
    private IWorker worker;
    // current submission
    private Submission submission;
    // related problem
    private Problem problem;
    // related test cases
    private List<TestCase> testCases;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void process() throws IOException {
        String cwd = baseDir + File.separator + submission.getId();
        Files.createDirectories(Paths.get(cwd));

        worker.save(cwd, submission);

        try {
            worker.compile(cwd, submission);
            worker.run(cwd, problem, testCases, submission);
        } catch (RuntimeException ignored) {
        } finally {
            try {
                FileUtils.deleteDirectory(new File(cwd));
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        }
    }
}
