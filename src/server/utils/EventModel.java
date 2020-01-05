package server.utils;

import java.sql.Timestamp;

public class EventModel {

    private String name, id;
    private int severity, notifiedcount;
    private long initime;


    public EventModel(String name, int severity) {
        this.name = name;
        this.severity = severity;
        setId();
        setInitime();
    }

    private void setId() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.id = name+timestamp.toString();
    }

    public int getSeverity() {
        return severity;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public long getInitime() {
        return initime;
    }

    private void setInitime() {
        initime = System.nanoTime();
    }

    public int getNotifiedcount() {
        return notifiedcount;
    }

    public void setNotifiedcount(int notifiedcount) {
        this.notifiedcount = notifiedcount;
    }
}
