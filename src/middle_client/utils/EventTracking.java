package middle_client.utils;

import java.util.ArrayList;
import java.util.Iterator;

public class EventTracking {

    private ArrayList<EventModel> currentEvents;

    public EventTracking() {
        currentEvents = new ArrayList<>();
    }

    /**
     * Adds an occurring event to the current ative event list of this Location.
     * 
     * @param eventRelatedThread event instance that represents a currently active event
     * @return true if added, false otherwise
     */
    public synchronized boolean addActiveEvent(EventModel event) {
        currentEvents.add(event);
        return true;
    }

    /**
     * Adds an occurring event to the current ative event list of this Location.
     * 
     * @param eventd event instance that represents a currently active event
     * @return true if added, false otherwise
     */
    public synchronized boolean removeActiveEvent(EventModel event) {
        currentEvents.remove(event);
        return true;
    }

    /**
     * Gets an Iterator over the current active events on this location.
     * 
     * @return Iterator instace for the current active events list
     */
    public Iterator<EventModel> getCurrentEvents() {
        return currentEvents.iterator();
    }
}