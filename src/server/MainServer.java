package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server.utils.ConnectionsTracking;

/**
 * Espera na porta especificada e lança threads para comunicação com o Cliente Intermédio
 */
public class MainServer {
    private static final String SEP = "\n----------\n";
    private static final String LISTENING_INFO = SEP + "Listening for Main MainServer Clients on port: ";
    private static final String CLIENT_CONNECTED = SEP + "Client connected to MainServer! IP: ";

    public static void main(String[] args) throws IOException {

        /** Wait and Process client connections. */
        ConnectionsTracking connectionsTracking = new ConnectionsTracking();
        try (
            ServerSocket serverSocket = new ServerSocket(0)
        ) {
            System.out.print(LISTENING_INFO + serverSocket.getLocalPort() + SEP);
            Socket clientSocket;
            while (true) {
                clientSocket = serverSocket.accept();
                new ServerCommunicationThread(clientSocket, connectionsTracking).start();
                System.out.print(CLIENT_CONNECTED + clientSocket.getInetAddress() + "; PORT: " + clientSocket.getPort() + SEP);
            }
        } catch (IOException e) {
            System.err.println("Could not listen with ServerSocket!");
        }
    }
}