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
    private ClientConnectionTracking shared;

    private String locationName, multicastIP;
    private int multicastPort;

    public MiddleClientLoginProtocol(ClientConnectionTracking shared) {
        this.shared = shared;
    }

    public String processInput(String theInput) {
        String theOutput = null;

        // Check special inputs
        if (theInput.equals("%dummy")) {
            main_state = MainStates.DUMMY;
            sec_state = SecStates.NOT_DEFINED;
        }

        /**
         * TODO guardar nome da localização, multicast ip e porta durante o processar do protocolo
         * NOTA: o ip e porta do multicast vao ser gerados automaticamente aqui ou no ClientConnectionTracking.
         *      Quando forem gerados devem ser guardados em variaveis neste protocolo para possibilitar os metodos "get"
         */

        if (dummy) {

        } else if (main_state == MainStates.DUMMY) {

        } else {
            // more ...
        }
        return theOutput;
    }

    public String getLocationName() {
        return this.locationName;
    }

    public String getMulticastIP() {
        return this.multicastIP;
    }

    public int getMulticastPort() {
        return this.multicastPort;
    }
}