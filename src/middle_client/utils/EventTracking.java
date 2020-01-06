package middle_client.utils;

import java.util.ArrayList;
import java.util.Iterator;

import middle_client.models.EventModel;

public class EventTracking {

    private ArrayList<EventModel> currentEvents;

    public EventTracking() {
        currentEvents = new ArrayList<>();
    }

    /**
     * Adds an occurring event to the current ative event list.
     * 
     * @param event event instance that represents a currently active event
     * @return true if added, false otherwise
     */
    public synchronized boolean addActiveEvent(EventModel event) {
        return currentEvents.add(event);
    }

    /**
     * Adds an occurring event to the current ative event list.
     * 
     * @param event event instance that represents a currently active event
     * @return true if added, false otherwise
     */
    public synchronized boolean removeActiveEvent(EventModel event) {
        return currentEvents.remove(event);
    }

    public synchronized EventModel getActiveEventBySeverity(int dangerDegree) {
        Iterator<EventModel> it = currentEvents.iterator();
        while (it.hasNext()) {
            EventModel next = it.next();
            if (dangerDegree == next.getSeverity())
                return next;
        }
        return null;
    }

    public synchronized EventModel getActiveEvent(String id) {
        Iterator<EventModel> it = currentEvents.iterator();
        while (it.hasNext()) {
            EventModel next = it.next();
            if (id.equals(next.getId()))
                return next;
        }
        return null;
    }

    public boolean isEventActive(int dangerDegree) {
        return (getActiveEventBySeverity(dangerDegree) != null ? true : false);
    }

    /**
     * Gets an Iterator over the current active.
     * 
     * @return Iterator instace for the current active events list
     */
    public Iterator<EventModel> getAllActiveEventsIterator() {
        return currentEvents.iterator();
    }

    /**
     * Gets the number of active events.
     * 
     * @return current total counter of active events
     */
    public int numberOfActiveEvents() {
        return currentEvents.size();
    }
}