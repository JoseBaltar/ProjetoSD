package client;

import java.io.*;
import java.net.*;

/**
 * Abrir duas threads. <br/>
 * - TCP, que comunica com o Middle-Client através de um protocolo<br/>
 * - UDP, fica à espera de receber notificações (aberto depois do registo e
 *out no Middle-Client)<br/>
 */
public class EndClient {
    private static final String SEP = "\n----------\n";
    private static final String LOGOUT_INFO = SEP + "NOTICE: To logout from this server write '%logout'." + SEP;
    private static final String EXIT_INFO = SEP + "NOTICE: To close the connection write '%quit'." + SEP;
    private static final String EXIT_WARNING = SEP + "Connection terminated!" + SEP;

    private static final String ASK_SERVER_INFO = SEP + "NOTICE: Check the Server connections list to get IP and Port Information." + SEP;
    private static final String ASK_SERVER_IP = "Enter Location Server IP:\n> ";
    private static final String ASK_SERVER_PORT = "Enter Location Server Port:\n> ";
    private static final String SENT_NOTIFICATION = SEP + "Notification sent Successfully!" + SEP;

    private static final String CLIENT_MESSAGE = "\nClient: ";
    private static final String SERVER_RESPONSE = "Server: ";

    public static void main(String[] args) throws IOException {

        String serverIP; // = args[0];
        int serverPort; // = Integer.parseInt(args[1]);

        String userInput, serverOutput; // store input from user and output from protocol, respectively

        boolean quit = false, exit = false;
        int separator;

        String multicastIP;
        int multicastPort;
        Thread notificationThread = null;

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        while (!exit) {
            try {
                /** Connect to middle_client.CivilProtection server and start communications */
                System.out.print(ASK_SERVER_INFO + ASK_SERVER_IP);
                serverIP = stdIn.readLine();
                System.out.print(ASK_SERVER_PORT);
                serverPort = Integer.parseInt(stdIn.readLine());
                try (
                    Socket serverConnection = new Socket(serverIP, serverPort); /** middle_client server connection */
                    PrintWriter to_server = 
                                new PrintWriter(serverConnection.getOutputStream(), true); /** output stream to server */
                    BufferedReader from_server = new BufferedReader(
                                new InputStreamReader(serverConnection.getInputStream())); /** input stream from server */
                ) {

                    quit = false;
                    System.out.print(EXIT_INFO + SERVER_RESPONSE + from_server.readLine() + CLIENT_MESSAGE);
                    while (!quit && (userInput = stdIn.readLine()) != null) {
                        /** Register and Login into Middle-Client Server */

                        if (userInput.equals("%quit")) {
                            to_server.println(userInput);
                            System.out.println(EXIT_WARNING);
                            quit = true;
                        } else {
                            to_server.println(userInput); // send input to server
                            serverOutput = from_server.readLine(); // get server response

                            if (serverOutput.equalsIgnoreCase("logged-in")) {
                                /** Client Logged In */
                                System.out.println(SERVER_RESPONSE + from_server.readLine());
                                
                                // Open thread for processing occurrence warnings
                                serverOutput = from_server.readLine(); // get extra information from server
                                separator = serverOutput.indexOf("/");
                                multicastIP = serverOutput.substring(0, separator);
                                multicastPort = Integer.parseInt(serverOutput.substring(separator + 1));
                                notificationThread = new ReceiveNotificationThread(multicastIP, multicastPort);
                                notificationThread.start();

                                // Continue communication with the server. Send notifications.
                                System.out.print(LOGOUT_INFO + SERVER_RESPONSE + from_server.readLine() + CLIENT_MESSAGE);
                                while (!quit && (userInput = stdIn.readLine()) != null) {
                                    /** Client - Middle_Client. Start receiving event notifications */

                                    if (userInput.equals("%logout")) {
                                        to_server.println(userInput);
                                        System.out.print(from_server.readLine() + EXIT_WARNING);
                                        quit = true;

                                    } else {
                                        to_server.println(userInput); // send input to server
                                        serverOutput = from_server.readLine(); // get server response
                                        
                                        if (serverOutput.equalsIgnoreCase("processed")) {
                                            // notification sent successfully, read new server line
                                            System.out.print(SENT_NOTIFICATION + SERVER_RESPONSE + from_server.readLine() + CLIENT_MESSAGE);
                                        } else {
                                            // print server response
                                            System.out.print(SERVER_RESPONSE + serverOutput + CLIENT_MESSAGE);
                                        }
                                    }

                                } /** notifications to server cicle */

                            } else {
                                // print server response
                                System.out.print(SERVER_RESPONSE + serverOutput + CLIENT_MESSAGE);
                            }
                        }

                    } /** location login cicle */

                } catch (UnknownHostException e) {
                    System.err.println("Don't know about host: " + serverIP);
                } catch (IOException e) {
                    System.err.println("Couldn't get I/O for the connection.");
                } finally {
                    if (notificationThread != null) notificationThread.interrupt();
                }

            } catch (IOException e) {
                System.err.println("Error reading from System.in!");
                exit = true;
            } catch (NumberFormatException e) {
                System.err.println("Server Port must be a valid Integer!");
            }

        } /** change location cicle */ 
        stdIn.close();
    }
}