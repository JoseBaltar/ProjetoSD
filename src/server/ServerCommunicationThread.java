package server;

import java.io.*;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

import server.utils.MiddleClientLoginProtocol;
import server.utils.ServerEventNotificationProtocol;
import server.utils.ClientConnectionTracking;

/**
 * Disponibiliza um meio de comunicação com o Cliente Intermédio através de um Protocolo.
 * 
 * Lança a thread "ReceiveReportsThread" no despoletar de um evento para a receção de relatórios (socket UDP).
 */
public class ServerCommunicationThread extends Thread {

    private Socket clientConnection;

    private ClientConnectionTracking sharedConnTracking;
    private MiddleClientLoginProtocol login_protocol;
    private ServerEventNotificationProtocol notification_protocol;

    ServerCommunicationThread(Socket clientConnection, ClientConnectionTracking sharedConnTracking) {
        super();
        this.clientConnection = clientConnection;
        this.sharedConnTracking = sharedConnTracking;
        this.login_protocol = new MiddleClientLoginProtocol(sharedConnTracking);
        this.notification_protocol = new ServerEventNotificationProtocol();
    }

    @Override
    public void run() {
        try (
        // Input from Client
        BufferedReader in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
        // Output to Client
        PrintWriter out = new PrintWriter(clientConnection.getOutputStream());
        ) {

            boolean quit = false;
            String clientInp, processed;

            String[] locations;
            String event, ip; int port;

            /** Register and Login Middle-Client */
            out.println(login_protocol.processInput(""));
            while (!quit && (clientInp = in.readLine()) != null) {
                if (clientInp.equalsIgnoreCase("%quit")) {
                    // check if middle-client exited during login
                    System.out.println("Client canceled the login. Terminating connection ...");
                    quit = true;
                } else {
                    // process client input through the protocol
                    processed = login_protocol.processInput(clientInp);
                    if (processed.equalsIgnoreCase("logged-in")) {

                        /** Middle-Client Logged-In */
                        out.println(processed);
                        // send locationName, multicast IP and Port to middle-client+
                        out.println(login_protocol.getLocationName() + ":" + login_protocol.getMulticastIP() + ":" + login_protocol.getMulticastPort());
                        // get extra information from middle-client, about listening socket port, to enable sending occurence notifications
                        login_protocol.processInput(in.readLine()); // GET_CLIENT_LISTENING_PORT main_state no protocolo

                        /** Start processing Middle-Client requests, redirected from End-Client*/
                        while ((clientInp = in.readLine()) != null) {

                            if (clientInp.startsWith("%login")) {
                                /** When a client logs in into this Location, save it into shared object */

                            } else if (clientInp.startsWith("%logout")) {
                                /** When a client logs out from this Location, update shared object */

                            } else {
                                /** Process notification sent by client: locations:ocurrence-degree:description */
                                processed = processEvent(clientInp);
                                // the method return one of the following: in-progress or create-event
                                if (processed.startsWith("create-event")) {
                                    /** Execute server actions on a new event */

                                    // notify every location specified - open a ReceiveReportsThread for every one of them
                                    event = processed.substring(0, processed.indexOf(";")); // get event details
                                    locations = processed.substring(processed.indexOf(";")).split(":"); // get locations
                                    System.out.println("\n-----------\nSending Event Notification to all Locations ...");
                                    for (String location : locations) {
                                        // start the UDP socket connection thread for receiving reports
                                        ReceiveReportsThread thread = new ReceiveReportsThread(sharedConnTracking);
                                        thread.start();
                                        // notify client
                                        ip = location.substring(0, location.indexOf(","));
                                        port = Integer.parseInt(
                                            location.substring(location.indexOf(","), location.length()));
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
            e.printStackTrace();
        }
    }

    /**
     * This method returns, after the prefix:
     *  - event details: the danger degree (1 - 3), 
     *      separated from location list by ";"
     *  - a list of locations, separated by ":"
     *      - the IP and respective listening port separated by ","
     * 
     * @param eventNotification client input with event notification details
     * @param returns the described string plus one of two prefixes, "in-progress" or "create-event",
     * separated from the rest of the string by "?".
     */
    private String processEvent(String eventNotification) {
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