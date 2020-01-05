package server;

import com.google.gson.*;

import server.utils.EventModel;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;

import server.utils.ConnectionsTracking;
import server.utils.EventTracking;
import server.utils.MiddleClientLoginProtocol;
import server.utils.UserTracking;

/**
 * Disponibiliza um meio de comunicação com o Cliente Intermédio através de um Protocolo.
 * 
 * Lança a thread "ReceiveReportsThread" no despoletar de um evento para a receção de relatórios (socket UDP).
 */
public class ServerCommunicationThread extends Thread {
    private String DISPLAY = "\nMain-Server: ";
    private static final String SEP = "\n==========\n";
    private static final String LOGIN_CANCEL = "Client canceled the login. Terminating connection ...";
    private static final String PROCESSING = "Processing Middle-Client inputs ...";
    private static final String FINISH = "Client disconnected! Terminating ... ";

    private static final String LOGIN = "Process End-Client login notification from Middle-Client!";
    private static final String LOGOUT = "Process End-Client logout notification from Middle-Client!";
    private static final String REGISTER = "Process End-Client register notification from Middle-Client!";

    private Socket clientConnection;
    private String JSON_FILE_PATH;

    private UserTracking userTracking;
    private EventTracking eventTracking;

    private MiddleClientLoginProtocol login_protocol;

    ServerCommunicationThread(Socket clientConnection, UserTracking userTracking, ConnectionsTracking connectionsTracking, 
                        EventTracking eventTracking, String path) {
        super();
        this.clientConnection = clientConnection;
        this.DISPLAY = "\nMain-Server | Middle-Client:" + clientConnection.getInetAddress() + ":" + clientConnection.getPort() + ": ";

        this.userTracking = userTracking;
        this.eventTracking = eventTracking;
        this.JSON_FILE_PATH = path;

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
            String clientUsername, locationName; // store client usernames from login and logout notifications
            String event, ip; int port; // store details about event notification received

            out.println(login_protocol.processInput(""));
            while (!quit && (clientInp = in.readLine()) != null) {
                /** Register and Login Middle-Client */

                if (clientInp.equalsIgnoreCase("%quit")) {
                    // Check if client exited during login
                    System.out.println(DISPLAY + LOGIN_CANCEL);
                    quit = true;

                } else {
                    processed = login_protocol.processInput(clientInp);

                    if (processed.equalsIgnoreCase("logged-in")) {
                        /** Middle-Client Logged-In */
                        out.println(processed);
                        // send locationName, multicast IP and Port to middle-client
                        locationName = login_protocol.getLocationName();
                        out.println(locationName + ":" + login_protocol.getMulticastAddress());
                        // get extra information from middle-client to enable sending occurence notifications
                        login_protocol.processInput(in.readLine()); // GET_CLIENT_LISTENING_PORT main_state no protocolo

                        System.out.println(DISPLAY + PROCESSING);
                        while ((clientInp = in.readLine()) != null) {
                            /** Start processing End-Client requests, redirected by Middle-Client */

                            if (clientInp.startsWith("%register")) {
                                clientUsername = clientInp.substring(clientInp.indexOf(":") + 1);
                                /** When a client is registered, save it in file and Data Structure */
                                System.out.println(DISPLAY + REGISTER);
                                if (addRegisteredMiddleClientUserToFile(clientUsername, locationName)
                                    && userTracking.getLoggedMiddleClient(locationName).addRegisteredUser(clientUsername))
                                    System.out.println("Operation Successful!");
                                else
                                    System.out.println("WARNING - Couldn't add registered User!");

                            } else if (clientInp.startsWith("%login")) {
                                clientUsername = clientInp.substring(clientInp.indexOf(":") + 1);
                                /** When a client logs in into this Location Client, save it into Data Structure */
                                System.out.println(DISPLAY + LOGIN);
                                if (userTracking.getLoggedMiddleClient(locationName).addLoggedUser(clientUsername))
                                    System.out.println("Operation Successful!");
                                else
                                    System.out.println("WARNING - Couldn't add logged User!");

                            } else if (clientInp.startsWith("%logout")) {
                                clientUsername = clientInp.substring(clientInp.indexOf(":") + 1);
                                /** When a client logs out from this Location Client, update it Data Structure */
                                System.out.println(DISPLAY + LOGOUT);
                                if (userTracking.getLoggedMiddleClient(locationName).removeLoggedUser(clientUsername))
                                    System.out.println("Operation Successful!");
                                else
                                    System.out.println("WARNING - Couldn't remove logged User!");

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
                                        ip = location.substring(0, location.indexOf("/"));
                                        port = Integer.parseInt(location.substring(location.indexOf("/") + 1));
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
            System.out.println(SEP + FINISH + SEP);
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
     *  - ex: prefix?eventdetails;ip,port:ip,port
     * 
     * @param eventNotification client input with event notification details
     * @param returns the described string plus one of two prefixes, "in-progress" or "create-event",
     * separated from the rest of the string by "?".
     */
    private String processEvent(String eventNotification) {
        int sep = eventNotification.indexOf(";"), sep2 = eventNotification.indexOf(";", sep),
            degree = Integer.parseInt(eventNotification.substring(sep + 1, sep2));
        String[] locationNames = eventNotification.substring(0, sep).split(":");
        String description = eventNotification.substring(sep2 + 1);

        String prefix = "create-event", eventDetails = degree + "," + description, locations = "";
        
        for (String location : locationNames) {
            if (userTracking.isMiddleClientLogged(location)) {
                // check if location is valid

                if (!eventTracking.isEventActive(location, degree)) {
                     // check if location already has the event active (same dange-degree)
                    eventTracking.addActiveEvent(new EventModel(location, degree, description));
                    if (locations.isEmpty())
                        locations = locations + userTracking.getLoggedMiddleClient(location).locationAddress;
                    else
                        locations = locations + ":" + userTracking.getLoggedMiddleClient(location).locationAddress;
                }
            }
        }

        if (locations.isEmpty())
            prefix = "in-progress";
        return prefix + eventDetails + locations;
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
        // server has received a request from a client, construct the response
        try (
            DatagramSocket socket = new DatagramSocket()
        ){
            // construct packet
            byte[] buf = (event + ":" + serverListeningPort).getBytes();

            // send it
            InetAddress address = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("\nERROR - Couldn't send event notification to Middle-Client!");
        }
    }
    
    private boolean addRegisteredMiddleClientUserToFile(String username, String locationName) {
        Gson gson = new Gson(); // Instância gson para escrever o ficheiro Json
        File pathf = new File(JSON_FILE_PATH); // Ficheiro de destino
        JsonElement file = loadFromJSONFile(JSON_FILE_PATH);
        JsonArray locations
                = (file != null && file.isJsonArray()
                ? file.getAsJsonArray() : null);
        
        if (locations == null) return false;

        Iterator<JsonElement> it = locations.iterator();
        JsonObject location = null; JsonArray users; int i = 0;
        while (it.hasNext()) {
            location = it.next().getAsJsonObject();
            if (locationName.equals(location.get("locationName").getAsString())) {
                users = location.get("registeredUsers").getAsJsonArray();
                users.add(locationName);
                location.add("registeredUsers", users);
            } else {
                i++;
            }
        }

        if (location == null) return false;

        locations.set(i, location);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathf))) {
            writer.write(gson.toJson(locations));
            writer.flush();
        } catch (IOException ex) {
            System.err.println("[" + ex.getClass().getName() + "] "
                    + "Erro na escrita do ficheiro" );
            return false;
        }
        return true;
    }

    private JsonElement loadFromJSONFile(String file_path) {
        JsonElement json; // JsonElement correspondente ao ficheiro
        try { 
            // Leitura do ficheiro e parse para uma instância de JsonElement
            FileReader inputFile = new FileReader(file_path);
            JsonParser parser = new JsonParser();
            json = parser.parse(inputFile);
        } catch (FileNotFoundException ex) { 
            return null;
        }
        if (json.isJsonArray() && json.getAsJsonArray().size() == 0)
            return null;

        return json;
    }
}