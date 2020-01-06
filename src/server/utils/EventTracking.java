package server.utils;

import java.util.ArrayList;
import java.util.Iterator;

import server.models.EventModel;

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

    public synchronized EventModel getActiveEvent(String location, int dangerDegree) {
        Iterator<EventModel> it = currentEvents.iterator();
        while (it.hasNext()) {
            EventModel next = it.next();
            if (location.equals(next.getLocationName()) && dangerDegree == next.getSeverity())
                return next;
        }
        return null;
    }

    public boolean isEventActive(String location, int dangerDegree) {
        return (getActiveEvent(location, dangerDegree) != null ? true : false);
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
     * Gets an Iterator over the current active events on this location.
     * 
     * @param locationName name of the location to search
     * @return Iterator instace for the current active events list
     */
    public Iterator<EventModel> getAllActiveEventsByLocationIterator(String locationName) {
        Iterator<EventModel> it = currentEvents.iterator();
        EventModel event; 
        ArrayList<EventModel> list = new ArrayList<>();
        while (it.hasNext()) {
            event = it.next();
            if (locationName.equals(event.getLocationName()))
                list.add(event);
        }
        return list.iterator();
    }

    /**
     * Gets the number of active events.
     * 
     * @return current total counter of active events
     */
    public int numberOfActiveEvents() {
        return currentEvents.size();
    }

    /**
     * Gets the number of active events in a location.
     * 
     * @param locationName name of the location to search
     * @return current total counter of active events
     */
    public int numberOfActiveEventsByLocation(String locationName) {
        Iterator<EventModel> it = currentEvents.iterator();
        int count = 0;
        while (it.hasNext()) {
            if (locationName.equals(it.next().getLocationName()))
                count++;
        }
        return count;
    }
}