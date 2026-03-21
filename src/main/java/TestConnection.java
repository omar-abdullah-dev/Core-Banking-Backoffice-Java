import com.finance.bank.model.Customer;
import com.finance.bank.repository.CustomerRepository;
import com.finance.bank.service.BankService;
import com.finance.bank.service.AuthenticationService;
import com.finance.bank.model.Employee;

public class TestConnection {
    public static void main(String[] args) {
        try {
            AuthenticationService auth = new AuthenticationService();
            Employee manager = auth.login("manager", "manager123");

            BankService bankService = BankService.getInstance();
            bankService.reset();

            Customer customer = bankService.createCustomer(manager, "Ahmed Hassan", "29001011234567");
            System.out.println("✅ Customer created!");
            System.out.println("Name: " + customer.getName());
            System.out.println("ID: " + customer.getSystemId());
            System.out.println("National ID: " + customer.getNationalId());

            Customer found = bankService.findCustomerByNationalId("29001011234567");
            System.out.println("✅ Customer found: " + found.getName());

        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}