package server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * TODO finish register and login on this class
 */
public class UserTracking {

    private ArrayList<MiddleClientModel> registeredClients; // ficheiro
    private ArrayList<String> loggedUsers;

    UserTracking() {
        registeredClients = new ArrayList<>();
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

    public synchronized boolean addRegisteredMC(JsonObject client) {
        MiddleClientModel mcm = new MiddleClientModel(client.get("middleclientip").getAsString(), client.get("multicastip").getAsString(),
                client.get("password").getAsString(), client.get("serverport").getAsInt(), client.get("multicastport").getAsInt(), client.get("waitingport").getAsInt());
        return registeredClients.add(mcm);
    }

    //percorrer para ler o ficheiro
    public synchronized void setRegisteredUsers(JsonElement file){
        JsonArray clients
                = (file != null && file.isJsonArray()
                ? file.getAsJsonArray() : new JsonArray());
        if (clients != null) {
            int len = clients.size();
            for (int i=0;i<len;i++){
                JsonObject client = clients.get(i).getAsJsonObject();
                MiddleClientModel mcm = new MiddleClientModel(client.get("middleclientip").getAsString(), client.get("multicastip").getAsString(),
                        client.get("password").getAsString(), client.get("serverport").getAsInt(), client.get("multicastport").getAsInt(), client.get("waitingport").getAsInt());
                registeredClients.add(mcm);
            }
        }

    }

}