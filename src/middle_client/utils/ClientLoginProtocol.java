package middle_client.utils;

/**
 * TODO fazer o registo e login para ficheiros, utilizar o shared object "UserTracking"
 */
public class ClientLoginProtocol {
    private static enum MainStates {
        CHECK_LOGIN, REGISTER_CLIENT, LOGGED
    };

    private static enum SecStates {
        NOT_DEFINED, GET_USERNAME, GET_PASSWORD, RETYPE_PASSWORD
    };

    private MainStates main_state = MainStates.CHECK_LOGIN;
    private SecStates sec_state = SecStates.NOT_DEFINED;

    private String location;
    private String password = "";
    private boolean dummy = false;

    public ClientLoginProtocol(String location) {
        this.location = location;
    }

    public String processInput(String theInput) {
        String theOutput = null;

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
                } else if (/* theInput does not exist in registered users */dummy) {
                    theOutput = "Client is not registered, want to register? Type %register, or enter a new username.";
                } else {
                    // User exists
                    theOutput = "Enter your password.";
                    sec_state = SecStates.GET_PASSWORD;
                }
            } else if (sec_state == SecStates.GET_PASSWORD) {
                if (/* password incorrect */dummy) {
                    theOutput = "Password is incorrect. Retype password or write %cancel to change username.";
                } else {
                    /**
                     * TODO Login User 
                     */
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
                    main_state = MainStates.CHECK_LOGIN;
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
}