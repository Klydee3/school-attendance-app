import java.util.ArrayList;
public class StudentManager {
    public static void main(String[] args) {
        ArrayList<Student> students = new ArrayList<>();
        int approvedCount = 0;
        int rejectedCount = 0;
        int pendingCount = 0;
        for (Student s : students) {
            if (s.getStatus() == RegistrationStatus.APPROVED) {
                approvedCount++;
            } else if (s.getStatus() == RegistrationStatus.REJECTED) {
                rejectedCount++;
            } else if (s.getStatus() == RegistrationStatus.PENDING) {
                pendingCount++;
            }
        }
        System.out.println("Всего студентов: " + students.size());
        System.out.println(approvedCount + " подтверждено");
        System.out.println(rejectedCount + " отклонено");
        System.out.println(pendingCount + " в ожидании");
    }
}