package middle_client.utils;

import java.sql.Timestamp;

public class EventModel {

    private String eventName, id, locationName, description;
    private int severity, notifiedcount;
    private long initime;

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
        this.notifiedcount = 0;
        setId();
        setInitime();
    }

    
    private void setId() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.id = eventName+timestamp.toString();
    }

    public String getId() {
        return id;
    }

    private void setInitime() {
        initime = System.nanoTime();
    }

    public long getInitime() {
        return initime;
    }

    public int getSeverity() {
        return severity;
    }

    public String getEventName() {
        return eventName;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getDescription() {
        return description;
    }

    public int getNotifiedCount() {
        return notifiedcount;
    }

    public void addNotifiedCount(int notifiedcount) {
        this.notifiedcount += notifiedcount;
    }
}
