package plus.justice.dispatcher.judgers;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;
import plus.justice.dispatcher.Application;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.sandbox.TaskResult;
import plus.justice.dispatcher.workers.impl.CWorker;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CJudgerTest {
    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private CWorker cWorker;

    private Random random = new Random();

    private TaskResult getTaskResult(String s) throws IOException {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_C);
        submission.setCode(new String(Files.readAllBytes(resourceLoader.getResource(s).getFile().toPath())));
        return cWorker.work(submission);
    }

    @Test
    public void t000AC() throws Exception {
        TaskResult taskResult = getTaskResult("classpath:tests/c/0.c");

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_AC);
    }
}