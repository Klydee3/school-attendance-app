public class Account {
    private String login;
    private String password;
    private Role role;
    Account(String login,String password,Role role) {
        this.login=login;
        this.password=password;
        this.role=role;
    }
    public String getLogin() {
        return login;
    }
    public String getPassword() {
        return password;
    }
    public Role getRole() {
        return role;
    }
}