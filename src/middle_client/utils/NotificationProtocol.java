package middle_client.utils;

/**
 * TODO
 */
public class NotificationProtocol {
    private static enum MainStates {
        DUMMY, LOGOUT
    };

    private static enum SecStates {
        NOT_DEFINED
    };

    private MainStates main_state = MainStates.DUMMY;
    private SecStates sec_state = SecStates.NOT_DEFINED;

    private boolean dummy = false;

    public String processInput(String theInput) {
        String theOutput = null;

        // Check special inputs
        if (theInput.equals("%logout")) {
            main_state = MainStates.LOGOUT;
            sec_state = SecStates.NOT_DEFINED;
        }

        if (dummy) {

            

        } else if (main_state == MainStates.LOGOUT) {
            if (sec_state == SecStates.NOT_DEFINED) {
                
            }
        } else {
            // more ...
        }
        return theOutput;
    }
}