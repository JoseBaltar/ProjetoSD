package server;

import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server.utils.UserTracking;
import server.utils.ConnectionsTracking;
import server.utils.EventTracking;

/**
 * Espera na porta especificada e lança threads para comunicação com o Cliente Intermédio
 */
public class MainServer {
    private static final String SEP = "\n----------\n";
    private static final String LISTENING_INFO = SEP + "Listening for Main MainServer Clients on port: ";
    private static final String CLIENT_CONNECTED = SEP + "Client connected to MainServer! IP: ";
    
    private static final String JSON_FILE_PATH = "../files/MainServerUsers.json";

    public static void main(String[] args) throws IOException {

        /** Shared Objects */
        UserTracking userTracking = new UserTracking();
        EventTracking eventTracking = new EventTracking();

        /** Instanciate all registered Users */
        userTracking.setRegisteredClients(loadFromJSONFile(JSON_FILE_PATH));
        ConnectionsTracking connectionsTracking = ConnectionsTracking.newInstance(userTracking.getAllRegisteredMulticastAddress());

        /** Wait and Process client connections. */
        try (
            ServerSocket serverSocket = new ServerSocket(0)
        ) {
            System.out.print(LISTENING_INFO + serverSocket.getLocalPort() + SEP);
            Socket clientSocket;
            while (true) {
                clientSocket = serverSocket.accept();
                new ServerCommunicationThread(clientSocket, userTracking, connectionsTracking, eventTracking, JSON_FILE_PATH).start();
                System.out.print(CLIENT_CONNECTED + clientSocket.getInetAddress() + "; PORT: " + clientSocket.getPort() + SEP);
            }
        } catch (IOException e) {
            System.err.println("Could not listen with ServerSocket!");
        }
    }

    private void createConnectionListWindow() {
        // lista com as conexão ao servidor e conexões de Middle-Clients
        // envia o objeto da TextArea para a thread de comunicação para escrever 
    }

    /**
     * Read a JSON file if JsonElement equals a JsonArray
     * 
     * @param file_path file path
     * @return JsonElement instance representing the file
     */
    private static JsonElement loadFromJSONFile(String file_path) {
        JsonElement json; // JsonElement correspondente ao ficheiro
        try
        { // Leitura do ficheiro e parse para uma instância de JsonElement
            FileReader inputFile = new FileReader(file_path);

            JsonParser parser = new JsonParser();
            json = parser.parse(inputFile);

        } catch (FileNotFoundException ex)
        { // Retorna null se o ficheiro não existir
            return null;
        }

        if (json.isJsonArray() && json.getAsJsonArray().size() == 0)
        {
            return null;
        }

        return json;
    }
}