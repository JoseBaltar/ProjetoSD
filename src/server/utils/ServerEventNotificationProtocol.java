package server.utils;

/**
 * TODO
 */
public class ServerEventNotificationProtocol {
    private static enum MainStates {
        DUMMY
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
        if (theInput.equals("%dummy")) {
            main_state = MainStates.DUMMY;
            sec_state = SecStates.NOT_DEFINED;
        }

        if (dummy) {

        } else if (main_state == MainStates.DUMMY) {

        } else {
            // more ...
        }
        return theOutput;
    }
}