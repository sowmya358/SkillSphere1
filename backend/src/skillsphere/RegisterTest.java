package skillsphere;

// A simple console test program — no framework needed.
// Run this to confirm registration logic works correctly before
// connecting it to the frontend.
// It uses a NEW email every run, so you can run it as many times as you like.
public class RegisterTest {

    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();
        String email = "test" + System.currentTimeMillis() + "@example.com";

        System.out.println("---- Test 1: Valid Registration ----");
        User validUser = new User("Arjun Kumar", email, "securePass123", "Loves coding.");
        System.out.println("Result: " + userDAO.registerUser(validUser));
        System.out.println("Expected: SUCCESS");

        System.out.println("\n---- Test 2: Duplicate Email ----");
        User duplicateUser = new User("Another Arjun", email, "anotherPass456", "Trying to reuse an email.");
        System.out.println("Result: " + userDAO.registerUser(duplicateUser));
        System.out.println("Expected: FAILED (already registered)");

        System.out.println("\n---- Test 3: Empty/Invalid Input ----");
        User invalidUser = new User("", "", "", "");
        System.out.println("Result: " + userDAO.registerUser(invalidUser));
        System.out.println("Expected: FAILED (all required)");
    }
}