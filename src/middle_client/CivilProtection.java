package middle_client;

import java.io.*;
import java.net.*;

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
public class CivilProtection {
    private static final String EXIT_INFO = "-------------\nTo close the connection to server write '%quit'.\n-------------\n";
    private static final String EXIT_WARNING = "\n-----\nConnection with Server terminated!\n-----\n";
    private static final String CLIENT_MESSAGE = "\nClient: ";
    private static final String SERVER_RESPONSE = "Server: ";

    public static void main(String[] args) throws IOException {
        /** Check arguments */
        if (args.length != 2) {
            System.out.println("Wrong Arguments!\n\nUsage:\n\t" 
                + "java middle_client.CivilProtection <main_server_ip> <main_server_port>");
        } else if (!args[1].matches("\\d+")) {
            System.out.println("Wrong Arguments! Make sure <serverPort> is a valid integer.\n\nUsage:\n\t" 
                + "java middle_client.CivilProtection <main_server_ip> <main_server_port>");
        } else {

            /** Argument variables */
            String serverIP = args[0];
            int serverPort = Integer.parseInt(args[1]);

            /** After Login variables */
            String locationName = "";
            String multicastIP = null;
            int multicastPort = 0;
            WaitOccurrenceThread waitOccurrenceThread = null;

            /** Connect to main serverSocket, serverSocket.Server */
            Socket mainServerConnection = null;
            try {
                mainServerConnection = new Socket(serverIP, serverPort);
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host: " + serverIP);
                System.exit(-1);
            }

            /** Register and Login into Main Server before anything else */
            try (
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
                // Input from Server
                BufferedReader from_server = new BufferedReader(new InputStreamReader(mainServerConnection.getInputStream()));
                // Output to Server
                PrintWriter to_server = new PrintWriter(mainServerConnection.getOutputStream());
            ) {
                boolean exit = false;
                String userInput;
                String serverOutput;
                System.out.print(EXIT_INFO + SERVER_RESPONSE + from_server.readLine() + CLIENT_MESSAGE);
                // register/login into main server
                while (!exit && (userInput = stdIn.readLine()) != null) {
                    // terminate communication
                    if (userInput.equals("%quit")) {
                        to_server.println(userInput);
                        System.out.println(EXIT_WARNING);
                        System.exit(0);
                    } else {
                        // send input to server
                        to_server.println(userInput);
                        // get server output
                        serverOutput = from_server.readLine();
                        if (serverOutput.equalsIgnoreCase("logged-in")) {
                            System.out.print("\nSuccessfully Logged In. Starting Services ...\n");
                            exit = true;

                        } else {
                            // print server output
                            System.out.print(SERVER_RESPONSE + serverOutput + CLIENT_MESSAGE);
                        }
                    }
                }

                /** Logged In, get server login data */
                // get multicast IP and PORT  and the Name of this location
                serverOutput = from_server.readLine();
                int sep1 = serverOutput.indexOf(":", 0), sep2 = serverOutput.indexOf(":", sep1);
                locationName = serverOutput.substring(0, sep1);
                multicastIP = serverOutput.substring(sep1, sep2);
                multicastPort = Integer.parseInt(serverOutput.substring(sep2, serverOutput.length()));
                // create thread for listening to server notifications
                waitOccurrenceThread = new WaitOccurrenceThread(multicastIP, multicastPort);
                // send extra information to server, about the waitOccurrencePort, enabling the server to notify this Middle-Client
                to_server.println(waitOccurrenceThread.getSocketPort());

            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection.");
                System.exit(-1);
            }

            /** Start listening for Server Ocurrence notifications */
            waitOccurrenceThread.start();

            /** Wait and Process client connections */
            try (
                ServerSocket serverSocket = new ServerSocket()
            ) {
                System.out.println("\nListening on Port " + serverPort + " for Citizen Clients to connect.\n");
                Socket serverConnection;
                while (true) {
                    serverConnection = serverSocket.accept();
                    new MiddleClientCommunicationThread(locationName, serverConnection, mainServerConnection, multicastIP, multicastPort).start();
                }
            } catch (IOException e) {
                System.err.println("Could not listen with ServerSocket!");
            }

            waitOccurrenceThread.interrupt(); // close thread
            mainServerConnection.close(); // close socket
        }
    }
}