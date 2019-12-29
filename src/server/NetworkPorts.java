package server;

import java.util.ArrayList;
import java.util.Iterator;

public class NetworkPorts {

    private ArrayList<Integer> eventPeriodPorts;

    NetworkPorts() {
        eventPeriodPorts = new ArrayList<>();
    }

    /**
     * Adds a port to the list.
     * 
     * @param port port number
     * @return the added port or -1 in case the port already exists or is invalid
     */
    public synchronized int addEventPeriodPort(int port) {
        if (eventPeriodPorts.contains(port) || port < 0 && port > 65535) {
            return -1;
        }
        eventPeriodPorts.add(port);
        return port;
    }

    public synchronized Iterator<Integer> getEventPeriodPorts() {
        return eventPeriodPorts.iterator();
    }
}