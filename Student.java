class Student {
    private String name;
    private RegistrationStatus status;
    Student(String name) {
        this.name = name;
        this.status = RegistrationStatus.PENDING;
    }
    public Student(String name,RegistrationStatus status) {
        this.name=name;
        this.status=status;
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
        return "{\"name\": \""+name+"\",\"status\": \""+status+"\"}";
    }
}