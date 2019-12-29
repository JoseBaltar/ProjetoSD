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
    private static final String EXIT_INFO = "-------------\nTo close the connection to server write 'exit'.\n-------------\n";
    private static final String EXIT_WARNING = "\n-----\nConnection with Server terminated!\n-----\n";
    private static final String CLIENT_MESSAGE = "\nClient: ";
    private static final String SERVER_RESPONSE = "Server: ";

    public static void main(String[] args) throws IOException {
        /** Check arguments */
        if (args.length != 5) {
            System.out.println("Wrong Arguments!\n\nUsage:\n\t" 
                + "java middle_client.CivilProtection <location_name> <main_server_ip> <main_server_port> <multicast_ip_address> <multicast_port>\nor\n\t");
        } else if (!args[2].matches("\\d+") && !args[4].matches("\\d+")) {
            System.out.println("Wrong Arguments! Make sure <serverPort> is a valid integer.\n\nUsage:\n\t" 
                + "java middle_client.CivilProtection <location_name> <main_server_ip> <main_server_port> <multicast_ip_address> <multicast_port>");
        } else {

            /** Argument variables */
            String locationName = args[0];
            String serverIP = args[1];
            int serverPort = Integer.parseInt(args[2]);
            String multicastIP = args[3];
            int multicastPort = Integer.parseInt(args[4]);

            /** Connect to main serverSocket, serverSocket.Server and other verifications */
            Socket mainServerConnection = null;
            try {
                mainServerConnection = new Socket(serverIP, serverPort);
                if (!InetAddress.getByName(multicastIP).isMulticastAddress()){
                    System.err.println("Invalid multicast address: " + multicastIP);
                    System.exit(-1);
                }
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host: " + serverIP);
                System.exit(-1);
            }

            /** Create Thread for listening to server notifications */
            WaitOccurrenceThread waitOccurrenceThread = new WaitOccurrenceThread(multicastIP, multicastPort);
            int waitOccurrencePort = waitOccurrenceThread.getSocketPort();

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
                System.out.print(CivilProtection.EXIT_INFO + CivilProtection.SERVER_RESPONSE 
                                    + from_server.readLine() + CivilProtection.CLIENT_MESSAGE);
                while (!exit && (userInput = stdIn.readLine()) != null) {
                    // terminate communication
                    if (userInput.equals("exit")) {
                        System.out.println(CivilProtection.EXIT_WARNING);
                        System.exit(0);
                    } else {
                        // send input to server
                        to_server.println(userInput);
                        // get server output
                        serverOutput = from_server.readLine();
                        if (serverOutput.equalsIgnoreCase("getport")) {
                            // send the waitOccurrencePort to enable the server to notify to this Middle-Client
                            to_server.println(waitOccurrencePort);

                        } else if (serverOutput.equalsIgnoreCase("loggedin")) {
                            System.out.print("\nSuccessfully Logged In. Starting Services ...\n");
                            exit = true;

                        } else {
                            // print server output
                            System.out.print(CivilProtection.SERVER_RESPONSE + serverOutput + CivilProtection.CLIENT_MESSAGE);
                        }
                    }
                }
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
                    new MiddleClientCommunicationThread(locationName, serverConnection, mainServerConnection, multicastIP, multicastPort, waitOccurrencePort).start();
                }
            } catch (IOException e) {
                System.err.println("Could not listen with ServerSocket!");
            }

            waitOccurrenceThread.interrupt(); // close thread
            mainServerConnection.close(); // close socket
        }
    }
}