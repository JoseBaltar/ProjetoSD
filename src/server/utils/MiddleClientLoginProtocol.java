package server.utils;

/**
 * TODO fazer o registo e login para ficheiros, utilizar o shared object "UserTracking"
 */
public class MiddleClientLoginProtocol {
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

        /**
         * TODO no final da validação para o login, enviar uma resposta com "getport"
         * , para adicionar a porta do socket em "waitOccurrenceThread" 
         * (ver o codigo do login em "MiddleClientCommunicationThread")
         * 
         */

        if (dummy) {

        } else if (main_state == MainStates.DUMMY) {

        } else {
            // more ...
        }
        return theOutput;
    }
}