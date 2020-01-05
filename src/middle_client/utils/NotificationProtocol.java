package middle_client.utils;

import jdk.jfr.Event;

/**
 * TODO
 */
public class NotificationProtocol {
    public NotificationProtocol(UserTracking userTracking, EventTracking eventTracking) {
        this.userTracking = userTracking;
        this.eventTracking = eventTracking;
    }

    private static enum MainStates {
        DUMMY, LOGOUT, SENDEVENT, NOTIFICATION_SENT
    };

    private static enum SecStates {
        NOT_DEFINED, GET_LOCATION,
        GET_SEVERITY
    };

    private MainStates main_state = MainStates.SENDEVENT;
    private SecStates sec_state = SecStates.NOT_DEFINED;

    private boolean dummy = false;
    private UserTracking userTracking;
    private EventTracking eventTracking;
    private String name;
    private int severity;

    public String processInput(String theInput) {
        String theOutput = null;



        // Check special inputs
        if (theInput.equals("%logout")) {
            //main_state = MainStates.LOGOUT;
            //  sec_state = SecStates.NOT_DEFINED;
        } else if (theInput.equals("%notify")){
            main_state = MainStates.SENDEVENT;
            sec_state = SecStates.GET_LOCATION;
        }

        if (main_state == MainStates.SENDEVENT) {
            //sec_state = SecStates.GET_LOCATION;
            if (sec_state == SecStates.GET_LOCATION) {
                if (theInput.isEmpty()) {
                    theOutput = "Invalid location! Enter the location.";

                } else if (!theInput.matches("[a-zA-Z]+")) {
                    theOutput = "Invalid location name, please try again.";

                } else {
                    name = theInput;
                    theOutput = "Enter the event severity (from 1 to 3)";
                    sec_state = SecStates.GET_SEVERITY;
                }
            }

            if(sec_state == SecStates.GET_SEVERITY){
                try {
                    if (theInput.isEmpty()) {
                        theOutput = "Invalid severity! Please enter a number between 1 and 3";
                    } else if ((Integer.parseInt(theInput)) > 3 || (Integer.parseInt(theInput)) < 1) {
                        theOutput = "Invalid severity! Please enter a number between 1 and 3";
                    } else {
                        severity = Integer.parseInt(theInput);
                        //eventTracking.addActiveEvent(setEventModel());
                        main_state = MainStates.NOTIFICATION_SENT;
                        theOutput = "processed";
                    }
                }catch(NumberFormatException ex){
                    theOutput = "Invalid severity! Please enter a number between 1 and 3";
                }
            }

        } else if (main_state == MainStates.LOGOUT) {
            if (sec_state == SecStates.NOT_DEFINED) {
                if (theInput.isEmpty()) {
                    theOutput = "Invalid username! Please try again";
                } else if (!userTracking.logoutUser(theInput)){
                    theOutput = "Invalid username! Please try again";
                } else{
                    theOutput = "Goodbye";
                }

            }
        } else {
            // more ...
        }
        return theOutput;
    }

    public EventModel setEventModel(){
        return new EventModel(name, severity);
    }


}