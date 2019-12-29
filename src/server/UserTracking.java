package server;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * TODO finish register and login on this class
 */
public class UserTracking {

    private ArrayList<String> registeredUsers; // ficheiro
    private ArrayList<String> loggedUsers;

    UserTracking() {
        registeredUsers = new ArrayList<>();
        loggedUsers = new ArrayList<>();
    }

    /**
     * Adds 
     * 
     * @param user 
     * @return 
     */
    public synchronized boolean loginUser(String user) {
        return loggedUsers.add(user);
    }

    /**
     * Adds 
     * 
     * @param user 
     * @return 
     */
    public synchronized boolean logoutUser(String user) {
        return loggedUsers.remove(user);
    }

    public Iterator<String> getLoggedUsers() {
        return loggedUsers.iterator();
    }
}