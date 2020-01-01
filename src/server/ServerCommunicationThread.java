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
            // PROCESS INPUT USING PROTOCOL

            /** Register and Login Client */
            boolean quit = false;
            String clientInp, processed;

            String[] locations;
            String event, ip; int port;

            out.println(login_protocol.processInput(""));
            while (!quit && (clientInp = in.readLine()) != null) {
                if (clientInp.equalsIgnoreCase("%quit")) {
                    /** Check if client exited during login */
                    System.out.println("Client canceled the login. Terminating connection ...");
                    quit = true;
                } else {
                    /** Process client input through the protocol */
                    if ((processed = login_protocol.processInput(clientInp)).equalsIgnoreCase("logged-in")) {
                        /** Middle-Client Logged-In */
                        out.println(processed);
                        // send locationName, multicast IP and Port to middle-client+
                        out.println(login_protocol.getLocationName() + ":" + login_protocol.getMulticastIP() + ":" + login_protocol.getMulticastPort());
                        // get extra information from client, about listening socket port, to enable sending occurence notifications
                        login_protocol.processInput(in.readLine()); // GET_CLIENT_LISTENING_PORT main_state no protocolo

                        /** Start processing redirected Client notification requests */
                        while ((clientInp = in.readLine()) != null) {
                            // process notification sent by client: locations, ocurrence-degree, description
                            processed = notification_protocol.processInput(clientInp);

                            // execute server actions on a new event
                            if (processed.startsWith("create-event")) {
                                /** Notify every location specified - open a ReceiveReportsThread for every one of them */
                                /*
                                INFORMACAO PARA O PROTOCOLO
                                O protocolo retorna os detalhes do evento, o IP e a listening port de cada localização
                                detalhes separado por ";", 
                                localizações por ":" começando pelo final do anterior,
                                ip e porta por ","
                                */
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

                            // send notification response to client: invalid-data, in-progress or create-event
                            out.println(processed);
                        }

                    } else {
                        out.println(processed);
                    }
                }
            }
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendOcurrenceWarning(String event, String ip, int port, int serverListeningPort) {
        // UDP DatagramSocket para enviar o evento para o Middle-Client
        // arranjar melhor maneira de verificar se a localização recebeu a notificação
    }
}