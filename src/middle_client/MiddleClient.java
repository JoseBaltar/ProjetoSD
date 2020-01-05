package middle_client;

import java.io.*;
import java.net.*;

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

    public static void main(String[] args) throws IOException {
        /** Check arguments */
        if (args.length != 2) {
            System.out.println("\nWrong Arguments!\nUsage:\n\t" 
                + "java middle_client.MiddleClient <main_server_ip> <main_server_port>");
        } else if (!args[1].matches("\\d+")) {
            System.out.println("\nWrong Arguments! Make sure <serverPort> is a valid integer.\nUsage:\n\t" 
                + "java middle_client.MiddleClient <main_server_ip> <main_server_port>");
        } else {

            /** Argument variables */
            String serverIP = args[0];
            int serverPort = Integer.parseInt(args[1]);

            /** After Login variables */
            String locationName = "";
            String multicastIP = null;
            int multicastPort = 0;
            WaitOccurrenceThread waitOccurrenceThread = null;

            /** Shared Objects */
            UserTracking userTracking = new UserTracking();
            EventTracking eventTracking = new EventTracking();

            /** Connect to main serverSocket, serverSocket.Server */
            try (
                Socket mainServerConnection = new Socket(serverIP, serverPort);
                // Input from Server
                BufferedReader from_server = new BufferedReader(new InputStreamReader(mainServerConnection.getInputStream()));
                // Output to Server
                PrintWriter to_server = new PrintWriter(mainServerConnection.getOutputStream(), true);
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            ) {
                boolean quit = false;
                String userInput;
                String serverOutput;
                System.out.print(EXIT_INFO + SERVER_RESPONSE + from_server.readLine() + CLIENT_MESSAGE);
                /** Register and Login into Main Server before anything else */
                while (!quit && (userInput = stdIn.readLine()) != null) {
                    // terminate communication
                    if (userInput.equals("%quit")) {
                        to_server.println(userInput);
                        System.out.println(EXIT_WARNING);
                        quit = true;

                    } else {
                        // send input to server
                        to_server.println(userInput);
                        // get server output
                        serverOutput = from_server.readLine();
                        if (serverOutput.equalsIgnoreCase("logged-in")) {
                            System.out.print(LOGIN);

                            /** Logged In, get server login data */
                            // get multicast IP and PORT  and the Name of this location
                            serverOutput = from_server.readLine();
                            int sep1 = serverOutput.indexOf(":", 0), sep2 = serverOutput.indexOf("/", sep1);
                            locationName = serverOutput.substring(0, sep1);
                            multicastIP = serverOutput.substring(sep1 + 1, sep2);
                            multicastPort = Integer.parseInt(serverOutput.substring(sep2 + 1));
                            // create thread for listening to server notifications
                            waitOccurrenceThread = new WaitOccurrenceThread(multicastIP, multicastPort, eventTracking);
                            // send extra information to server, about the waitOccurrencePort, enabling the server to notify this Middle-Client
                            to_server.println(waitOccurrenceThread.getSocketPort());

                            /** Start listening for Server Ocurrence notifications */
                            waitOccurrenceThread.start();

                            /** Wait and Process client connections */
                            try (
                                ServerSocket serverSocket = new ServerSocket(0)
                            ) {
                                System.out.println(SEP + "Listening on Port " + serverSocket.getLocalPort() + " for Citizen Clients to connect." + SEP);
                                Socket clientConnection;
                                while (true) {
                                    clientConnection = serverSocket.accept();
                                    new MiddleClientCommunicationThread(locationName, clientConnection, to_server, 
                                                    multicastIP, multicastPort, eventTracking, userTracking).start();
                                }
                            } catch (IOException e) {
                                System.err.println("Could not listen with ServerSocket!");
                            }

                        } else {
                            // print server output
                            System.out.print(SERVER_RESPONSE + serverOutput + CLIENT_MESSAGE);
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection.");
                e.printStackTrace();
            } finally {
                if (waitOccurrenceThread != null) waitOccurrenceThread.interrupt(); // close thread
                // mainServerConnection.close(); // close socket
            }
        }
    }
}