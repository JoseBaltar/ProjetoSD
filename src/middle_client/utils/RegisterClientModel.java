package middle_client.utils;

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
}
