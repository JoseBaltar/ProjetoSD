package middle_client.utils;

/**
 * TODO
 */
public class EventNotificationProtocol {
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

            /*
            ifs para adicionar aqui estao abaixo, retirado do codigo da Thread "MiddleClientCommunicationThread"

            if (clientInp.equalsIgnoreCase("%exit")) {
                /** Check client input for exiting /
                done = true;
            } else if (clientInp.equalsIgnoreCase("%logout")) {
                /** Check client input for logout, stop communication with this server /
                // Notify client of successfull logout
                out_cli.println("Successfully Logged Out ...");
                done = true;
            } else {
                /** Redirect client input into main server /
                out_srv.println(clientInp);

                // NOTA: esta parte poderia estar a ser feita pelo protocolo. Só não tenho a certeza disso ainda.
                // processed = notification_protocol.processInput(clientInp);
                // Mas parece pouca coisa para criar um protocolo só para isto. 
                // Pode-me estar a falhar alguma coisa no entanto.

                /** Process server output 
                 * - a string representing an array of strings separated by ","
                 * with all response details
                 /
                response = in_srv.readLine().split(",");

                if (response[0].equals("invalid-data")) {
                    /** Invalid data - [1] data details /
                    // notify client about the data
                    out_cli.println("Invalid Notification Data! Details: " + response[1]);

                } else if (response[0].equals("in-progress")) {
                    /** Notification relates to an already in progress event
                     * [1] socket port /

                    // reenviar a notificação?
                    // avisar o cliente que o evento ja foi notificado?
                    // falta decidir ...
                    out_cli.println("Request sent and processed successfully!");

                } else {
                    /**
                     * New Event, nothing else is needed, 
                     * though something has to be returned to the client still
                     /
                    out_cli.println("Request sent and processed successfully!");
            
                }
            } */

        } else if (main_state == MainStates.LOGOUT) {

        } else {
            // more ...
        }
        return theOutput;
    }
}