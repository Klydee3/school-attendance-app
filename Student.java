class Student {
    private String surName;
    private String name;
    private RegistrationStatus status;
    public Student(String name,String surName,RegistrationStatus status) {
        this.name=name;
        this.surName=surName;
        this.status=status;
    }
    public Student(String name, String surName) {
        this(name,surName,RegistrationStatus.PENDING);
    }
    public Student(String name) {
        this(name,"",RegistrationStatus.PENDING);
    }
    public Student(String name,RegistrationStatus status) {
        this(name,"",status);
    }
    public String fullName() {
        return surName+" "+name;
    }
    String getName() {
        return name;
    }
    RegistrationStatus getStatus() {
        return status;
    }
    void setStatus(RegistrationStatus newStatus) {
        this.status = newStatus;
    }
    public String toJson() {
        return "{\"name\": \""+name+"\",\"surname\":\""+surName+"\",\"status\": \""+status+"\"}";
    }
}