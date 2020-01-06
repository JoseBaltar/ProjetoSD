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
    private static final String REGISTER = "Client Registered!";
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
     * @param outMainServer
     * @param multicastIPAddress This Location, Middle-Client, respective multicastIP address
     * @param multicastPort This Location, Middle-Client, respective multicast address PORT
     */
    MiddleClientCommunicationThread(String locationName, Socket clientConnection, PrintWriter outMainServer, 
                        String multicastIPAddress, int multicastPort, EventTracking eventTracking, UserTracking userTracking, String path) {
        super();
        this.clientConnection = clientConnection;
        this.outMainServer = outMainServer;
        this.DISPLAY = "\nMiddle-Client | Client:" + clientConnection.getInetAddress() + ":" + clientConnection.getLocalPort() + ": ";

        this.multicastIPAddress = multicastIPAddress;
        this.multicastPort = multicastPort;

        // this.eventTracking = eventTracking;

        this.login_protocol = new ClientLoginProtocol(locationName, userTracking, path);
        this.notification_protocol = new NotificationProtocol();
    }

    public void run() {
        try (
        // Input from Client
        BufferedReader in_cli = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
        // Output to Client
        PrintWriter out_cli = new PrintWriter(clientConnection.getOutputStream(), true);
        ) {
            boolean quit = false;
            String clientInp, processed; // store input from user and output from protocol, respectively

            out_cli.println(login_protocol.processInput("")); // send first message to Client
            while (!quit && (clientInp = in_cli.readLine()) != null) {
                /** Register and Login End-Client */

                if (clientInp.equalsIgnoreCase("%quit")) {
                    // Check if client exited during login
                    System.out.println(DISPLAY + LOGIN_CANCEL);
                    quit = true;

                } else {
                    processed = login_protocol.processInput(clientInp);

                    if (processed.startsWith("Registered")) {
                        /** Client Registered */
                        out_cli.println(processed);
                        System.out.println(DISPLAY + REGISTER);
                        // notify server of client login on this location
                        outMainServer.println("%register:" + login_protocol.getLastRegisteredUsername());

                    } else if (processed.equalsIgnoreCase("logged-in")) {
                        /** Client Logged In */
                        out_cli.println(processed);
                        System.out.println(DISPLAY + LOGIN);
                        out_cli.println(LOGIN);
                        // send extra information to client, about the multicast connection
                        out_cli.println(multicastIPAddress + "/" + multicastPort);
                        // notify server of client login on this location
                        outMainServer.println("%login:" + login_protocol.getLoginUsername());
                        
                        System.out.println(DISPLAY + PROCESSING);
                        out_cli.println(notification_protocol.processInput(""));
                        while (!quit && (clientInp = in_cli.readLine()) != null) {
                            /** Client - Middle_Client. Start receiving event notifications */

                            if (clientInp.equalsIgnoreCase("%logout")) {
                                /** Client Logged Out. Finish communication Thread */
                                // notify server of client logout on this location
                                outMainServer.println("%logout:" + login_protocol.getLoginUsername());
                                System.out.println(DISPLAY + LOGOUT + SEP);
                                out_cli.println(LOGOUT);
                                quit = true;

                            } else {
                                processed = notification_protocol.processInput(clientInp);
                                
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
            
            System.out.println("==========\nService terminated on\n\tClient: " + login_protocol.getLoginUsername() 
                            + "\n\tIP: " + clientConnection.getInetAddress() + "!\n==========\n"
                            + "Continue to listen for End-Client requests ...\n==========");
        } catch (IOException e) {
            System.err.print(SEP + "Server connection is down! Terminating ..." + SEP);
        }
    }
}