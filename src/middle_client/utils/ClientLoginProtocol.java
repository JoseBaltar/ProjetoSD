package middle_client.utils;

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
    private boolean dummy = false;

    public ClientLoginProtocol(String location) {
        this.location = location;
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
            if (sec_state == SecStates.GET_USERNAME) {
                if (!theInput.isEmpty()) {
                    theOutput = "Invalid Username! Enter your username.";
                } else if (/* theInput does not exist in registered users */dummy) {
                    theOutput = "Client is not registered, want to register? Type %register, or enter a new username.";
                } else {
                    // User exists
                    username = theInput;
                    theOutput = "Enter your password.";
                    sec_state = SecStates.GET_PASSWORD;
                }
            } else if (sec_state == SecStates.GET_PASSWORD) {
                if (/* password incorrect */dummy) {
                    theOutput = "Password is incorrect. Retype password or write %cancel to change username.";
                } else {
                    theOutput = "findUserLogin:" + username;
                    sec_state = SecStates.GET_SERVER_DATA;
                }
            } else if (sec_state == SecStates.GET_SERVER_DATA) {
                if (theInput.startsWith("true")) {
                    /** User has a login in another location */
                    theOutput = "You are already logged-in in " + theInput.substring(theInput.indexOf(":"))
                                    + ". Please logout or try another account.";
                    sec_state = SecStates.GET_USERNAME;
                } else {

                    /**
                     * TODO Login User (adicionar user a um objeto partilhado entre threads, com os clientes de login efetuado)
                     */

                    theOutput = "logged-in"; // key-word for sinalizing login in communication thread
                    main_state = MainStates.LOGGED;
                }
            } else {
                theOutput = "Greetings from " + location + "! Enter your Username. (To register a user type %register!)";
                sec_state = SecStates.GET_USERNAME;
            }

        } else if (main_state == MainStates.REGISTER_CLIENT) {
            if (sec_state == SecStates.GET_USERNAME) {
                if (theInput.length() < 3 && theInput.length() > 30) {
                    theOutput = "Username must be within 3 and 30 characters. Enter a Username.";
                } else if (/* theInput exists in registered users */dummy) {
                    theOutput = "Client already exists. Write a new username or %cancel to go back to Login!";
                } else {
                    // User is new
                    theOutput = "Enter password.";
                    sec_state = SecStates.GET_PASSWORD;
                }
            } else if (sec_state == SecStates.GET_PASSWORD) {
                if (theInput.length() < 4 && theInput.length() > 16) {
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
                    theOutput = "Passwords do not match, restart registration. Enter a Username. (Type %cancel to go back to Login page!)";
                    sec_state = SecStates.GET_USERNAME;
                    password = "";
                } else {

                    /** 
                     * TODO Register Client (adicionar user à base de dados de ficheiros)
                     */

                    main_state = MainStates.CHECK_LOGIN;
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
}