package skillsphere;

import java.sql.*;

// DAO = Data Access Object. This class is the ONLY place that talks to the
// "users" table directly with SQL. Keeping SQL in one place makes the
// project easier to maintain and explain.
public class UserDAO {

    // Checks if an email is already registered
    public boolean emailExists(String email) {
        String sql = "SELECT user_id FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true if a row was found
            }

        } catch (SQLException e) {
            System.out.println("Error checking email: " + e.getMessage());
            return false;
        }
    }

    // Registers a new user. Returns true if successful, false otherwise.
    public boolean registerUser(User user) {

        // Basic validation before touching the database
        if (user.getName() == null || user.getName().trim().isEmpty()
                || user.getEmail() == null || user.getEmail().trim().isEmpty()
                || user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            System.out.println("Registration failed: missing required fields.");
            return false;
        }

        if (emailExists(user.getEmail())) {
            System.out.println("Registration failed: email already registered.");
            return false;
        }

        String sql = "INSERT INTO users (name, email, password, bio, credits) VALUES (?, ?, ?, ?, ?)";
        int startingCredits = 100; // every new user starts with 100 credits

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getBio());
            stmt.setInt(5, startingCredits);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
            return false;
        }
    }
}
