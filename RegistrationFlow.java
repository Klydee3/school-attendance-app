import java.util.ArrayList;
public class RegistrationFlow {
    public static void main(String[] args) {
        ArrayList<Student> students=new ArrayList<>();
        students.add(new Student("Илья"));
        students.add(new Student("Мартын"));
        students.add(new Student("Арсений"));
        Teacher teacher = new Teacher("Екатерина");
        teacher.reviewStudent(students.get(0), RegistrationStatus.APPROVED);
        teacher.reviewStudent(students.get(1), RegistrationStatus.REJECTED);
        FileStorage.saveStudents(students,"students.txt");
    }
}