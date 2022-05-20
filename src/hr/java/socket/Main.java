package hr.java.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        int port = 505;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            int i = 1;
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected" + i);
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
                i++;
            }


        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
