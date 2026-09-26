public class Account {
    private String login;
    private Role role;
    private String salt;
    private String passwordHash;
	private boolean approved;
    public Account(String login,Role role,String salt,String passwordHash) {
        this.login=login;
        this.role=role;
        this.salt=salt;
        this.passwordHash=passwordHash;
    }
    public String getLogin(){return login;}
    public Role getRole(){return role;}
    public String getSalt(){return salt;}
    public String getPasswordHash(){return passwordHash;}
    public void setSalt(String salt){this.salt=salt;}
    public void setPasswordHash(String passwordHash){this.passwordHash=passwordHash;}
	public boolean isApproved(){return approved;}
	public void setApproved(boolean approved){this.approved=approved;}
}