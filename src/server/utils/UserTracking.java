package server.utils;

import com.google.gson.*;

import server.models.MiddleClientModel;
import server.models.RegisterClientModel;

import java.util.ArrayList;
import java.util.Iterator;

public class UserTracking {

    private ArrayList<RegisterClientModel> registeredClients; // ficheiro
    private ArrayList<MiddleClientModel> loggedClients;

    public UserTracking() {
        registeredClients = new ArrayList<>();
        loggedClients = new ArrayList<>();
    }

    public synchronized boolean loginMiddleClient(String locationName){
        MiddleClientModel mcm = (MiddleClientModel) this.getRegisteredMiddleClient(locationName);
        if (mcm == null)
            return false; 
        return loggedClients.add(mcm);
    }

    public synchronized boolean logoutMiddleClient(String locationName){
        MiddleClientModel mcm = this.getLoggedMiddleClient(locationName);
        if (mcm == null)
            return false;
        return loggedClients.remove(mcm);
    }

    public Iterator<MiddleClientModel> getLoggedMiddleClientsIterator(){
        return loggedClients.iterator();
    }
    
    public int getNumberLoggedClients() {
        return loggedClients.size();
    }

    public MiddleClientModel getLoggedMiddleClient(String locationName) {
        Iterator<MiddleClientModel> it = loggedClients.iterator();
        while (it.hasNext()) {
            MiddleClientModel next = it.next();
            if (locationName.equals(next.getLocationName()))
                return next;
        }
        return null;
    }

    public synchronized boolean isMiddleClientLogged(String locationName) {
        return (getLoggedMiddleClient(locationName) != null ? true : false);
    }

    public synchronized boolean checkPassword(String password, String locationName){
        for(int ix=0; ix<registeredClients.size(); ix++) {
            if (locationName.equals(registeredClients.get(ix).getLocationName()))
                return password.equals(registeredClients.get(ix).getPassword());
        }
        return false;
    }

    public synchronized boolean checkUserClear(String username){
        for(int ix=0; ix<registeredClients.size(); ix++){
            if(username.equals(registeredClients.get(ix).getLocationName()))
                return false;
        }
        return true;
    }

    public MiddleClientModel getRegisteredMiddleClient(String locationName) {
        Iterator<RegisterClientModel> it = registeredClients.iterator();
        while (it.hasNext()) {
            RegisterClientModel next = it.next();
            if (locationName.equals(next.getLocationName()))
                return (MiddleClientModel) next;
        }
        return null;
    }

    public synchronized boolean isMiddleClientRegistered(String locationName) {
        return (getRegisteredMiddleClient(locationName) != null ? true : false);
    }

    //it does the same as the Middle_Client version of this class
    public synchronized boolean addRegisteredMiddleClient(RegisterClientModel client) {
        if (getRegisteredMiddleClient(client.getLocationName()) != null)
            return false;
        return registeredClients.add(client);
    }

    //percorrer para ler o ficheiro
    public synchronized void setRegisteredClients(JsonElement file){
        JsonArray clients
                = (file != null && file.isJsonArray()
                ? file.getAsJsonArray() : new JsonArray());
        if (clients != null) {
            int len = clients.size();
            for (int i=0;i<len;i++){
                JsonObject client = clients.get(i).getAsJsonObject();
                Iterator<JsonElement> rUsersIt = client.get("registeredUsers").getAsJsonArray().iterator();
                ArrayList<String> registeredUsers = new ArrayList<>();
                while (rUsersIt.hasNext())
                    registeredUsers.add(rUsersIt.next().getAsString());
                RegisterClientModel mcm = new RegisterClientModel(client.get("password").getAsString(), client.get("locationName").getAsString(), 
                            client.get("multicastAddress").getAsString(), registeredUsers);
                registeredClients.add(mcm);
            }
        }

    }

    public synchronized ArrayList<String> getAllRegisteredMulticastAddress() {
        ArrayList<String> list = new ArrayList<>();
        Iterator<RegisterClientModel> it = registeredClients.iterator();
        while (it.hasNext())
            list.add(it.next().getMulticastAddress());
        return list;
    }

    public Iterator<RegisterClientModel> getAllRegisteredClientsIterator(){
        return registeredClients.iterator();
    }
}