import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String args[]) throws Exception {
        ServerSocket serverSocket = new ServerSocket(44444);
        Socket socket = serverSocket.accept();
        OutputStream os = socket.getOutputStream();
        PrintWriter pw = new PrintWriter(os, true);
        pw.close();
        socket.close();
    }
}