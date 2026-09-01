public class Teacher {
    private String name;
    Teacher(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void reviewStudent(Student student, RegistrationStatus decision) {
        if (decision == RegistrationStatus.PENDING) {
            System.out.println("Нельзя установить статус ожидания вручную.");
            return;
        }
            student.setStatus(decision);
            if(decision==RegistrationStatus.APPROVED) {
                System.out.printf("Учитель %s подтвердил студента %s\n", name, student.getName());
            } else {
            System.out.printf("Учитель %s отклонил студента %s\n", name, student.getName());
        }    
    }
}