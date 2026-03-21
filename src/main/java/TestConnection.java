import com.finance.bank.model.Employee;
import com.finance.bank.service.AuthenticationService;

public class TestConnection {
    public static void main(String[] args) {
        AuthenticationService auth = new AuthenticationService();

        try {
            Employee emp = auth.login("manager", "manager123");
            System.out.println("✅ Login successful!");
            System.out.println("Name: " + emp.getUserName());
            System.out.println("Role: " + emp.getRole());
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }
}