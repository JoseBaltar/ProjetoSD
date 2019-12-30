package middle_client;

import java.io.*;
import com.google.gson.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * TODO finish register and login on this class
 */
public class UserTracking {

    private ArrayList<ClientUserModel> registeredUsers; // ficheiro
    private ArrayList<String> registeredUsername;
    private ArrayList<String> loggedUsers;

    UserTracking() {
        registeredUsers = new ArrayList<>();
        loggedUsers = new ArrayList<>();
    }

    //Esta aqui deverá ser chamada no ínicio de cada "sessão" para carregar os que existem no ficheiro
    public synchronized void setRegisteredUsers(JsonElement file){
        JsonArray utilizadores
                = (file != null && file.isJsonArray()
                ? file.getAsJsonArray() : new JsonArray());
        if (utilizadores != null) {
            int len = utilizadores.size();
            for (int i=0;i<len;i++){
                JsonObject obj = utilizadores.get(i).getAsJsonObject();
                ClientUserModel cum = new ClientUserModel(obj.get("username").getAsString(), obj.get("password").getAsString(), obj.get("location").getAsString());
                registeredUsers.add(cum);
                registeredUsername.add(obj.get("username").getAsString());
            }
        }

    }

    //Esta aqui deve ser usada para adicionar um novo user enquanto o programa corre, sem haver necessidade de reler o ficheiro (no método do protocolo registerUserJson)
    public synchronized boolean addRegisteredUser(JsonObject obj){
        ClientUserModel cum = new ClientUserModel(obj.get("username").getAsString(), obj.get("password").getAsString(), obj.get("location").getAsString());
        registeredUsers.add(cum);
        return registeredUsername.add(obj.get("username").getAsString());
    }

    public Iterator<ClientUserModel> getAllUsers(){
        return  registeredUsers.iterator();
    }

    public synchronized boolean checkUserClear(String username){
        for(int ix=0; ix<=registeredUsername.size(); ix++){
            if(username.equals(registeredUsername.get(ix)))
                return false;
        }
        return true;
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