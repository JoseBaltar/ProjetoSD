package middle_client;

import java.io.*;
import java.net.*;

import middle_client.utils.ClientLoginProtocol;
import middle_client.utils.EventNotificationProtocol;

/**
 * Registo e Login <br/>
 * Redireciona as notificações dos clientes para o servidor <br/>
 * Abre threads relativas a um evento decorrente.
 */
public class MiddleClientCommunicationThread extends Thread {

    private Socket clientConnection;
    private Socket serverConnection;

    private String multicastIPAdress;
    private int multicastPort;
    
    private ClientLoginProtocol login_protocol;
    private EventNotificationProtocol notification_protocol;

    MiddleClientCommunicationThread(String locationName, Socket serverConnection, Socket clientConnection, 
                        String multicastIPAddress, int multicastPort) {
        super();
        this.clientConnection = clientConnection;
        this.serverConnection = serverConnection;

        this.multicastIPAdress = multicastIPAddress;
        this.multicastPort = multicastPort;

        this.login_protocol = new ClientLoginProtocol(locationName);
        this.notification_protocol = new EventNotificationProtocol();
    }

    public void run() {
        try (
        // Input from Client
        BufferedReader in_cli = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
        // Output to Client
        PrintWriter out_cli = new PrintWriter(clientConnection.getOutputStream());
        // Input from Server
        BufferedReader in_srv = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
        // Output to Server
        PrintWriter out_srv = new PrintWriter(serverConnection.getOutputStream());
        ) {
            // PROCESS INPUT USING PROTOCOL

            /** Register and Login Client */
            boolean quit = false;
            String clientInp;
            String processed;
            String[] response;
            out_cli.println(login_protocol.processInput(""));
            while (!quit && (clientInp = in_cli.readLine()) != null) {
                if (clientInp.equalsIgnoreCase("%quit")) {
                    /** Check if client exited during login */
                    System.out.println("Client canceled the login. Terminating connection ...");
                    quit = true;
                } else {
                    /** Process client input through the protocol */
                    processed = login_protocol.processInput(clientInp);
                    if (processed.startsWith("findUserLogin")) {

                        /** When username and password exist, check for other login from same user */
                        // request server for login check
                        out_srv.println("%" + processed); // returns the location name if true
                        // send server response to protocol
                        processed = login_protocol.processInput(in_srv.readLine()); 

                        if (processed.equalsIgnoreCase("logged-in")) {
                            out_cli.println(processed);
                            // send extra information to client, about the multicast connection
                            out_cli.println(multicastIPAdress + ":" + multicastPort);
                            
                            /** Client Logged-In, start receiving event notifications */
                            while (!quit && (clientInp = in_cli.readLine()) != null) {

                                if (clientInp.equalsIgnoreCase("%logout")) {
                                    /** Check if client logged out. Stop communication with this server */
                                    // Notify client of successfull logout
                                    out_cli.println("Successfully Logged Out ...");
                                    quit = true;
                                } else {

                                    /** Redirect client input into main server */
                                    out_srv.println(clientInp);
            
                                    // NOTA: esta parte poderia estar a ser feita pelo protocolo. Só não tenho a certeza disso ainda.
                                    // processed = notification_protocol.processInput(clientInp);
                                    // Mas parece pouca coisa para criar um protocolo só para isto. 
                                    // Pode-me estar a falhar alguma coisa no entanto.
                                    // TODO resolver esta situação
            
                                    /** Process server output 
                                     * - a string representing an array of strings separated by ","
                                     * with all response details
                                     */
                                    response = in_srv.readLine().split(",");
            
                                    if (response[0].equals("invalid-data")) {
                                        /** Invalid data - [1] data details */
                                        // notify client about the data
                                        out_cli.println("Invalid Notification Data! Details: " + response[1]);
            
                                    } else if (response[0].equals("in-progress")) {
                                        /** Notification relates to an already in progress event
                                         * [1] socket port */
            
                                        // reenviar a notificação?
                                        // avisar o cliente que o evento ja foi notificado?
                                        // falta decidir ...
                                        out_cli.println("Request sent and processed successfully!");
            
                                    } else {
                                        /**
                                         * New Event, nothing else is needed because thread "WaitOccurrenceThread" handles that.
                                         * Though, something has to be returned to the client still
                                         */
                                        out_cli.println("Request sent and processed successfully!");
                                
                                    }
                                }

                            } /** notification processing cicle */

                        } else {
                            out_cli.println(processed);
                        }
                    } else {
                        out_cli.println(processed);
                    }
                }

            } /** client login cicle */
            
            System.out.println("-------------\nService terminated on Client " 
                    + clientConnection.getInetAddress() + "!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}