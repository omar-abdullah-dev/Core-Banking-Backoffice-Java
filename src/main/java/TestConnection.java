import com.finance.bank.model.*;
import com.finance.bank.service.AuthenticationService;
import com.finance.bank.service.BankService;

public class TestConnection {
    public static void main(String[] args) {
        try {
            AuthenticationService auth = new AuthenticationService();
            Employee manager = auth.login("manager", "manager123");

            BankService bankService = BankService.getInstance();
            bankService.reset();

            Customer customer = bankService.createCustomer(manager, "Ahmed Hassan", "29001011234567");
            System.out.println("✅ Customer created: " + customer.getName());

            SavingsAccount account = new SavingsAccount("1001000100000001", customer);
            bankService.openAccount(manager, account);
            System.out.println("✅ Account created: " + account.getAccountNumber());

            Account found = bankService.findAccountByNumber("1001000100000001");
            System.out.println("✅ Account found: " + found.getAccountNumber());
            System.out.println("Balance: " + found.getBalance());

        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}