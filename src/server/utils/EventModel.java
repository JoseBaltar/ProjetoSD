package server.utils;

public class EventModel {

    private String eventName, locationName, description;
    private int severity;

    public EventModel(String locationName, int severity, String description) {
        switch (severity) {
            case 1:
                this.eventName = "ForestFire";
                break;
            case 2: 
                this.eventName = "Earthquake";
            default:
                this.eventName = "NuclearAccident";
        }
        this.locationName = locationName;
        this.severity = severity;
        this.description = description;
    }

    public int getSeverity() {
        return severity;
    }

    public String getLocationName() {
        return locationName;
    }
    
    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return description;
    }
}
