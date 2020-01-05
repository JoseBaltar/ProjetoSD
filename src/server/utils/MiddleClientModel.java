package server.utils;

import java.util.ArrayList;
import java.util.Iterator;

public class MiddleClientModel {

    private String locationName, multicastAddress;
    private ArrayList<String> currentlyLoggedUsers;
    private ArrayList<String> registeredUsers;

    public MiddleClientModel(String locationName, String multicastAddress, ArrayList<String> registeredUsers) {
        this.locationName = locationName;
        this.multicastAddress = multicastAddress;
        this.registeredUsers = registeredUsers;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getMulticastAddress() {
        return multicastAddress;
    }

    public boolean addLoggedUser(String username) {
        if (currentlyLoggedUsers.contains(username) || !registeredUsers.contains(username)) {
            return false;
        }
        return currentlyLoggedUsers.add(username);
    }

    public boolean removeLoggedUser(String username) {
        return currentlyLoggedUsers.remove(username);
    }

    public Iterator<String> getLoggedUsersIterator() {
        return currentlyLoggedUsers.iterator();
    }

    public boolean addRegisteredUser(String username) {
        if (registeredUsers.contains(username)) {
            return false;
        }
        return registeredUsers.add(username);
    }

    public Iterator<String> getRegisteredUsersIterator() {
        return registeredUsers.iterator();
    }
}
