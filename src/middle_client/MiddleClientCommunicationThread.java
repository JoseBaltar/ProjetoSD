package middle_client;

import java.io.*;
import java.net.*;

import middle_client.utils.ClientLoginProtocol;
import middle_client.utils.NotificationProtocol;
import middle_client.utils.UserTracking;
import middle_client.utils.EventTracking;

/**
 * Registo e Login <br/>
 * Redireciona as notificações dos clientes para o servidor <br/>
 * Abre threads relativas a um evento decorrente.
 */
public class MiddleClientCommunicationThread extends Thread {
    private String DISPLAY = "\nMiddle-Client: ";
    private static final String SEP = "\n----------\n";
    private static final String LOGIN_CANCEL = "Client canceled the login. Terminating connection ...";
    private static final String LOGIN = "Client Logged In!";
    private static final String PROCESSING = "Processing and Redirecting End-Client event notifications ...";
    private static final String LOGOUT = "Successfully Logged Out. Terminating connection ...";

    private Socket clientConnection;
    private PrintWriter outMainServer;

    private String multicastIPAddress;
    private int multicastPort;

    // private EventTracking eventTracking;
    
    private ClientLoginProtocol login_protocol;
    private NotificationProtocol notification_protocol;

    /**
     * @param locationName Location name relative to this Middle-Client
     * @param clientConnection Client/Middle-Client socket connection
     * @param mainServerConnection Middle-Client/Server socket connection, the same for every thread 
     * @param multicastIPAddress This Location, Middle-Client, respective multicastIP address
     * @param multicastPort This Location, Middle-Client, respective multicast address PORT
     */
    MiddleClientCommunicationThread(String locationName, Socket clientConnection, PrintWriter outMainServer, 
                        String multicastIPAddress, int multicastPort, EventTracking eventTracking, UserTracking userTracking) {
        super();
        this.clientConnection = clientConnection;
        this.outMainServer = outMainServer;
        this.DISPLAY = "Middle-Client | Client" + clientConnection.getInetAddress() + ":" + clientConnection.getLocalPort() + ": ";

        this.multicastIPAddress = multicastIPAddress;
        this.multicastPort = multicastPort;

        // this.eventTracking = eventTracking;

        this.login_protocol = new ClientLoginProtocol(locationName, userTracking);
        this.notification_protocol = new NotificationProtocol();
    }

    public void run() {
        try (
        // Input from Client
        BufferedReader in_cli = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
        // Output to Client
        PrintWriter out_cli = new PrintWriter(clientConnection.getOutputStream(), true);
        ) {
            /** Register and Login End-Client */
            boolean quit = false;
            String clientInp, processed;

            out_cli.println(login_protocol.processInput("")); // send first message to Client
            while (!quit && (clientInp = in_cli.readLine()) != null) {
                if (clientInp.equalsIgnoreCase("%quit")) {
                    /** Check if client exited during login */
                    System.out.println(DISPLAY + LOGIN_CANCEL);
                    quit = true;
                } else {
                    /** Process client input through the protocol */
                    processed = login_protocol.processInput(clientInp);

                    if (processed.equalsIgnoreCase("logged-in")) {
                        out_cli.println(processed);
                        System.out.println(SEP + DISPLAY + LOGIN);
                        out_cli.println(LOGIN);
                        // send extra information to client, about the multicast connection
                        out_cli.println(multicastIPAddress + "/" + multicastPort);
                        // notify server of client login on this location
                        outMainServer.println("%login:" + login_protocol.getLoginUsername());
                        
                        /** Client Logged-In, start receiving event notifications */
                        System.out.println(DISPLAY + PROCESSING);
                        out_cli.println(notification_protocol.processInput(""));
                        while (!quit && (clientInp = in_cli.readLine()) != null) {

                            if (clientInp.equalsIgnoreCase("%logout")) {
                                /** Check if client logged out. Stop communication with this server */
                                // notify server of client logout on this location
                                outMainServer.println("%logout:" + login_protocol.getLoginUsername());
                                // notify client of successfull logout
                                out_cli.println(LOGOUT);
                                System.out.println(DISPLAY + LOGOUT + SEP);
                                quit = true;

                            } else {
                                processed = notification_protocol.processInput("");
                                
                                if (processed.equalsIgnoreCase("processed")) {
                                    // redirect event notification into main server
                                    outMainServer.println(notification_protocol.processInput(""));
                                    // notify client about sent notification
                                    out_cli.println(processed);
                                    // restart notification sending communication
                                    out_cli.println(notification_protocol.processInput(""));
                                } else {
                                    out_cli.println(processed);
                                }
                            }

                        } /** notification processing cicle */

                    } else {
                        out_cli.println(processed);
                    }
                    
                }

            } /** client login cicle */
            
            System.out.println("\n==========\nService terminated on\n\tClient: " + login_protocol.getLoginUsername() 
                            + "\n\tIP: " + clientConnection.getInetAddress() + "!\n==========\n"
                            + "Continue to listen for End-Client requests ...\n==========");
        } catch (IOException e) {
            System.err.print(SEP + "Server connection is down! Terminating ..." + SEP);
        }
    }

    /*
    /** Process server output through Protocol /
    processed = notification_protocol.processInput(in_srv.readLine());

    // ... check processed, or not
    out_cli.println(processed);

    // NOTA: esta parte poderia estar a ser feita pelo protocolo. Só não tenho a certeza disso ainda.
    // processed = notification_protocol.processInput(clientInp);
    // Mas parece pouca coisa para criar um protocolo só para isto. 
    // Pode-me estar a falhar alguma coisa no entanto.

    /** Process server output 
     * - a string representing an array of strings separated by ","
     * with all response details
     /
    response = in_srv.readLine().split(",");

    if (response[0].equals("invalid-data")) {
        /** Invalid data - [1] data details /
        // notify client about the data
        out_cli.println("Invalid Notification Data! Details: " + response[1]);

    } else if (response[0].equals("in-progress")) {
        /** Notification relates to an already in progress event
         * [1] socket port /

        // reenviar a notificação?
        // avisar o cliente que o evento ja foi notificado?
        // falta decidir ...
        out_cli.println("Request sent and processed successfully!");

    } else {
        /**
         * New Event, nothing else is needed because thread "WaitOccurrenceThread" handles that.
         * Though, something has to be returned to the client still
         /
        out_cli.println("Request sent and processed successfully!");

    }
    */
    /*
    if (processed.startsWith("findUserLogin")) {
        /** When username and password exist, check for other login from same user /

        // request server for login check
        outMainServer.println("%" + processed); // returns the location name if true
        // send server response to protocol
        processed = login_protocol.processInput(in_srv.readLine()); 

        if (processed.equalsIgnoreCase("logged-in")) {
            out_cli.println(processed);
            // send extra information to client, about the multicast connection
            out_cli.println(multicastIPAddress + ":" + multicastPort);
            // notify server of client login on this location
            outMainServer.println("%login:" + login_protocol.getLoginUsername());
            
            /** Client Logged-In, start receiving event notifications /
            out_cli.println(notification_protocol.processInput(""));
            while (!quit && (clientInp = in_cli.readLine()) != null) {

                if (clientInp.equalsIgnoreCase("%logout")) {
                    /** Check if client logged out. Stop communication with this server /
                    // notify server of client logout on this location
                    outMainServer.println("%logout:" + login_protocol.getLoginUsername());
                    // notify client of successfull logout
                    out_cli.println("Successfully Logged Out ...");
                    quit = true;
                } else {

                    /** Redirect client input into main server /
                    outMainServer.println(clientInp);

                    /** Process server output through Protocol /
                    processed = notification_protocol.processInput(in_srv.readLine());

                    // ... check processed, or not
                    out_cli.println(processed);

                    // NOTA: esta parte poderia estar a ser feita pelo protocolo. Só não tenho a certeza disso ainda.
                    // processed = notification_protocol.processInput(clientInp);
                    // Mas parece pouca coisa para criar um protocolo só para isto. 
                    // Pode-me estar a falhar alguma coisa no entanto.
                    // TODO resolver esta situação

                    /** Process server output 
                     * - a string representing an array of strings separated by ","
                     * with all response details
                     /
                    response = in_srv.readLine().split(",");

                    if (response[0].equals("invalid-data")) {
                        /** Invalid data - [1] data details /
                        // notify client about the data
                        out_cli.println("Invalid Notification Data! Details: " + response[1]);

                    } else if (response[0].equals("in-progress")) {
                        /** Notification relates to an already in progress event
                         * [1] socket port /

                        // reenviar a notificação?
                        // avisar o cliente que o evento ja foi notificado?
                        // falta decidir ...
                        out_cli.println("Request sent and processed successfully!");

                    } else {
                        /**
                         * New Event, nothing else is needed because thread "WaitOccurrenceThread" handles that.
                         * Though, something has to be returned to the client still
                         /
                        out_cli.println("Request sent and processed successfully!");
                
                    }
                }

            } /** notification processing cicle /
        
        } else {
            out_cli.println(processed);
        }
    }
*/
}