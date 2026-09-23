import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
public class FileStorage {
    public static void saveStudents(ArrayList<Student> students,String fileName) {  
        try {
            OutputStreamWriter writer=new OutputStreamWriter(
				new FileOutputStream(fileName),StandardCharsets.UTF_8);
            for (Student s:students) {
                writer.write(s.fullName()+":"+s.getClassName()+":"+s.getStatus()+"\n");
            }
            writer.close();
            System.out.println("Сохранено в файл "+fileName);
        } catch (IOException e) {
            System.out.println("Ошибка записи: "+e.getMessage());
        }
    }
    public static ArrayList<Student> loadStudents(String fileName) {
        ArrayList<Student> students=new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName),StandardCharsets.UTF_8));
            String line;
            while ((line=reader.readLine())!=null) {
                String[] parts=line.split(":");
                String[] fio=parts[0].trim().split(" ",2);
                String surname=fio[0];
				String className=parts[1];
                String name=fio.length>1?fio[1]:"";
                RegistrationStatus status=RegistrationStatus.valueOf(parts[2].trim());
                students.add(new Student(name,surname,className,status));
            }
            reader.close();
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
            BufferedReader reader=new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName),StandardCharsets.UTF_8));
            String line;
            while ((line=reader.readLine()) != null) {
                if(line.trim().isEmpty()) {
                    continue;
                }
                String[] parts=line.split(":",4);
				if (parts.length<4) {
					continue;
				}
				result.add(new Account(parts[0],Role.valueOf(parts[1]),parts[2],parts[3]));
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Ошибка чтения: " + e.getMessage());
        }
        return result;
    }
    static void saveAccounts(List<Account> accounts,String fileName) {
        try {
            OutputStreamWriter writer=new OutputStreamWriter(
				new FileOutputStream(fileName),StandardCharsets.UTF_8);
            for (Account a:accounts) {
                writer.write(a.getLogin()+":"+a.getRole()+":"+a.getSalt()+":"+a.getPasswordHash()+"\n");
            }
            writer.close();
            System.out.println("Сохранено в файл "+fileName);
        } catch (IOException e) {
            System.out.println("Ошибка записи: " + e.getMessage());
        }
    }
}