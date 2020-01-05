package server.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;

public class UserTracking {

    private ArrayList<RegisterClientModel> registeredClients; // ficheiro
    private ArrayList<String> loggedLocations;
    private ArrayList<String> loggedUsers;
    private ArrayList<String> registeredUsers;

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

    /**
     * @param location name of location to be logged
     * @return true if it logged, false if it doesn't login (due to there already being a logged location with the same name)
     */
    public synchronized boolean loginLocation(String location){
        for(int ix=0; ix < loggedLocations.size(); ix++)
            if(loggedLocations.get(ix).equals(location))
                return false;

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
        RegisterClientModel mcm = new RegisterClientModel(client.get("locationName").getAsString(), client.get("multicastip").getAsString(),
                client.get("password").getAsString(), registeredUsers);
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
                RegisterClientModel mcm = new RegisterClientModel(client.get("locationName").getAsString(), client.get("multicastAddress").getAsString(),
                        client.get("password").getAsString(), registeredUsers);
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

    public Iterator<RegisterClientModel> getAllMiddleClients(){
        return registeredClients.iterator();
    }
}