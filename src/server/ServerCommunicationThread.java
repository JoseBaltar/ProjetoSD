package server;

import java.io.*;
import java.net.Socket;

import server.utils.ConnectionsTracking;
import server.utils.MiddleClientLoginProtocol;
import server.utils.UserTracking;

/**
 * Disponibiliza um meio de comunicação com o Cliente Intermédio através de um Protocolo.
 * 
 * Lança a thread "ReceiveReportsThread" no despoletar de um evento para a receção de relatórios (socket UDP).
 */
public class ServerCommunicationThread extends Thread {

    private Socket clientConnection;

    private ConnectionsTracking connectionsTracking;
    private MiddleClientLoginProtocol login_protocol;

    ServerCommunicationThread(Socket clientConnection, UserTracking userTracking, ConnectionsTracking connectionsTracking, String path) {
        super();
        this.clientConnection = clientConnection;
        this.connectionsTracking = connectionsTracking;
        this.login_protocol = new MiddleClientLoginProtocol(connectionsTracking, userTracking, path);
    }

    @Override
    public void run() {
        try (
        // Input from Client
        BufferedReader in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
        // Output to Client
        PrintWriter out = new PrintWriter(clientConnection.getOutputStream(), true);
        ) {

            boolean quit = false; // quit communication (client input)
            String clientInp, processed; // store input from user and output from protocol, respectively
            String[] locations; // store locations for sending warnings (format: "%s,%s", ip, port)
            String clientUsername; // store clientUsername from login and logout notifications
            String event, ip; int port; // store details about event notification received

            out.println(login_protocol.processInput(""));
            while (!quit && (clientInp = in.readLine()) != null) {
                /** Register and Login Middle-Client */

                if (clientInp.equalsIgnoreCase("%quit")) {
                    // Check if client exited during login
                    System.out.println("Client canceled the login. Terminating connection ...");
                    quit = true;

                } else {
                    processed = login_protocol.processInput(clientInp);

                    if (processed.equalsIgnoreCase("logged-in")) {
                        /** Middle-Client Logged-In */
                        out.println(processed);
                        // send locationName, multicast IP and Port to middle-client
                        // out.println(login_protocol.getLocationName() + ":230.0.0.1/6000"); // for testing
                        out.println(login_protocol.getLocationName() + ":" + login_protocol.getMulticastAddress());
                        // get extra information from middle-client to enable sending occurence notifications
                        login_protocol.processInput(in.readLine()); // GET_CLIENT_LISTENING_PORT main_state no protocolo

                        while ((clientInp = in.readLine()) != null) {
                            /** Start processing End-Client requests, redirected by Middle-Client */

                            if (clientInp.startsWith("%login")) {
                                clientUsername = clientInp.substring(clientInp.indexOf(":") + 1);
                                /** When a client logs in into this Location, save it */
                                System.out.println("\n\nTEST: receive login notification from Middle-Client, check. Username: " + clientUsername);

                            } else if (clientInp.startsWith("%logout")) {
                                clientUsername = clientInp.substring(clientInp.indexOf(":") + 1);
                                /** When a client logs out from this Location, update it */
                                System.out.println("\n\nTEST: receive logout notification from Middle-Client, check. Username: " + clientUsername);

                            } else {
                                /** Process notification sent by client: location:location;danger-degree;description */
                                processed = processEvent(clientInp);
                                System.out.println("\n\nTEST: receive event notification from Middle-Client, check. Notification: " + clientInp);

                                if (processed.startsWith("create-event")) {
                                    /** Execute server actions on a new event */
                                    // notify every location specified - open a ReceiveReportsThread for every one of them
                                    event = processed.substring(processed.indexOf("?") + 1, processed.indexOf(";")); // get event details
                                    locations = processed.substring(processed.indexOf(";") + 1).split(":"); // get locations
                                    if (!event.equals("3")) {
                                        System.out.println("\n==========\nSending Event Notification to all mentioned Locations ...");
                                    } else {
                                        System.out.println("\n==========\nBroadcasting Event Notification Nationaly ...");
                                    }

                                    // method "processEvent" already verifies locations given the danger-degree
                                    for (String location : locations) {
                                        // start the UDP socket connection thread for receiving reports
                                        ReceiveReportsThread thread = new ReceiveReportsThread();
                                        thread.start();
                                        // notify client
                                        ip = location.substring(0, location.indexOf(","));
                                        port = Integer.parseInt(location.substring(location.indexOf(",") + 1));
                                        sendOcurrenceWarning(event, ip, port, thread.getLocalPort()); // check if location received notification
                                        System.out.println("> Location IP: " + ip + "; PORT: " + port);
                                    }
                                }
                            }
                        } /** client input waiting cicle */

                    } else {
                        out.println(processed);
                    }
                }
            } /** client login cicle */
        
        } catch (IOException e) {
            System.out.println("Client disconnected! Terminating ... ");
        }
    }

    /**
     * Processes the Event Notification sent by a Middle-Client Location,
     * with the given parameter string: location:location;danger-degree;description
     * 
     * After the prefix, this method returns a string with parameters:
     *  - event details: the danger degree (1 - 3), 
     *      separated from location list by ";"
     *  - a list of locations, separated by ":"
     *      - the IP and respective listening port separated by ","
     *  - ex: prefix?eventdetails;ip,port:ip,port:
     * 
     * @param eventNotification client input with event notification details
     * @param returns the described string plus one of two prefixes, "in-progress" or "create-event",
     * separated from the rest of the string by "?".
     */
    private String processEvent(String eventNotification) {
        // check se ja existem conexoes de eventos para cada localização, e se a localização existe
        // connectionsTracking.getActiveEventsIterator();
        connectionsTracking.getMulticastAddresses();
        return "";
    }

    /**
     * Sends a DatagramPacket with the event information to the given address to start listening for
     * reports.
     * 
     * @param event event details
     * @param ip the ip address to send the details
     * @param port the port relative to the address
     * @param serverListeningPort server port who will be listening for Packets sent by the given address
     */
    private void sendOcurrenceWarning(String event, String ip, int port, int serverListeningPort) {
        // UDP DatagramSocket para enviar o evento para o Middle-Client
        // arranjar melhor maneira de verificar se a localização recebeu a notificação
    }
}