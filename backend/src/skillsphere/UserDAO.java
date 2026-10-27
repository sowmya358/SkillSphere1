package skillsphere;

import java.sql.*;

// DAO = Data Access Object. This class is the ONLY place that talks to the
// "users" table directly with SQL. Keeping SQL in one place makes the
// project easier to maintain and explain.
public class UserDAO {

    // Checks if an email is already registered
    public boolean emailExists(String email) {
        try (Connection conn = DBConnection.getConnection()) {
            return emailExists(conn, email.trim().toLowerCase());
        } catch (SQLException e) {
            System.out.println("Error checking email: " + e.getMessage());
            return false;
        }
    }

    // Helper that reuses an already-open connection
    private boolean emailExists(Connection conn, String email) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true if a row was found
            }
        }
    }

    // Registers a new user and returns a message for the caller:
    //   "SUCCESS: ..." -> account created
    //   "FAILED: ..."  -> the user's input was a problem (empty field, duplicate email)
    //   "ERROR: ..."   -> a server/database problem (wrong password, MySQL not running...)
        public String registerUser(User user) {
        // Validate the input first (name length, email format, password length)
        String problem = Validator.checkRegistration(
                user.getName(), user.getEmail(), user.getPassword());
        if (problem != null) return problem;

        String name = user.getName().trim();
        String email = user.getEmail().trim().toLowerCase();
        String bio = (user.getBio() == null) ? "" : user.getBio();
        int startingCredits = 100; // every new user starts with 100 credits

        String sql = "INSERT INTO users (name, email, password, bio, credits) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            if (emailExists(conn, email)) {
                return "FAILED: This email is already registered";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, PasswordUtil.hash(user.getPassword())); // hashed, never plain text
                stmt.setString(4, bio);
                stmt.setInt(5, startingCredits);
                stmt.executeUpdate();
            }
            return "SUCCESS: Account created";

        } catch (SQLIntegrityConstraintViolationException e) {
            // Two people registering the same email at the same moment
            return "FAILED: This email is already registered";
        } catch (SQLException e) {
            // Real details go to the server console, not to the browser
            System.out.println("Error registering user: " + e.getMessage());
            return "ERROR: Server could not save your account. Check the backend console.";
        }
    }

    // Checks email + password. Returns the User if correct, or null if the
    // email doesn't exist or the password is wrong.
    // Throws SQLException on a database problem so the server can tell
    // "wrong password" (401) apart from "database down" (500).
    public User loginUser(String email, String password) throws SQLException {
        if (email == null || password == null
                || email.trim().isEmpty() || password.isEmpty()) {
            return null;
        }

        String sql = "SELECT user_id, name, email, password, bio, credits FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(password, rs.getString("password"))) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setBio(rs.getString("bio"));
                    user.setCredits(rs.getInt("credits"));
                    // password is deliberately NOT copied into the object
                    return user;
                }
            }
        }
        return null; // wrong email or wrong password
    }
}