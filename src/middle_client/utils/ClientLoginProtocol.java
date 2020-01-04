package middle_client.utils;

import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;

/**
 * TODO fazer o registo e login para ficheiros, utilizar o shared object "UserTracking" (anotaçoes TODO nos locais proprios)
 */
public class ClientLoginProtocol {
    private static enum MainStates {
        CHECK_LOGIN /** handle login communication with client */, 
        REGISTER_CLIENT/** handle registration communication with client */, LOGGED
    };

    private static enum SecStates {
        NOT_DEFINED, GET_USERNAME /** request Client a username */, GET_PASSWORD /** request Client the password */, 
        RETYPE_PASSWORD /** registration password confirmation */, 
        GET_SERVER_DATA /** request Server if the Client is already logged in (other locations) */
    };

    private MainStates main_state = MainStates.CHECK_LOGIN;
    private SecStates sec_state = SecStates.NOT_DEFINED;

    private String location; /** Location name of the Server using an instance of this Protocol */
    private String username = ""; /** Client username input */
    private String password = ""; /** Client password input */
    private String registeredUsername; /** Username of a registered Client - used for verification */
    private boolean dummy = false;
    private UserTracking userTracking;

    public ClientLoginProtocol(String location, UserTracking userTracking) {
        this.location = location;
        this.userTracking = userTracking;
    }

    public String processInput(String theInput) {
        String theOutput = null;
        String tempusername = "";
        // Check special inputs
        if (theInput.equals("%register")) {
            main_state = MainStates.REGISTER_CLIENT;
            sec_state = SecStates.NOT_DEFINED;
        } else if (theInput.equals("%cancel")) {
            main_state = MainStates.CHECK_LOGIN;
            sec_state = SecStates.NOT_DEFINED;
        }

        if (main_state == MainStates.CHECK_LOGIN) {
            if (sec_state == SecStates.GET_USERNAME) {
                if (theInput.isEmpty()) {
                    theOutput = "Invalid Username! Enter your username.";
                } else if (userTracking.checkUserClear(theInput)) {
                    theOutput = "Client is not registered, want to register? Type %register, or enter a new username.";
                } else {
                    // User exists
                    username = theInput;
                    theOutput = "Enter your password.";
                    sec_state = SecStates.GET_PASSWORD;
                }
            } else if (sec_state == SecStates.GET_PASSWORD) {
                if (!userTracking.checkPassword(theInput, tempusername)) {
                    theOutput = "Password is incorrect. Retype password or write %cancel to change username.";
                } else {
                    /** Login User */
                    userTracking.loginUser(tempusername);

                    theOutput = "logged-in"; // key-word for enabling loging in communication thread
                    main_state = MainStates.LOGGED;

                    /* Removido uma vez que InputStream do Main-Server já não existe. Explicação no relatório.
                    theOutput = "findUserLogin:" + username;
                    sec_state = SecStates.GET_SERVER_DATA;
                    */
                }
            /* } else if (sec_state == SecStates.GET_SERVER_DATA) {
                if (theInput.startsWith("true")) {
                    /** User has a login in another location /
                    theOutput = "You are already logged-in in " + theInput.substring(theInput.indexOf(":"))
                                    + ". Please logout or try another account.";
                    sec_state = SecStates.GET_USERNAME;
                } else {

                    // Login User to Data Structure

                    theOutput = "logged-in"; // key-word for sinalizing login in communication thread
                    main_state = MainStates.LOGGED;
                } 
            */
            } else {
                theOutput = "Greetings from " + location + "! Enter your Username. (To register a user type %register!)";
                sec_state = SecStates.GET_USERNAME;
            }

        } else if (main_state == MainStates.REGISTER_CLIENT) {
            if (sec_state == SecStates.GET_USERNAME) {
                if (theInput.length() < 3 || theInput.length() > 30) {
                    theOutput = "Username must be within 3 and 30 characters. Enter a Username.";
                } else if (/* theInput exists in registered users */dummy) {
                    theOutput = "Client already exists. Write a new username or %cancel to go back to Login!";
                } else {
                    // User is new
                    registeredUsername = theInput;
                    theOutput = "Enter password.";
                    sec_state = SecStates.GET_PASSWORD;
                }
            } else if (sec_state == SecStates.GET_PASSWORD) {
                if (theInput.length() < 4 || theInput.length() > 16) {
                    theOutput = "Password must be within 4 and 16 characters. Retype password or write %cancel to change username.";
                } else {
                    /*
                    Pode-se aproveitar para enviar uma mensagem a avisar o User se a password é fraca, etc. aqui.
                    */
                    theOutput = "Nice password! Please, retype it for verification.";
                    password = theInput; // Store password to check with the retype
                    sec_state = SecStates.RETYPE_PASSWORD;
                }
            } else if (sec_state == SecStates.RETYPE_PASSWORD) {
                if (!password.equals(theInput)) {
                    theOutput = "Passwords do not match, rewrite password or type %register to restart registration or %cancel to go back to Login page!";
                    sec_state = SecStates.GET_PASSWORD;
                    password = "";
                } else {
                    /** Register Client */
                    registerUserJson();
                    theOutput = "User " + registeredUsername + ", registered successfully! Back to Login. Enter a Username.";

                    main_state = MainStates.CHECK_LOGIN;
                    sec_state = SecStates.GET_USERNAME;
                }
            } else {
                theOutput = "Welcome to the Registration Page! Enter a Username. (To go back to Login page type %cancel!)";
                sec_state = SecStates.GET_USERNAME;
            }

        } else {
            // main_state == MainStates.LOGGED
            theOutput = "Client Logged-In successfully to " + location + "!";
        }
        return theOutput;
    }

    public String getLoginUsername() {
        return username;
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

    /**
     * @return a JsonElement if it can read, null if can not read
     *
     * It creates a JsonElement to be parsed into a JsonArray in order to be accessible for further use
     *
     */
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

    /**
     * @return False if it fails writing to the file, true if succeeds
     *
     * This method rewrites the json file by adding a new entry to it
     * It creats a JSON Object with several Properties associated, to which it will add to a JSON array with the old entries, which will then be written
     */
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
        userTracking.addRegisteredUser(utilizador);
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