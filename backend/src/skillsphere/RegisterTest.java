package skillsphere;

// A simple console test program — no framework needed.
// Run this to confirm registration logic works correctly before
// connecting it to the frontend.
public class RegisterTest {

    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();

        System.out.println("---- Test 1: Valid Registration ----");
        User validUser = new User("Arjun Kumar", "arjun.kumar@example.com", "securePass123", "Loves coding.");
        boolean result1 = userDAO.registerUser(validUser);
        System.out.println("Result: " + (result1 ? "SUCCESS - user registered" : "FAILED"));

        System.out.println("\n---- Test 2: Duplicate Email ----");
        User duplicateUser = new User("Another Arjun", "arjun.kumar@example.com", "anotherPass456", "Trying to reuse an email.");
        boolean result2 = userDAO.registerUser(duplicateUser);
        System.out.println("Result: " + (result2 ? "SUCCESS (unexpected!)" : "FAILED as expected - duplicate email blocked"));

        System.out.println("\n---- Test 3: Empty/Invalid Input ----");
        User invalidUser = new User("", "", "", "");
        boolean result3 = userDAO.registerUser(invalidUser);
        System.out.println("Result: " + (result3 ? "SUCCESS (unexpected!)" : "FAILED as expected - empty fields blocked"));
    }
}
