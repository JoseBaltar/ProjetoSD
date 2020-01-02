package server;

public class MiddleClientModel {

    private String middleclientip, multicastip, password;
    private int serverport, multicastport, waitingport;

    public MiddleClientModel(String middleclientip, String multicastip, String password, int serverport, int multicastport, int waitingport) {
        this.middleclientip = middleclientip;
        this.multicastip = multicastip;
        this.password = password;
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
}
