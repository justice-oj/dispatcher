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
import plus.justice.dispatcher.receiver.Context;
import plus.justice.dispatcher.repositories.ProblemRepository;
import plus.justice.dispatcher.repositories.TestCaseRepository;
import plus.justice.dispatcher.workers.impl.JavaWorker;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaJudgerTest {
    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private Context context;

    @Autowired
    private JavaWorker javaWorker;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    private final Random random = new Random();
    private final Submission submission = new Submission();

    private Submission getTaskResult(String s) throws IOException {
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setStatus(Submission.STATUS_QUEUE);
        submission.setCode(new String(Files.readAllBytes(resourceLoader.getResource(s).getFile().toPath())));

        context.setSubmission(submission);
        context.setProblem(problemRepository.findOne(context.getSubmission().getProblemId()));
        context.setTestCases(testCaseRepository.findByProblemId(context.getProblem().getId()));
        context.setWorker(javaWorker);
        context.process();

        return context.getSubmission();
    }


    @Test
    public void t000AC() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/0.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_AC);
    }

    @Test
    public void t001PlainText() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/1.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_CE);
        assertThat(submission.getError()).contains("Compile error");
    }

    @Test
    public void t002SyntaxError() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/2.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_CE);
        assertThat(submission.getError()).contains("Compile error");
    }

    @Test
    public void t003OutOfIndex() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/3.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError()).contains("String index out of range").contains("Exception ");
    }

    @Test
    public void t004NullPointerException() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/4.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError()).contains("NullPointerException");
    }

    @Test
    public void t005ProhibitReadingFile() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/5.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.io.FilePermission");
    }

    @Test
    public void t006ProhibitWritingFile() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/6.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.io.FilePermission");
    }

    @Test
    public void t007ProhibitTCPServer() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/7.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.net.SocketPermission");
    }

    @Test
    public void t008ProhibitTCPClient() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/8.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.net.SocketPermission");
    }

    @Test
    public void t009ProhibitCallingCLI() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/9.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.io.FilePermission");
    }

    @Test
    public void t010ProhibitGettingEnvParam() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/10.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.lang.RuntimePermission");
    }

    @Test
    public void t011CPURuntimeLimitExceeded() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/11.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_TLE);
        assertThat(submission.getError()).contains("Time Limit Exceeded");
    }

    @Test
    public void t012RuntimeLimitExceeded() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/12.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_TLE);
        assertThat(submission.getError()).contains("Time Limit Exceeded");
    }

    @Test
    public void t013WA() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/13.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_WA);
        assertThat(submission.getInput()).contains("07:05:45PM");
        assertThat(submission.getOutput()).contains("1234");
        assertThat(submission.getExpected()).contains("19:05:45");
    }

    @Test
    public void t014ProhibitGettingSystemProperty() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/14.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.util.PropertyPermission");
    }

    @Test
    public void t015ProhibitUDPClient() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/15.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.net.SocketPermission");
    }

    @Test
    public void t016ProhibitUDPServer() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/16.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.net.SocketPermission");
    }

    @Test
    public void t017ProhibitMulticastClient() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/17.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.net.SocketPermission");
    }

    @Test
    public void t018ProhibitMulticastServer() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/18.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.net.SocketPermission");
    }

    @Test
    public void t019ProhibitSettingProperty() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/19.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.util.PropertyPermission");
    }

    @Test
    public void t020ProhibitOverridingSecurityManager() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/20.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(submission.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.lang.RuntimePermission");
    }

    @Test
    public void t021OutputTooLong() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/21.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_WA);
        assertThat(submission.getOutput()).contains("1111");
        assertThat(submission.getOutput()).contains("...");
    }

    @Test
    public void t022CompileTimeExceeded() throws Exception {
        Submission submission = getTaskResult("classpath:tests/java/22.in");

        assertThat(submission.getStatus()).isEqualTo(Submission.STATUS_CE);
        assertThat(submission.getError()).contains("Compile Time Exceeded");
    }
}