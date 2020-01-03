package server.utils;

import middle_client.utils.ClientUserModel;

import java.util.ArrayList;

public class MiddleClientModel {

    private String middleclientip, multicastip, password, locationName;
    private int serverport, multicastport, waitingport;
    private ArrayList<String> associatedLoggedUsers;
    private ArrayList<ClientUserModel> associatedUsers;

    public MiddleClientModel(String middleclientip, String multicastip, String password, String locationName, int serverport, int multicastport, int waitingport) {
        this.middleclientip = middleclientip;
        this.multicastip = multicastip;
        this.password = password;
        this.locationName = locationName;
        this.serverport = serverport;
        this.multicastport = multicastport;
        this.waitingport = waitingport;
    }

    public String getMiddleclientip() {
        return middleclientip;
    }

    public String getMulticastip() {
        return multicastip;
    }

    public int getServerport() {
        return serverport;
    }

    public int getMulticastport() {
        return multicastport;
    }

    public int getWaitingport() {
        return waitingport;
    }

    public String getPassword() {
        return password;
    }

    public String getLocationName() {
        return locationName;
    }

    public ArrayList<String> getAssociatedLoggedUsers() {
        return associatedLoggedUsers;
    }

    public void setAssociatedLoggedUsers(ArrayList<String> associatedLoggedUsers) {
        this.associatedLoggedUsers = associatedLoggedUsers;
    }

    public ArrayList<ClientUserModel> getAssociatedUsers() {
        return associatedUsers;
    }

    public void setAssociatedUsers(ArrayList<ClientUserModel> associatedUsers) {
        this.associatedUsers = associatedUsers;
    }
}
