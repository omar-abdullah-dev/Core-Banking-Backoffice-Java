public class TestConnection {
    public static void main(String[] args) {
        try (var conn = com.finance.bank.config.DatabaseConfig.getConnection()) {
            System.out.println("✅ Connected successfully!");
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }
}