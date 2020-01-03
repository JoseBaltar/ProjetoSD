package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server.utils.ClientConnectionTracking;

/**
 * Espera na porta especificada e lança threads para comunicação com o Cliente Intermédio
 */
public class MainServer {
    private static final String SEP = "\n----------\n";
    private static final String LISTENING_INFO = SEP + "Listening for Main MainServer Clients on port: ";
    private static final String CLIENT_CONNECTED = SEP + "Client connected to MainServer! IP: ";

    public static void main(String[] args) throws IOException {

        /** Wait and Process client connections. */
        ClientConnectionTracking shared = new ClientConnectionTracking();
        try (
            ServerSocket serverSocket = new ServerSocket(0)
        ) {
            System.out.print(LISTENING_INFO + serverSocket.getLocalPort() + SEP);
            Socket clientSocket;
            while (true) {
                clientSocket = serverSocket.accept();
                new ServerCommunicationThread(clientSocket, shared).start();
                System.out.print(CLIENT_CONNECTED + clientSocket.getInetAddress() + SEP);
            }
        } catch (IOException e) {
            System.err.println("Could not listen with ServerSocket!");
        }
    }
}