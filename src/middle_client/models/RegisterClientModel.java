package middle_client.models;

public class RegisterClientModel {
    private String username;
    private String password;

    public RegisterClientModel(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "username: " + username + "; password: " + password;
    }
}
