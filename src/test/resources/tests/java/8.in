import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class Main {
    public static void main(String args[]) throws Exception {
        Socket s = new Socket("www.amazon.com", 80);
        BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String answer = input.readLine();
        System.out.println(answer);
    }
}