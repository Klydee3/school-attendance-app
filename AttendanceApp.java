import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.InputMismatchException;
public class AttendanceApp {
    static HashMap<String,Student> students=new HashMap<>();
    static void loadRegistry() {
        ArrayList<Student> list=FileStorage.loadStudents("students.txt");
        for(Student s:list) {
            students.put(s.getName(),s);
        }
    }
    static void printMenu() {
        System.out.println("=== Школьная система ===");
        System.out.println("1. Показать всех студентов");
        System.out.println("2. Зарегистрировать студента");
        System.out.println("3. Подтвердить/отклонить студента");
        System.out.println("4. Сохранить в файл");
        System.out.println("5. Показать всех в Json");
        System.out.println("0. Выход");
        System.out.print("Твой выбор:");
    }
    static int readChoise(Scanner scann) {
        int choise;
        try {
            choise=scann.nextInt();
            scann.nextLine();
        }catch(InputMismatchException e) {
            scann.nextLine();
            System.out.println("Ошибка, введите число!");
            choise=-1;
        }
        return choise;
    }
    static void showAll() {
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
    }
    static void registerStudent(Scanner scann) {
        System.out.print("Введите имя студента:");
        String newName=scann.nextLine();
        students.put(newName,new Student(newName));
        System.out.println("Студент "+newName+" успешно зарегистрирован!(статус:ожидание)");
    }
    static void reviewStudent(Scanner scann) {
        System.out.print("Введите имя студента:");
        String name=scann.nextLine();
        if(students.containsKey(name)) {
            Student target=students.get(name);
            System.out.println("Текущий статус: Ожидание");
            System.out.println("Подтвердить/отклонить?");
            String answer=scann.nextLine();
            if(answer.equalsIgnoreCase("Подтвердить")) {
                target.setStatus(RegistrationStatus.APPROVED);
                System.out.println("Студент "+name+" подтвержден!");
            } else if(answer.equalsIgnoreCase("Отклонить")) {
                target.setStatus(RegistrationStatus.REJECTED);
                System.out.println("Студент "+name+" отклонен!");
            } else {
                System.out.println("Вы ввели некорректный ответ");
            } 
        } else {
            System.out.println("Студент не найден");
        }
    }
    static void saveToFile() {
        FileStorage.saveStudents(new ArrayList<>(students.values()),"students.txt");
    }
    static String studentsToJson() {
        String result="[";
        int count=0;
        for(Student s:students.values()) {
            result+=s.toJson();
            count++;
            if(count<students.size()) {
                result+=", ";
            }
        }
        return result+="]";
    }
    static void showAllJson() {
        System.out.println(studentsToJson());
    }
    public static void main(String[] args) {
        loadRegistry();
        Scanner scann=new Scanner(System.in);
        int choise;
        do {
                printMenu();
                choise=readChoise(scann);
                if(choise==1) {
                    showAll();
                } else if(choise==2) {
                    registerStudent(scann);
                } else if(choise==3) {
                    reviewStudent(scann);
                } else if(choise==4) {
                    saveToFile();
                } else if(choise==5) {
                    showAllJson();
                } else if(choise!=0) {
                    System.out.println("Неизвестная команда");
                }   
        }while(choise!=0);
        System.out.println("До свидания!");
        scann.close();
    }
}