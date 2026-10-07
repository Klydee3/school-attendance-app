import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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
    public static void main(String[] args) {
        loadRegistry();
    }
}