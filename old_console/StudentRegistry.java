import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
public class StudentRegistry {
    static HashMap<String,Student> students=new HashMap<>();
    static void loadRegistry() {
        ArrayList<Student> list=FileStorage.loadStudents("students.txt");
        for(Student s:list) {
            students.put(s.getName(),s);
        }
    }
    static void findStudent(HashMap<String,Student> map,String name) {
        if(map.get(name)!=null) {
            Student found=map.get(name);
            System.out.println("Статус ученика "+name+": "+found.getStatus());
        } else {
            System.out.println("Студент не найден");
        }
    }
    public static void main(String[] args) {
        loadRegistry();
        Scanner scann=new Scanner(System.in);
        String answer;
        do {
            System.out.println("Какого студента вы хотите найти?");
            String name=scann.nextLine();
            findStudent(students,name);
            System.out.println("Найти еще?(Да/Нет)");
            answer=scann.nextLine();
        } while(answer.equalsIgnoreCase("Да"));
        scann.close();
    }
}