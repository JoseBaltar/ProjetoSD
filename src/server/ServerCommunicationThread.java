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
 * 
 * TODO acabar o código para notificação das localizações presentes num novo evento (protocol_response = "create-event")
 */
public class ServerCommunicationThread extends Thread {

    private Socket clientConnection;

    private ClientConnectionTracking shared;
    private MiddleClientLoginProtocol login_protocol;
    private ServerEventNotificationProtocol notification_protocol;

    ServerCommunicationThread(Socket clientConnection, ClientConnectionTracking shared) {
        super();
        this.clientConnection = clientConnection;
        this.shared = shared;
        this.login_protocol = new MiddleClientLoginProtocol(shared);
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
            // PROCESS INPUT USING PROTOCOL

            /** Register and Login Client */
            boolean done = false, exit = false;
            String clientInp;
            String processed;
            out.println(login_protocol.processInput(""));
            while (!done && (clientInp = in.readLine()) != null) {
                if (clientInp.equalsIgnoreCase("%exit")) {
                    /** Check client input for exiting */
                    exit = done = true;
                } else {
                    /** Process client input through the protocol */
                    if ((processed = login_protocol.processInput(clientInp)).equalsIgnoreCase("logged-in")) {
                        out.println(processed);
                        // send locationName, multicast IP and Port to middle-client+
                        out.println(login_protocol.getLocationName() + ":" + login_protocol.getMulticastIP() + ":" + login_protocol.getMulticastPort());
                        done = true;
                    } else {
                        out.println(processed);
                    }
                }
            }

            // Check if client exited while logging in and terminate the connection
            if (!exit) {
                /** Process Redirected Client Notification Requests */
                String[] locations;
                while ((clientInp = in.readLine()) != null) {
                    // process notification sent by client: locations, ocurrence-degree, description
                    processed = notification_protocol.processInput(clientInp);
                    // send notification response to client: invalid-data, in-progress or create-event
                    out.println(processed);

                    // execute server actions on a new event
                    if (processed.startsWith("create-event")) {
                        /** 
                        * NOTIFY EVERY LOCATION SPECIFIED
                        * 
                        * OPEN A ReceiveReportsThread FOR EVERY ONE OF THEM
                        * 
                        * protocol send a list of ip and ports of the locations
                        * (locations separated by ":" and ip and port by ",")
                        */
                        locations = processed.split(":");
                        for (int i = 0; i < locations.length;) {
                            // start the UDP socket connection thread for receiving reports
                            ReceiveReportsThread thread = new ReceiveReportsThread(shared);
                            thread.start();
                        }
                    }
                }
            }
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendOcurrenceWarning(String ip, int port) {

    }
}