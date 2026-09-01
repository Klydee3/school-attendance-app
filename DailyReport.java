import java.util.ArrayList;
public class DailyReport {
    public static void main(String[] args) {
        ArrayList<Student> students=FileStorage.loadStudents("students.txt");
        System.out.println("--- Отчет для столовой ---");
        for(Student s:students) {
            if (s.getStatus() == RegistrationStatus.APPROVED) {
                System.out.println("Ученик " + s.getName() + " учтен. Готовить порцию.");
            } else if (s.getStatus() == RegistrationStatus.REJECTED) {
                System.out.println("Ученик " + s.getName() + " отсутствует. Порцию не готовить.");
            } else {
                System.out.println("Внимание! Статус ученика " + s.getName() + " не подтвержден учителем!");
            }
        }      
    }
}