import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
public class AttendanceApp {
    static HashMap<String,Student> students=new HashMap<>();
    static void loadRegistry() {
        ArrayList<Student> list=FileStorage.loadStudents("students.txt");
        for(Student s:list) {
            students.put(s.getName(),s);
        }
    }
    public static void main(String[] args) {
        loadRegistry();
        Scanner scann =new Scanner(System.in);
        int choise;
        do {
            System.out.println("=== Школьная система ===");
            System.out.println("1. Показать всех студентов");
            System.out.println("2. Зарегистрировать студента");
            System.out.println("3. Подтвердить/отклонить студента");
            System.out.println("4. Сохранить в файл");
            System.out.println("0. Выход");
            System.out.print("Твой выбор:");
            choise=scann.nextInt();
            scann.nextLine();
            if(choise==1) {
                int approvedCount=0;
                int rejectedCount=0;
                int pendingCount=0;
                for (Student s:students.values()) {
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
            } else if(choise==2) {
                System.out.print("Введите имя студента:");
                String newName=scann.nextLine();
                students.put(newName,new Student(newName));
            } else if(choise==3) {
                System.out.print("Введите имя студента:");
                String nameForNewStatus=scann.nextLine();
                System.out.print("Подтвердить/отклонить?");
                String answer=scann.nextLine();
                Student target=students.get(nameForNewStatus);
                if(answer.equalsIgnoreCase("Подтвердить")) {
                    target.setStatus(RegistrationStatus.APPROVED);
                } else if(answer.equalsIgnoreCase("Отклонить")) {
                    target.setStatus(RegistrationStatus.REJECTED);
                } else {
                    System.out.println("Вы ввели некорректный ответ");
                }
            } else if(choise==4) {
                FileStorage.saveStudents(new ArrayList<>(students.values()),"students.txt");
            } else if(choise!=0) {
                System.out.println("Неизвестная команда");
            }
        }while(choise!=0);
        System.out.println("До свидания!");
        scann.close();
    }
}