package middle_client.models;

import java.sql.Timestamp;

public class EventModel {

    private String eventName, id, description;
    private int severity, notifiedcount;
    private long initime;

    public EventModel(int severity, String description) {
        switch (severity) {
            case 1:
                this.eventName = "ForestFire";
                break;
            case 2: 
                this.eventName = "Earthquake";
                break;
            default:
                this.eventName = "NuclearAccident";
        }
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
        initime = System.currentTimeMillis();
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
