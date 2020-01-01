package client;

import java.io.*;
import java.net.*;

/**
 * Abrir duas threads. <br/>
 * - TCP, que comunica com o Middle-Client através de um protocolo<br/>
 * - UDP, fica à espera de receber notificações (aberto depois do registo e
 *out no Middle-Client)<br/>
 */
public class Citizen {
    private static final String EXIT_INFO = "-------------\nTo close the connection write '%quit'.\n-------------\n";
    private static final String LOGOUT_INFO = "-------------\nTo logout from this server write '%logout'.\n-------------\n";
    private static final String EXIT_WARNING = "\n-----\nConnection terminated!\n-----\n";
    private static final String ASK_SERVER_INFO = "-------------\nCheck the Server connections list to get IP and Port Information.\n-------------\n";
    private static final String ASK_SERVER_IP = "Enter Location Server IP:\n> ";
    private static final String ASK_SERVER_PORT = "Enter Location Server Port:\n> ";
    private static final String CLIENT_MESSAGE = "\nClient: ";
    private static final String SERVER_RESPONSE = "Server: ";

    public static void main(String[] args) throws IOException {

        /** Connect to middle_client.CivilProtection server and start communications */
        String serverIP; // = args[0];
        int serverPort; // = Integer.parseInt(args[1]);

        String userInput;
        String serverOutput;

        boolean quit = false;
        int separator;

        String multicastIP;
        int multicastPort;
        Thread notificationThread = null;
        try (
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {

            /** Input which Location Server to connect */
            while (true) {
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

                    /** Register and Login into Middle-Client Server */
                    quit = false;
                    System.out.print(EXIT_INFO + SERVER_RESPONSE + from_server.readLine() + CLIENT_MESSAGE);
                    while (!quit && (userInput = stdIn.readLine()) != null) {
                        // terminate communication
                        if (userInput.equals("%quit")) {
                            to_server.println(userInput);
                            System.out.println(EXIT_WARNING);
                            quit = true;
                        } else {
                            // send input to server
                            to_server.println(userInput);
                            if ((serverOutput = from_server.readLine()).equalsIgnoreCase("logged-in")) {

                                /** Start communication between Logged Client and Middle-Client Server */
                                System.out.print("\nSuccessfully Logged In. Starting Notification Services ...\n");
                                
                                // Open thread for processing occurrence warnings
                                serverOutput = from_server.readLine();
                                separator = serverOutput.indexOf(":", 0);
                                multicastIP = serverOutput.substring(0, separator);
                                multicastPort = Integer.parseInt(serverOutput.substring(separator, serverOutput.length()));
                                notificationThread = new ReceiveNotificationThread(multicastIP, multicastPort);
                                notificationThread.start();

                                // Continue communication with the server. Send notifications.
                                System.out.print(LOGOUT_INFO + CLIENT_MESSAGE);
                                while (!quit && (userInput = stdIn.readLine()) != null) {
                                    // terminate communication
                                    if (userInput.equals("%logout")) {
                                        to_server.println(userInput);
                                        System.out.println(from_server.readLine() + EXIT_WARNING);
                                        quit = true;

                                    } else {
                                        // send input to server
                                        to_server.println(userInput);
                                        // print server response
                                        System.out.print(SERVER_RESPONSE + from_server.readLine() + CLIENT_MESSAGE);
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
                    System.exit(-1);
                } catch (IOException e) {
                    System.err.println("Couldn't get I/O for the connection.");
                    System.exit(-1);
                } finally {
                    if (notificationThread != null) notificationThread.interrupt();
                }

            } /** change location cicle */

        } catch (IOException e) {
            System.err.println("Error reading from System.in!");
        } catch (NumberFormatException e) {
            System.err.println("Server Port must be a valid Integer!");
        }
    }
}