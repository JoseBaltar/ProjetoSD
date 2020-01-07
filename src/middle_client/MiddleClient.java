package middle_client;

import com.google.gson.*;

import java.io.*;
import java.net.*;

import middle_client.utils.CreateWindow;
import middle_client.utils.EventTracking;
import middle_client.utils.UserTracking;

/**
 * Middle-Client (1º):
 * - Registo/Login no servidor <br/>
 * 
 * Abre threads com socket TCP para Clientes, que resumidamente fazem: <br/>
 * - Registo/Login do Cliente
 * - Depois do login, retorna o ip multicast e a porta para receber notificações (mais detalhes na thread). <br/>
 * - Quando chega uma notificação do Cliente, redireciona para o Servidor e espera pela resposta <br/>
 * - Se o evento é novo, abre um socket de comunicação de relatorios com o servidor (UDP) <br/>
 * - Se o evento ja esta ativo (evento na mesma localização, com o mesmo grau) nao faz uma nova conexão <bt/>
 */
public class MiddleClient {
    private static final String SEP = "\n----------\n";
    private static final String EXIT_INFO = SEP + "To close the connection to server write '%quit'." + SEP;
    private static final String EXIT_WARNING = SEP + "Connection with Server terminated!" + SEP;
    
    private static final String LOGIN = SEP + "Successfully Logged In. Starting Services ..." + SEP;
    private static final String CLIENT_MESSAGE = "\nClient: ";
    private static final String SERVER_RESPONSE = "Server: ";

    private static final String LISTENING_INFO = SEP + "Listening for Middle-Client Clients on port: ";
    private static final String CLIENT_CONNECTED = SEP + "Client connected to Middle-Client Server! IP: ";

    private static final String JSON_FILE_PATH = "../files/MiddleClientUsers.json";
    private static final String ASK_SERVER_INFO = SEP + "NOTICE: Check the Server connections list to get IP and Port Information." + SEP;
    private static final String ASK_SERVER_IP = "Enter Main-Server IP:\n> ";
    private static final String ASK_SERVER_PORT = "Enter Main-Server Port:\n> ";

    private static String locationName;

    public static void main(String[] args) throws IOException {

        MiddleClient client = new MiddleClient();

        /** Argument variables */
        String serverIP; // = args[0];
        int serverPort; // = Integer.parseInt(args[1]);
        boolean exit = false;

        /** After Login variables */
        String multicastIP = null;
        int multicastPort = 0;
        WaitOccurrenceThread waitOccurrenceThread = null;

        /** Shared Objects */
        UserTracking userTracking = new UserTracking();
        EventTracking eventTracking = new EventTracking();

        /** Instanciate all registered Users */
        userTracking.setRegisteredUsers(loadFromJSONFile(JSON_FILE_PATH));

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        while (!exit) {
            try {
                /** Connect to server.MainServer and start communications */
                System.out.print(ASK_SERVER_INFO + ASK_SERVER_IP);
                serverIP = stdIn.readLine();
                System.out.print(ASK_SERVER_PORT);
                serverPort = Integer.parseInt(stdIn.readLine());
            
                /** Connect to main serverSocket, serverSocket.Server */
                try (
                    Socket mainServerConnection = new Socket(serverIP, serverPort);
                    // Input from Server
                    BufferedReader from_server = new BufferedReader(new InputStreamReader(mainServerConnection.getInputStream()));
                    // Output to Server
                    PrintWriter to_server = new PrintWriter(mainServerConnection.getOutputStream(), true);
                ) {
                    boolean quit = false;
                    String userInput, serverOutput; // store input from user and output from protocol, respectively

                    to_server.println("?qweqweqweasdasdasd123654!"); // notify server this is a Middle-Client
                    serverOutput = from_server.readLine();

                    if (serverOutput.startsWith("Invalid User")) {
                        System.out.println(SEP + from_server + SEP);
                    } else {
                        System.out.print(EXIT_INFO + SERVER_RESPONSE + from_server.readLine() + CLIENT_MESSAGE);
                        while (!quit && (userInput = stdIn.readLine()) != null) {
                            /** Register and Login into Main Server before anything else */

                            if (userInput.equals("%quit")) {
                                to_server.println(userInput);
                                System.out.println(EXIT_WARNING);
                                quit = true;

                            } else {
                                to_server.println(userInput); // send input to server
                                serverOutput = from_server.readLine(); // get server output

                                if (serverOutput.equalsIgnoreCase("logged-in")) {
                                    /** Logged In, get server login data */
                                    System.out.print(LOGIN);

                                    /*
                                    TODO request ao servidor para verificar se existe um evento ativo nesta localização
                                    */

                                    try {
                                        // create thread for listening to server notifications
                                        waitOccurrenceThread = new WaitOccurrenceThread(eventTracking, userTracking);
                                    } catch (IOException e) {
                                        e.printStackTrace(); // socket error in thread
                                    }

                                    // send extra information to server, about the waitOccurrencePort, enabling the server to notify this Middle-Client
                                    to_server.println(mainServerConnection.getInetAddress().getHostAddress() + "/" + waitOccurrenceThread.getSocketPort());
                                    
                                    // get multicast IP and PORT and the Name of this location
                                    serverOutput = from_server.readLine();
                                    int sep1 = serverOutput.indexOf(":", 0), sep2 = serverOutput.indexOf("/", sep1);
                                    locationName = serverOutput.substring(0, sep1);
                                    multicastIP = serverOutput.substring(sep1 + 1, sep2);
                                    multicastPort = Integer.parseInt(serverOutput.substring(sep2 + 1));

                                    /** Start listening for Server Ocurrence notifications */
                                    waitOccurrenceThread.setMulticastIP(multicastIP);
                                    waitOccurrenceThread.setMulticastPort(multicastPort);
                                    waitOccurrenceThread.start();

                                    /** Wait and Process client connections */
                                    try (
                                        ServerSocket serverSocket = new ServerSocket(0)
                                    ) {
                                        to_server.println(serverSocket.getInetAddress().getHostAddress() + "/" + serverSocket.getLocalPort());
                                        System.out.print(LISTENING_INFO + serverSocket.getLocalPort() + SEP);
                                        CreateWindow.createActiveEventListWindow();
                                        CreateWindow.setDisplayText(" Middle-Client from " + locationName + "!\n Listening for clients at ->"
                                                                    + "\n\n Current events will show below\n    when active.");
                                        CreateWindow.setDisplayServerConnectionText("IP: " + serverSocket.getInetAddress().getHostAddress() 
                                                                            + "| PORT: " + serverSocket.getLocalPort());
                                        Socket clientConnection;
                                        while (true) {
                                            clientConnection = serverSocket.accept();
                                            new MiddleClientCommunicationThread(locationName, clientConnection, to_server, 
                                                            multicastIP, multicastPort, eventTracking, userTracking, JSON_FILE_PATH).start();
                                        System.out.print(CLIENT_CONNECTED + clientConnection.getInetAddress() + "; PORT: " + clientConnection.getPort() + SEP);
                                        }
                                    } catch (IOException e) {
                                        System.err.println("Could not listen with ServerSocket!");
                                    }

                                } else {
                                    // print server output
                                    System.out.print(SERVER_RESPONSE + serverOutput + CLIENT_MESSAGE);
                                }
                            } /** Login to server cicle */
                        }
                    }

                } catch (IOException e) {
                    System.err.println(SEP + "Couldn't get I/O for the connection. Server may be shut down!");
                } finally {
                    if (waitOccurrenceThread != null) waitOccurrenceThread.interrupt(); // close thread
                    // mainServerConnection.close(); // close socket
                }
            } catch (IOException e) {
                System.err.println("Error reading from System.in!");
                exit = true;
            } catch (NumberFormatException e) {
                System.err.println(SEP + "Server Port must be a valid Integer!");
            }

        } /** change location cicle */ 
        stdIn.close();
        CreateWindow.dispose();
    }

    public static String getThisLocationName() {
        return locationName;
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