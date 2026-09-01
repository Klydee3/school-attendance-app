import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
public class FileStorage {
    public static void saveStudents(ArrayList<Student> students,String fileName) {  
        try {
            FileWriter writer = new FileWriter("students.txt");
            for (Student s : students) {
                writer.write(s.getName() + ":" + s.getStatus() + "\n");
            }
            writer.close();
            System.out.println("Сохранено в файл "+fileName);
        } catch (IOException e) {
            System.out.println("Ошибка записи: " + e.getMessage());
        }
    }
    public static ArrayList<Student> loadStudents(String fileName) {
        ArrayList<Student> students=new ArrayList<>();
        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts=line.split(":");
                String name=parts[0].trim();
                RegistrationStatus status=RegistrationStatus.valueOf(parts[1].trim());
                students.add(new Student(name,status));
            }
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Ошибка чтения: " + e.getMessage());
        }
        return students;
    }
}