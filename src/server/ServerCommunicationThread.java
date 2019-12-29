package server;

import java.io.*;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

import server.utils.MiddleClientLoginProtocol;
import server.utils.ServerEventNotificationProtocol;

/**
 * Disponibiliza um meio de comunicação com o Cliente Intermédio através de um Protocolo.
 * 
 * Lança a thread "ReceiveReportsThread" no despoletar de um evento para a receção de relatórios (socket UDP).
 * 
 * TODO ver o ciclo while na linha 69 junto com o protocolo do servidor
 */
public class ServerCommunicationThread extends Thread {

    private Socket clientConnection;

    private NetworkPorts shared;
    private MiddleClientLoginProtocol login_protocol;
    private ServerEventNotificationProtocol notification_protocol;

    ServerCommunicationThread(Socket clientConnection, NetworkPorts shared) {
        super();
        this.clientConnection = clientConnection;
        this.shared = shared;
        this.login_protocol = new MiddleClientLoginProtocol();
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
                    if ((processed = login_protocol.processInput(clientInp)).equalsIgnoreCase("loggedin")) {
                        out.println(processed);
                        // if nothing else is required by the Middle-Client this statement can be removed
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
                    /** Process Client Notification */
                    // O protocolo tem acesso aos eventos ja ativos
                    processed = notification_protocol.processInput(clientInp);
                    // send notification response to client: invalid-data, in-progress or create-event
                    out.println(processed);

                    /** Depending on protocol output, send adicional information to client */
                    if (processed.startsWith("invalid-data")) {
                        /** Invalid data */
                        // notify client about the data
                        out.println(""); // este passo pode ser removido em principio

                    } else if (processed.startsWith("in-progress")) {
                        /** Notification relates to an already in progress event */
                        String[] details = processed.split(",");
                        // send details to client

                    } else {
                        /** Notification relates to a new event 
                        * 
                        * NOTIFY EVERY LOCATION SPECIFIED
                        * 
                        * OPEN A ReceiveReportsThread FOR EVERY ONE OF THEM
                        * 
                        * protocol send a list of ip and ports of the locations
                        */
                        locations = processed.split(",");
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