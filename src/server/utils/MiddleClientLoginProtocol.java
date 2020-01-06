package server.utils;

import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;

public class MiddleClientLoginProtocol {
    private static enum MainStates {
        CHECK_LOGIN /** handle login communication with client */, 
        REGISTER_CLIENT/** handle registration communication with client */, LOGGED
    };

    private static enum SecStates {
        NOT_DEFINED, GET_NAME /** request Client a locationName */, GET_PASSWORD /** request Client the password */, 
        RETYPE_PASSWORD /** registration password confirmation */, 
        GET_CLIENT_DATA /** wait for Client response for it's occurrence listening address */
    };

    private MainStates main_state = MainStates.CHECK_LOGIN;
    private SecStates sec_state = SecStates.NOT_DEFINED;
    private String JSON_FILE_PATH;

    private String registeredPassword; /** Location password for verification */
    private String registeredName; /** Location name of a registered Client - used for verification */
    private String registeredMulticastAddress; /** Registered Client respective multicastAddress, generated in shared object - used for verification */

    private String locationName; /** Location name of the Client that's logging in */
    private String multicastAddress; /** Client respective multicastAddress - IP and Port */
    private UserTracking userTracking; /** Shared Object that manages client users */
    private ConnectionsTracking connectionsTracking; /** Shared Object that manages active multicast addresses */

    public MiddleClientLoginProtocol(ConnectionsTracking connectionsTracking, UserTracking userTracking, String path) {
        this.connectionsTracking = connectionsTracking;
        this.userTracking = userTracking;
        this.JSON_FILE_PATH = path;
    }

    public String processInput(String theInput) {
        String theOutput = null;

        // Check special inputs
        if (theInput.equals("%register")) {
            main_state = MainStates.REGISTER_CLIENT;
            sec_state = SecStates.NOT_DEFINED;
        } else if (theInput.equals("%cancel")) {
            main_state = MainStates.CHECK_LOGIN;
            sec_state = SecStates.NOT_DEFINED;
        }

        if (main_state == MainStates.CHECK_LOGIN) {

            if (sec_state == SecStates.GET_NAME) {

                if (theInput.isEmpty()) {
                    theOutput = "Invalid Location! Enter a Location name. (To register a new Location type %register!)";

                } else if (userTracking.checkUserClear(theInput)) {
                    theOutput = "Location is not registered, want to register? Type %register, or enter a new name.";

                } else if (userTracking.isMiddleClientLogged(theInput)) {
                    theOutput = "Location is already Logged In! Enter a Location name. (To register a new Location type %register!)";
                    
                } else {
                    // User exists
                    locationName = theInput;
                    theOutput = "Enter the password: ";
                    sec_state = SecStates.GET_PASSWORD;
                }

            } else if (sec_state == SecStates.GET_PASSWORD) {

                if (!userTracking.checkPassword(theInput, locationName)) {
                    theOutput = "Password is incorrect. Retype password or write %cancel to change Location name.";

                } else {
                    /** Logged In, save multicastAddress and wait for client response */
                    theOutput = "logged-in"; // key-word for sinalizing login in communication thread
                    sec_state = SecStates.GET_CLIENT_DATA;
                }

            } else if (sec_state == SecStates.GET_CLIENT_DATA) {

                if (userTracking.loginMiddleClient(locationName)) {
                    userTracking.getLoggedMiddleClient(locationName).locationAddress = theInput;
                    this.multicastAddress = theOutput = userTracking.getLoggedMiddleClient(locationName).getMulticastAddress();
                    main_state = MainStates.LOGGED;
                } else
                    theOutput = "ERROR while logging in! Enter a Location name. (To register a new Location type %register!)";

            } else {
                theOutput = "Greetings from the Main Server! Enter your Location name: (To register a new Location type %register!)";
                sec_state = SecStates.GET_NAME;
            }

        } else if (main_state == MainStates.REGISTER_CLIENT) {

            if (sec_state == SecStates.GET_NAME) {

                if (!theInput.matches("[A-Z][a-z]+([ -][A-Z][a-z]+)*")) {
                    theOutput = "Invalid location name, please try again. (Only first letter capital, words separated by ' ' or '-')";

                } else if (theInput.length() < 3 || theInput.length() > 50) {
                    theOutput = "Location name must be within 3 and 50 characters. Enter a new name.";

                } else if (userTracking.isMiddleClientRegistered(theInput)) {
                    theOutput = "Location already exists. Write a new name or %cancel to go back to Login!";

                } else {
                    // User is new
                    registeredName = theInput;
                    theOutput = "Enter Location password.";
                    sec_state = SecStates.GET_PASSWORD;
                }

            } else if (sec_state == SecStates.GET_PASSWORD) {

                if (theInput.length() < 4 || theInput.length() > 16) {
                    theOutput = "Password must be within 4 and 16 characters. Retype password or write %cancel to change locationName.";

                } else {
                    /*
                    Pode-se aproveitar para enviar uma mensagem a avisar o User se a password é fraca, etc. aqui.
                    */
                    theOutput = "Nice password! Please, retype it for verification.";
                    registeredPassword = theInput; // Store password to check with the retype
                    sec_state = SecStates.RETYPE_PASSWORD;
                }
                
            } else if (sec_state == SecStates.RETYPE_PASSWORD) {

                if (!registeredPassword.equals(theInput)) {
                    theOutput = "Passwords do not match, rewrite password or type %register to restart registration or %cancel to go back to Login page!";
                    sec_state = SecStates.GET_PASSWORD;
                    registeredPassword = "";
                } else {
                    // check if a new multicast address can be generated
                    if ((registeredMulticastAddress = connectionsTracking.generateMulticastAddress()) != null) {

                        if (registerMiddleClient()) {
                            /** Register Client */
                            theOutput = "Location " + registeredName + ", registered successfully! Back to Login. Enter a Location Name.";
                            main_state = MainStates.CHECK_LOGIN;
                        } else {
                            theOutput = "Location is already Registered! Enter a Location name. (To go back to Login page type %cancel!)";
                        }
                        sec_state = SecStates.GET_NAME;

                    } else {
                        theOutput = "WARNING! Multicast Address limit reached! Couln't conclude Registration successfully. Back to Login. Enter a Location Name.";
                        main_state = MainStates.CHECK_LOGIN;
                        sec_state = SecStates.GET_NAME;
                    }
                }

            } else {
                theOutput = "Welcome to the Registration Page! Enter a Location name. (To go back to Login page type %cancel!)";
                sec_state = SecStates.GET_NAME;
            }

        } else {
            // main_state == MainStates.LOGGED
            theOutput = "Client Logged-In successfully into Main Server!";
        }
        return theOutput;
    }

    public String getLocationName() {
        return this.locationName;
    }

    public String getMulticastAddress() {
        return this.multicastAddress;
    }
    
    private boolean registerMiddleClient() {
        RegisterClientModel client = 
            new RegisterClientModel(registeredPassword, registeredName, registeredMulticastAddress, new ArrayList<>());

        if (userTracking.addRegisteredMiddleClient(client)) {
            Gson gson = new Gson(); // Instância gson para escrever o ficheiro Json
            File pathf = new File(JSON_FILE_PATH); // Ficheiro de destino
            JsonElement file = loadFromJSONFile(JSON_FILE_PATH);
            JsonArray locations
                    = (file != null && file.isJsonArray()
                    ? file.getAsJsonArray() : new JsonArray());

            JsonObject location = new JsonObject();
            location.addProperty("password", registeredPassword);
            location.addProperty("locationName", registeredName);
            location.addProperty("multicastAddress", registeredMulticastAddress);
            location.add("registeredUsers", new JsonArray());

            locations.add(location);

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
        return false;
    }

    /**
     * Read a JSON file if JsonElement equals a JsonArray
     * 
     * @param file_path file path
     * @return JsonElement instance representing the file
     */
    private JsonElement loadFromJSONFile(String file_path) {
        JsonElement json; // JsonElement correspondente ao ficheiro
        try
        { // Leitura do ficheiro e parse para uma instância de JsonElement
            FileReader inputFile = new FileReader(file_path);

            JsonParser parser = new JsonParser();
            json = parser.parse(inputFile);

        } catch (FileNotFoundException ex)
        { // Retorna null se o ficheiro não existir
            return null;
        }

        if (json.isJsonArray() && json.getAsJsonArray().size() == 0)
        {
            return null;
        }

        return json;
    }

}