package middle_client.utils;

import com.google.gson.*;
import middle_client.UserTracking;

import java.io.*;
import java.util.ArrayList;

/**
 * TODO fazer o registo e login para ficheiros, utilizar o shared object "UserTracking"
 */
public class ClientLoginProtocol {
    private static enum MainStates {
        CHECK_LOGIN, REGISTER_CLIENT, LOGGED
    }

    ;

    private static enum SecStates {
        NOT_DEFINED, GET_USERNAME, GET_PASSWORD, REQUEST_SERVER_DATA, GET_SERVER_DATA, RETYPE_PASSWORD
    }

    ;

    private MainStates main_state = MainStates.CHECK_LOGIN;
    private SecStates sec_state = SecStates.NOT_DEFINED;

    private String username;

    private String location;
    private String password = "";
    private boolean dummy = false;
    private UserTracking tracking;

    public ClientLoginProtocol(String location, UserTracking tracking) {
        this.location = location;
        this.tracking = tracking;
    }

    public String processInput(String theInput) {
        String theOutput = null;
        String tempusername = "";
        // Check special inputs
        if (theInput.equals("%register")) {
            main_state = MainStates.REGISTER_CLIENT;
            sec_state = SecStates.GET_USERNAME;
        } else if (theInput.equals("%cancel")) {
            main_state = MainStates.CHECK_LOGIN;
            sec_state = SecStates.NOT_DEFINED;
        }

        if (main_state == MainStates.CHECK_LOGIN) {
            if (sec_state == SecStates.GET_USERNAME) {
                if (!theInput.isEmpty()) {
                    theOutput = "Invalid Username! Enter your username.";
                } else if (tracking.checkUserClear(theInput)) {
                    theOutput = "Client is not registered, want to register? Type %register, or enter a new username.";
                } else {
                    // User exists
                    tempusername = theInput;
                    theOutput = "Enter your password.";
                    sec_state = SecStates.GET_PASSWORD;
                }
            } else if (sec_state == SecStates.GET_PASSWORD) {
                if (!tracking.checkPassword(theInput, tempusername)) {
                    theOutput = "Password is incorrect. Retype password or write %cancel to change username.";
                } else {
                    /**
                     * TODO Login User
                     */
                    tracking.loginUser(tempusername);
                    theOutput = "logged-in"; // key-word for enabling login in communication thread
                    main_state = MainStates.LOGGED;
                }
            } else {
                theOutput = "Greetings from " + location + "! Enter your Username. (To register a user type %register!)";
                sec_state = SecStates.NOT_DEFINED;
            }
        } else if (main_state == MainStates.REGISTER_CLIENT) {
            if (sec_state == SecStates.GET_USERNAME) {
                if (theInput.length() < 3 && theInput.length() > 30) {
                    theOutput = "Username must be within 3 and 30 characters. Enter username.";
                } else if (tracking.checkUserClear(theInput)) {
                    theOutput = "Client already exists. Write a new username or %cancel to go back to Login!";
                } else {
                    // User is new
                    username = theInput;
                    theOutput = "Enter password.";
                    sec_state = SecStates.GET_PASSWORD;
                }
            } else if (sec_state == SecStates.GET_PASSWORD) {
                if (theInput.length() < 4 && theInput.length() > 16) {
                    theOutput = "Password must be within 4 and 16 characters. Retype password or write %cancel to change username.";
                } else {
                    // Store password to check retype
                    password = theInput;
                    sec_state = SecStates.RETYPE_PASSWORD;
                }
            } else if (sec_state == SecStates.RETYPE_PASSWORD) {
                if (!password.equals(theInput)) {
                    theOutput = "Passwords do not match. Enter your username. (Type %cancel to go back to Login page!)";
                    sec_state = SecStates.GET_USERNAME;
                    password = "";
                } else {
                    /**
                     * TODO Register Client 
                     */
                    registerUserJson();
                    main_state = MainStates.CHECK_LOGIN;
                }
            } else if (sec_state == SecStates.REQUEST_SERVER_DATA) {
                theOutput = "findUserLogin:<username>";
                sec_state = SecStates.GET_SERVER_DATA;
            } else if (sec_state == SecStates.GET_SERVER_DATA) {
                //false nao encontrado, true se encontrado
                if (theInput.equals("false")) {

                } else if (theInput.equals("true")) {

                }
            } else {
                theOutput = "Welcome to the Registration Page! Enter your username. (To go back to Login page type %cancel!)";
                sec_state = SecStates.GET_USERNAME;
            }
        } else {
            // more ...
        }
        return theOutput;
    }
    //Em ficheiro TXT,
    @Deprecated
    public String registerUser() {
        String userdata = username + ";" + password + ";" + location + "; \n";
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("userfile.txt"), "utf-8"))) {
            writer.write(userdata);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userdata;
    }

    public JsonElement loadUsersFromJSONFile() {
        JsonElement json; // JsonElement correspondente ao ficheiro
        try
        { // Leitura do ficheiro e parse para uma instância de JsonElement
            FileReader inputFile = new FileReader("utilizadores.json");

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

    public boolean registerUserJson() {

        Gson gson = new Gson(); // Instância gson para escrever o ficheiro Json
        File pathf = new File("userfile.json"); // Ficheiro de destino
        JsonElement file = this.loadUsersFromJSONFile();
        JsonArray utilizadores
                = (file != null && file.isJsonArray()
                ? file.getAsJsonArray() : new JsonArray());

        JsonObject utilizador = new JsonObject();
        utilizador.addProperty("username",username);
        utilizador.addProperty("password", password);
        utilizador.addProperty("location", location);
        tracking.addRegisteredUser(utilizador);
        utilizadores.add(utilizador);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathf))) {
            writer.write(gson.toJson(utilizadores));
            writer.flush();
        } catch (IOException ex) {
            System.err.println("[" + ex.getClass().getName() + "] "
                    + "Erro na escrita do ficheiro" );
            return false;
        }

        return true;
    }

    //Em ficheiro TXT,
    @Deprecated
    public ArrayList<String> getRegisteredUsers() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("userfile.txt"));
            String line = br.readLine();
            ArrayList<String> users = new ArrayList<>();

            while (line != null) {
                users.add(line);
                line = br.readLine();
            }
            br.close();
            return users;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}