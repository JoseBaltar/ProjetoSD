package middle_client.utils;

import com.google.gson.*;
import middle_client.utils.RegisterClientModel;

import java.util.ArrayList;
import java.util.Iterator;

public class UserTracking {

    private ArrayList<RegisterClientModel> registeredUsers; // ficheiro
    private ArrayList<String> registeredUsername;
    private ArrayList<String> loggedUsers;

    public UserTracking() {
        registeredUsers = new ArrayList<>();
        registeredUsername = new ArrayList<>();
        loggedUsers = new ArrayList<>();
    }

    /**
     * @param file Elemento Json com a informação contida no ficheiro
     * @param middleClientlocation Nome da localizacao a utilizar
     *
     *                             Loads JSON data into a Json Array which is then parsed to an ArrayList, containing all the information about  the End Users
     *                             Checks if the location of the User is identical of the current instance and loads into into a Model Class referring to the End User
     *                             Additionally, it loads those usernames into another ArrayList made of Strings
     *
     */
    //Esta aqui deverá ser chamada no ínicio de cada "sessão" para carregar os que existem no ficheiro, carregando os utilizadores existentes na sua localização apenas
    public synchronized void setRegisteredUsers(JsonElement file, String middleClientlocation){
        JsonArray utilizadores
                = (file != null && file.isJsonArray()
                ? file.getAsJsonArray() : new JsonArray());
        if (utilizadores != null) {
            int len = utilizadores.size();
            for (int i=0;i<len;i++){
                JsonObject obj = utilizadores.get(i).getAsJsonObject();
                if(middleClientlocation.equals(obj.get("location").getAsString())) {
                    RegisterClientModel cum = new RegisterClientModel(obj.get("username").getAsString(), obj.get("password").getAsString());
                    registeredUsers.add(cum);
                    registeredUsername.add(obj.get("username").getAsString());
                }
            }
        }

    }

    /**
     * @param obj JsonObject containing the information about a new user being added
     * @return true if successful, false if it fails
     *
     * Adds a new user to the User related Data Structures.
     * Should be used to add a new user whenever the program is running, allowing not to re-read the JSON file containing all the info (used in ClientLoginProtocol))
     *
     */
    //Esta aqui deve ser usada para adicionar um novo user enquanto o programa corre, sem haver necessidade de reler o ficheiro (no método do protocolo registerUserJson)
    public synchronized boolean addRegisteredUser(JsonObject obj){
        RegisterClientModel cum = new RegisterClientModel(obj.get("username").getAsString(), obj.get("password").getAsString());
        registeredUsers.add(cum);
        return registeredUsername.add(obj.get("username").getAsString());
    }


    /**
     * @param password Password to be checked
     * @param username Username to check
     * @return true if the Passwords match for the user, false if otherwise
     *
     *Checks the given Password to the given Username, in order to enable log in
     *
     */
    public synchronized boolean checkPassword(String password, String username){
        for(int ix=0; ix<=registeredUsers.size(); ix++) {
            if (username.equals(registeredUsers.get(ix).getUsername()))
                return password.equals(registeredUsers.get(ix).getPassword());
        }
        return false;
    }

    public Iterator<RegisterClientModel> getAllUsers(){
        return  registeredUsers.iterator();
    }

    /**
     * @param username Username to be checked
     * @return FALSE if the user is not clear, TRUE if the user is clear and can be used
     *
     * it checks the registeredUsername ArrayList associated to the location being used on and checks if it exists or not
     */
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