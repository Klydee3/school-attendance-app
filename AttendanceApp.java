import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
                String studentSalt=newSalt();
				Account studentAcc=new Account(a.fullName(),Role.STUDENT,studentSalt,hashPassword("12345678",studentSalt));
				studentAcc.setApproved(true);
				accounts.put(a.fullName(),studentAcc);
            }
            String teacherSalt=newSalt();
			Account teacherAcc=new Account("teacher",Role.TEACHER,teacherSalt,hashPassword("12345678",teacherSalt));
			teacherAcc.setApproved(true);
			accounts.put("teacher",teacherAcc);
            String adminSalt=newSalt();
			Account adminAcc=new Account("admin",Role.ADMIN,adminSalt,hashPassword("12345678",adminSalt));
			adminAcc.setApproved(true);
			adminAcc.setSchool("Школа №1");
			accounts.put("admin",adminAcc);
			String cafeSalt=newSalt();
			Account cafeAcc=new Account("admin",Role.CAFETERIA,cafeSalt,hashPassword("12345678",cafeSalt));
			cafeAcc.setApproved(true);
			cafeAcc.setSchool("Школа №1");
			accounts.put("cafe",cafeAcc);
            FileStorage.saveAccounts(new ArrayList<>(accounts.values()),"accounts.txt");
        } else {
            for(Account a:loaded) {
                accounts.put(a.getLogin(),a);
            }
        }
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
	static String newSalt() {
		byte[] salt=new byte[16];
		new SecureRandom().nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
		}
	static String hashPassword(String password, String salt) {
		try {
			PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),
				Base64.getDecoder().decode(salt),10000,256);
			SecretKeyFactory factory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			byte[] hash=factory.generateSecret(spec).getEncoded();
			return Base64.getEncoder().encodeToString(hash);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static java.util.Map<String,String> classTeacher=new java.util.HashMap<>();
	static void loadClassTeachers(){
		classTeacher.clear();
		java.io.File f=new java.io.File("classteachers.txt");
		if(!f.exists()){
			return;
		}
		try(BufferedReader r=new BufferedReader(new InputStreamReader(new FileInputStream(f),StandardCharsets.UTF_8))){
			String line;
			while((line=r.readLine())!=null){
				String[] p=line.split(";");
				if(p.length==2&&!p[0].isEmpty()){
					classTeacher.put(p[0],p[1]);
				}
			}
		}catch(IOException e){
			System.out.println("Ошибка чтения classteachers.txt: "+e.getMessage());
		}
	}
	static synchronized void saveClassTeachers(){
		try(java.io.Writer w=new OutputStreamWriter(new FileOutputStream("classteachers.txt"),StandardCharsets.UTF_8)){
			for(java.util.Map.Entry<String,String> e:classTeacher.entrySet()){
				w.write(e.getKey()+";"+e.getValue()+"\n");
			}
		}catch(IOException e){
			System.out.println("Ошибка записи classteachers.txt: "+e.getMessage());
		}
	}
	static synchronized void saveAccounts() {
		try(java.io.Writer w=new java.io.OutputStreamWriter(
				new java.io.FileOutputStream("accounts.txt"),
				java.nio.charset.StandardCharsets.UTF_8)) {
			for(java.util.Map.Entry<String,Account> e:accounts.entrySet()) {
				Account a=e.getValue();
				w.write(e.getKey()+":"+
						a.getRole()+":"+
						a.getSalt()+":"+
						a.getPasswordHash()+":"+
						(a.isApproved()?"APPROVED":"PENDING")+
						(a.getSchool()==null?"":a.getSchool())+"\n");
			}
		} catch(java.io.IOException e) {
			System.out.println("Ошибка записи accounts.txt: "+e.getMessage());
		}
	}
    public static void main(String[] args) {
        loadRegistry();
		loadClassTeachers();
    }
}