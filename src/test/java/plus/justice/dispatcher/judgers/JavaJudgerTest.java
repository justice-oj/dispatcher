package plus.justice.dispatcher.judgers;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import plus.justice.dispatcher.Application;
import plus.justice.dispatcher.models.database.Submission;
import plus.justice.dispatcher.models.sandbox.TaskResult;
import plus.justice.dispatcher.workers.impl.JavaWorker;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaJudgerTest {
    @Autowired
    private JavaWorker javaWorker;

    private Random random = new Random();

    @Test
    public void test001CompileError1() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("this is a plain test");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_CE);
        assertThat(taskResult.getError()).contains("class, interface, or enum expected");
    }

    @Test
    public void test002CompileError2() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("import java.util.Scanner;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        Scanner in = new Scanner(System.in);\n" +
                "        String s = in.nextLine();\n" +
                "        if (s.charAt(0) == '1' && s.charAt(1) == '2' && s.charAt(s.length() - 2) == 'A') {\n" +
                "            System.out.print(\"00\" + s.substring(2, s.length() - 2));\n" +
                "        } else if (s.charAt(0) == '1' && s.charAt(1) == '2' && s.charAt(s.length() - 2) == 'P') {\n" +
                "            System.out.print(\"12\");\n" +
                "        } else {\n" +
                "            if (s.charAt(s.length() - 2) == 'A') {\n" +
                "                System.out.print(222));\n" +
                "            } else {\n" +
                "                int h = s.charAt(0) == '0' ? Integer.valueOf(s.substring(1, 2)) : Integer.valueOf(s.substring(0, 2));\n" +
                "                System.out.print(4444));\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_CE);
        assertThat(taskResult.getError()).contains("error: ';' expected");
    }

    @Test
    public void test003RuntimeError1() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("import java.util.Scanner;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        Scanner in = new Scanner(System.in);\n" +
                "        String s = in.nextLine();\n" +
                "        if (s.charAt(0) == '1' && s.charAt(1) == '2' && s.charAt(s.length() - 2) == 'A') {\n" +
                "            System.out.print(\"00\" + s.substring(22222, s.length() - 2));\n" +
                "        } else if (s.charAt(0) == '1' && s.charAt(1) == '2' && s.charAt(s.length() - 2) == 'P') {\n" +
                "            System.out.print(\"12\" + s.substring(23333, s.length() - 2));\n" +
                "        } else {\n" +
                "            if (s.charAt(s.length() - 2) == 'A') {\n" +
                "                System.out.print(s.substring(0, s.length() - 2));\n" +
                "            } else {\n" +
                "                int h = s.charAt(0) == '0' ? Integer.valueOf(s.substring(1, 2)) : Integer.valueOf(s.substring(0, 2));\n" +
                "                System.out.print(String.valueOf(h + 122222) + s.substring(2, s.length() - 2222));\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(taskResult.getError()).contains("String index out of range").contains("Exception ");
    }

    @Test
    public void test004RuntimeError2() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        String s = null;\n" +
                "        if (s.charAt(0) == '1' && s.charAt(1) == '2' && s.charAt(s.length() - 2) == 'A') {\n" +
                "            System.out.print(\"00\" + s.substring(2, s.length() - 2));\n" +
                "        } else {\n" +
                "            System.out.print(\"12\" + s.substring(2, s.length() - 2));\n" +
                "        }\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(taskResult.getError()).contains("NullPointerException");
    }

    @Test
    public void test005RuntimeError3() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("import java.io.FileReader;\n" +
                "import java.util.Scanner;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String args[]) throws Exception {\n" +
                "        Scanner test = new Scanner(new FileReader(\"/etc/hosts\"));\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(taskResult.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.io.FilePermission");
    }

    @Test
    public void test006RuntimeError4() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("import java.io.PrintWriter;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String args[]) throws Exception {\n" +
                "        PrintWriter out = new PrintWriter(\"test.txt\");\n" +
                "        out.println(\"WARNING: I shouldn't be here.\");\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(taskResult.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.io.FilePermission");
    }

    @Test
    public void test007RuntimeError5() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("import java.io.OutputStream;\n" +
                "import java.io.PrintWriter;\n" +
                "import java.net.ServerSocket;\n" +
                "import java.net.Socket;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String args[]) throws Exception {\n" +
                "        ServerSocket serverSocket = new ServerSocket(44123);\n" +
                "        Socket socket = serverSocket.accept();\n" +
                "        OutputStream os = socket.getOutputStream();\n" +
                "        PrintWriter pw = new PrintWriter(os, true);\n" +
                "        pw.close();\n" +
                "        socket.close();\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(taskResult.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.net.SocketPermission");
    }

    @Test
    public void test008RuntimeError6() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("import java.io.BufferedReader;\n" +
                "import java.io.InputStreamReader;\n" +
                "import java.net.Socket;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String args[]) throws Exception {\n" +
                "        Socket s = new Socket(\"www.amazon.com\", 80);\n" +
                "        BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));\n" +
                "        String answer = input.readLine();\n" +
                "        System.out.println(answer);\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(taskResult.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.net.SocketPermission");
    }

    @Test
    public void test009RuntimeError7() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("public class Main {\n" +
                "    public static void main(String args[]) throws Exception {\n" +
                "        Runtime rt = Runtime.getRuntime();\n" +
                "        Process p = rt.exec(\"/usr/bin/ls\");\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(taskResult.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.io.FilePermission");
    }

    @Test
    public void test010RuntimeError8() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("public class Main {\n" +
                "    public static void main(String args[]) throws Exception {\n" +
                "        System.out.println(System.getenv(\"PATH\"));\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_RE);
        assertThat(taskResult.getError())
                .contains("java.security.AccessControlException: access denied")
                .contains("java.lang.RuntimePermission");
    }

    @Test
    public void test011TimeLimitExceededError1() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("import java.io.*;\n" +
                "import java.util.*;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String args[] ) throws Exception {\n" +
                "        while(true) {\n" +
                "            int a = 222;\n" +
                "        }\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_TLE);
        assertThat(taskResult.getError()).contains("Time Limit Exceeded");
    }

    @Test
    public void test012TimeLimitExceededError2() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("import java.util.concurrent.TimeUnit;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String args[]) throws Exception {\n" +
                "        TimeUnit.MINUTES.sleep(1);\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_TLE);
        assertThat(taskResult.getError()).contains("Time Limit Exceeded");
    }

    @Test
    public void test013WrongAnswerError1() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("import java.util.Scanner;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        Scanner in = new Scanner(System.in);\n" +
                "        String s = in.nextLine();\n" +
                "        if (s.charAt(0) == '1' && s.charAt(1) == '2' && s.charAt(s.length() - 2) == 'A') {\n" +
                "            System.out.print(\"00\" + s.substring(2, s.length() - 2));\n" +
                "        } else if (s.charAt(0) == '1' && s.charAt(1) == '2' && s.charAt(s.length() - 2) == 'P') {\n" +
                "            System.out.print(\"12\");\n" +
                "        } else {\n" +
                "            if (s.charAt(s.length() - 2) == 'A') {\n" +
                "                System.out.print(222);\n" +
                "            } else {\n" +
                "                int h = s.charAt(0) == '0' ? Integer.valueOf(s.substring(1, 2)) : Integer.valueOf(s.substring(0, 2));\n" +
                "                System.out.print(4444);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_WA);
        assertThat(taskResult.getInput()).contains("07:05:45PM");
        assertThat(taskResult.getOutput()).contains("4444");
        assertThat(taskResult.getExpected()).contains("19:05:45");
    }

    @Test
    public void test014Accepted1() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("import java.util.Scanner;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        Scanner in = new Scanner(System.in);\n" +
                "        String s = in.nextLine();\n" +
                "        if (s.charAt(0) == '1' && s.charAt(1) == '2' && s.charAt(s.length() - 2) == 'A') {\n" +
                "            System.out.println(\"00\" + s.substring(2, s.length() - 2));\n" +
                "        } else if (s.charAt(0) == '1' && s.charAt(1) == '2' && s.charAt(s.length() - 2) == 'P') {\n" +
                "            System.out.println(\"12\" + s.substring(2, s.length() - 2));\n" +
                "        } else {\n" +
                "            if (s.charAt(s.length() - 2) == 'A') {\n" +
                "                System.out.println(s.substring(0, s.length() - 2));\n" +
                "            } else {\n" +
                "                int h = s.charAt(0) == '0' ? Integer.valueOf(s.substring(1, 2)) : Integer.valueOf(s.substring(0, 2));\n" +
                "                System.out.println(String.valueOf(h + 12) + s.substring(2, s.length() - 2));\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
        TaskResult taskResult = javaWorker.work(submission);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_AC);
    }

    @Test
    public void test015SecurityManagerAllowed() throws Exception {
        Submission submission = new Submission();
        submission.setId(Math.abs(random.nextLong()));
        submission.setProblemId(1L);
        submission.setLanguage(Submission.LANGUAGE_JAVA);
        submission.setCode("public class Main {\n" +
                "    public static void main(String args[]) throws Exception {\n" +
                "        System.out.println(System.getProperty(\"java.version\", \"unknown\"));\n" +
                "    }\n" +
                "}");
        TaskResult taskResult = javaWorker.work(submission);

        System.out.println(taskResult);

        assertThat(taskResult.getStatus()).isEqualTo(Submission.STATUS_WA);
        assertThat(taskResult.getOutput()).contains("1.8.0_");
    }
}