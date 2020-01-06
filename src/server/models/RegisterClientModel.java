package server.models;

import java.util.ArrayList;

public class RegisterClientModel extends MiddleClientModel {
    private String password;

    public RegisterClientModel(String password, String locationName, String multicastAddress, ArrayList<String> registeredUsers){
        super(locationName, multicastAddress, registeredUsers);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
