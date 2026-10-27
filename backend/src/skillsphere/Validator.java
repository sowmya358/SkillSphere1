package skillsphere;

// Server-side checks for registration input.
public class Validator {

    // Returns an error message starting with "FAILED: ", or null if everything is fine
    public static String checkRegistration(String name, String email, String password) {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 50) {
            return "FAILED: Name must be 2 to 50 characters";
        }
        if (email == null || email.trim().length() > 100
                || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "FAILED: Enter a valid email address";
        }
        if (password == null || password.length() < 6 || password.length() > 64) {
            return "FAILED: Password must be 6 to 64 characters";
        }
        return null;
    }
}