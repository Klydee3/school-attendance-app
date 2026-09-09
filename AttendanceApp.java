import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.InputMismatchException;
public class AttendanceApp {
    static HashMap<String,Student> students=new HashMap<>();
    static HashMap<String,Account> accounts=new HashMap<>();
    static HashMap<String,Account> sessions=new HashMap<>();
    static void loadRegistry() {
        students.clear();
        for (Student s:FileStorage.loadStudents("students.txt")) {
            students.put(s.fullName(),s);
        }
        accounts.clear();
        List<Account> loaded=FileStorage.loadAccounts("accounts.txt");
        if(loaded.isEmpty()) {
            for(Student a:students.values()) {
                accounts.put(a.fullName(), new Account(a.fullName(), "1234", Role.STUDENT));
            }
            accounts.put("teacher",new Account("teacher","teacher123",Role.TEACHER));
            accounts.put("admin",new Account("admin","admin123",Role.ADMIN));
            FileStorage.saveAccounts(new ArrayList<>(accounts.values()),"accounts.txt");
        } else {
            for(Account a:loaded) {
                accounts.put(a.getLogin(),a);
            }
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
        System.out.print("Введите имя и фамилию студента:");
        String newFullName=scann.nextLine();
        String[] fio=newFullName.split(" ",2);
        String surName=fio[0];
        String name=fio.length>1?fio[1]:"";
        Student s=new Student(surName,name,RegistrationStatus.PENDING);
        students.put(s.fullName(),s);
        System.out.println("Студент "+s.fullName()+" успешно зарегистрирован!(статус:ожидание)");
    }
    static void reviewStudent(Scanner scann) {
        System.out.print("Введите имя и фамилию студента:");
        String fullName=scann.nextLine();
        if(students.containsKey(fullName)) {
            Student target=students.get(fullName);
            System.out.println("Текущий статус: "+target.getStatus());
            System.out.println("Подтвердить/отклонить?");
            String answer=scann.nextLine();
            if(answer.equalsIgnoreCase("Подтвердить")) {
                target.setStatus(RegistrationStatus.APPROVED);
                System.out.println("Студент "+fullName+" подтвержден!");
            } else if(answer.equalsIgnoreCase("Отклонить")) {
                target.setStatus(RegistrationStatus.REJECTED);
                System.out.println("Студент "+fullName+" отклонен!");
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