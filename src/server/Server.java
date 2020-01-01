package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server.utils.ClientConnectionTracking;

/**
 * Espera na porta especificada e lança threads para comunicação com o Cliente Intermédio
 */
public class Server {
    public static void main(String[] args) throws IOException {

        /** Wait and Process client connections. */
        ClientConnectionTracking shared = new ClientConnectionTracking();
        try (
            ServerSocket serverSocket = new ServerSocket()
        ) {
            System.out.println("Listening on Port " + serverSocket.getLocalPort() 
                            + " for Main Server Clients to connect.");
            Socket clientSocket;
            while (true) {
                clientSocket = serverSocket.accept();
                new ServerCommunicationThread(clientSocket, shared).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen with ServerSocket!");
        }
    }
}