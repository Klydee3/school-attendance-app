import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class FileStorage {
    public static void saveStudents(ArrayList<Student> students,String fileName) {  
        try {
            FileWriter writer = new FileWriter(fileName);
            for (Student s : students) {
                writer.write(s.fullName() + ":" + s.getStatus() + "\n");
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
                String[] fio=parts[0].trim().split(" ",2);
                String surname=fio[0];
                String name=fio.length>1?fio[1]:"";
                RegistrationStatus status=RegistrationStatus.valueOf(parts[1].trim());
                students.add(new Student(name,surname,status));
            }
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Ошибка чтения: " + e.getMessage());
        }
        return students;
    }
    static List<Account> loadAccounts(String fileName) {
        List<Account> result=new ArrayList<>();
        java.io.File file=new java.io.File(fileName);
        if(!file.exists()) {
            return result;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line=reader.readLine()) != null) {
                if(line.trim().isEmpty()) {
                    continue;
                }
                String[] parts=line.split(":",3);
                if(parts.length<3) {
                    continue;
                }
                result.add(new Account(parts[0],parts[1],Role.valueOf(parts[2])));
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Ошибка чтения: " + e.getMessage());
        }
        return result;
    }
    static void saveAccounts(List<Account> accounts,String fileName) {
        StringBuilder sb=new StringBuilder();
        for(Account a:accounts) {
            sb.append(a.getLogin()).append(":")
            .append(a.getPassword()).append(":")
            .append(a.getRole()).append("\n");
        }
        try {
            FileWriter writer=new FileWriter(fileName);
            for (Account a:accounts) {
                writer.write(a.getLogin()+":"+a.getPassword()+":"+a.getRole()+"\n");
            }
            writer.close();
            System.out.println("Сохранено в файл "+fileName);
        } catch (IOException e) {
            System.out.println("Ошибка записи: " + e.getMessage());
        }
    }
}