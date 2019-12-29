package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Espera na porta especificada e lança threads para comunicação com o Cliente Intermédio
 */
public class Server {
    public static void main(String[] args) throws IOException {
        /** Check arguments */
        if (args.length != 1 && !args[0].matches("\\d+")) {
            System.out.println("\tWrong Arguments! Make sure <port> is a valid integer.\n"
                    + "Usage:\n\tjava server.Server <server_listening_port>");
        } else {

            /** Wait and Process client connections. */
            int port = Integer.parseInt(args[0]);
            NetworkPorts shared = new NetworkPorts();
            System.out.println("Listening on Port " + port + " for Main Server Clients to connect.");
            try (
                ServerSocket serverSocket = new ServerSocket()
            ) {
                Socket clientSocket;
                while (true) {
                    clientSocket = serverSocket.accept();
                    new ServerCommunicationThread(clientSocket, shared).start();
                }
            } catch (IOException e) {
                System.err.println("Could not listen on port: " + port + " .");
            }
        }
    }
}