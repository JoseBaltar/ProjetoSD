package server.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;

public class UserTracking {

    private ArrayList<MiddleClientModel> registeredClients; // ficheiro
    private ArrayList<String> loggedLocations;
    private ArrayList<String> loggedUsers;

    public UserTracking() {
        registeredClients = new ArrayList<>();
        loggedUsers = new ArrayList<>();
        loggedLocations = new ArrayList<>();
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

    public synchronized boolean loginLocation(String location){ 
        return loggedLocations.add(location);
    }

    public synchronized boolean logoutLocation(String location){
        return loggedLocations.remove(location);
    }

    public Iterator<String> getLoggedLocations(){
        return loggedLocations.iterator();
    }

    //it does the same as the Middle_Client version of this class
    public synchronized boolean addRegisteredMC(JsonObject client) {
        MiddleClientModel mcm = new MiddleClientModel(client.get("middleclientip").getAsString(), client.get("multicastip").getAsString(),
                client.get("password").getAsString(), client.get("locationName").getAsString(), client.get("serverport").getAsInt(), client.get("multicastport").getAsInt(), client.get("waitingport").getAsInt());
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
                        client.get("password").getAsString(), client.get("locationName").getAsString(), client.get("serverport").getAsInt(), client.get("multicastport").getAsInt(), client.get("waitingport").getAsInt());
                registeredClients.add(mcm);
            }
        }

    }

    public synchronized boolean checkPassword(String password, String location){
        for(int ix=0; ix<=registeredClients.size(); ix++) {
            if (location.equals(registeredClients.get(ix).getLocationName()))
                return password.equals(registeredClients.get(ix).getPassword());
        }
        return false;
    }

    public synchronized boolean checkUserClear(String username){
        for(int ix=0; ix<=registeredClients.size(); ix++){
            if(username.equals(registeredClients.get(ix).getLocationName()))
                return false;
        }
        return true;
    }

    public Iterator<MiddleClientModel> getAllMiddleClients(){
        return registeredClients.iterator();
    }
}