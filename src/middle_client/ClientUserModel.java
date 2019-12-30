package middle_client;

public class ClientUserModel {
    private String username;
    private String password;
    private String location;

    public ClientUserModel(String username, String password, String location){
        this.username = username;
        this.password = password;
        this.location = location;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getLocation() {
        return location;
    }
}
