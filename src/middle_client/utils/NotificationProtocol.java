package middle_client.utils;

import java.util.ArrayList;
import java.util.Iterator;

public class NotificationProtocol {

    private static enum MainStates {
        CREATE_NOTIFICATION, SEND_NOTIFICATION
    };

    private static enum SecStates {
        NOT_DEFINED, GET_LOCATIONS, GET_SEVERITY, GET_DESCRIPTION
    };

    private MainStates main_state = MainStates.CREATE_NOTIFICATION;
    private SecStates sec_state = SecStates.NOT_DEFINED;

    private ArrayList<String> locations;
    private String description;
    private int severity;

    public NotificationProtocol() {
        locations = new ArrayList<>();
    }

    public String processInput(String theInput) {
        String theOutput = null;

        // Check special inputs
        if (theInput.equals("%done")) {
            sec_state = SecStates.GET_DESCRIPTION;
        } else if (theInput.equals("%restart")) {
            main_state = MainStates.CREATE_NOTIFICATION;
            sec_state = SecStates.NOT_DEFINED;
        }

        if (main_state == MainStates.CREATE_NOTIFICATION) {
            
            if (sec_state == SecStates.GET_SEVERITY) {

                if (theInput.isEmpty()) {
                    theOutput = "Invalid Event type! Please repeat.";

                } else if (theInput.equalsIgnoreCase("ForestFire") || theInput.equalsIgnoreCase("Earthquake") || theInput.equalsIgnoreCase("NuclearAccident")) {
                    switch (theInput) {
                        case "ForestFire":
                            severity = 1;
                            break;
                        case "Earthquake": 
                            severity = 2;
                            break;
                        default:
                            severity = 3;
                    }

                    if (severity == 3) {
                        sec_state = SecStates.GET_DESCRIPTION;
                        theOutput = "Event type added! The Event is going to be broadcasted at National level! Enter the Event description.";
                    } else {
                        sec_state = SecStates.GET_LOCATIONS;
                        theOutput = "Event type added! In which locations is the Event happening? Input location names.";
                    }

                } else {
                    theOutput = "Invalid severity! Please enter one of the following - ForestFire, Earthquake or NuclearAccident.";
                }

            } else if(sec_state == SecStates.GET_LOCATIONS) {

                if (theInput.isEmpty()) {
                    theOutput = "Invalid location! Enter a new location.";

                } else if (!theInput.matches("[A-Z][a-z]+([ -][A-Z][a-z]+)*")) {
                    theOutput = "Invalid location name, please try again. (Only first letter capital, words separated by ' ' or '-')";

                } else {
                    locations.add(theInput);
                    theOutput = "Location added successfuly! Enter another or type %done to go to next input.";
                    sec_state = SecStates.GET_DESCRIPTION;
                }

            } else if (sec_state == SecStates.GET_DESCRIPTION){

                if (theInput.equalsIgnoreCase("%done")) {
                    theOutput = "Enter the Event description (max 300 digits).";

                } else if (theInput.isEmpty()) {
                    theOutput = "Invalid Description! Enter new Description or %restart to start over.";

                } else if (theInput.length() > 300) {
                    theOutput = "Description can only have at max 300 digits! Enter new Description or %restart to start over.";

                } else {
                    description = theInput;
                    theOutput = "processed";
                    main_state = MainStates.SEND_NOTIFICATION;
                }

            } else {
                theOutput = "Ready to start sending Notifications! You will be asked three different inputs: "
                        + "Event Type, Locations, and a Description. Type %restart to restart inputs whenever you want!"
                        + "Enter the Event type - one of the following: ForestFire, Earthquake or NuclearAccident";
                sec_state = SecStates.GET_SEVERITY;
            }

        } else if (main_state == MainStates.SEND_NOTIFICATION) {

            theOutput = "";
            Iterator<String> it = locations.iterator();
            while (it.hasNext())
                if (theOutput.isEmpty())
                    theOutput = theOutput + it.next();
                else
                    theOutput = theOutput + ":" + it.next();
            theOutput = theOutput + ";" + severity + ";" + description;
            locations.clear();
            main_state = MainStates.CREATE_NOTIFICATION;
            sec_state = SecStates.NOT_DEFINED;

        } else {    
            // more ...
        }
        return theOutput;
    }
}