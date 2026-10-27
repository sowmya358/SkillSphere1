package skillsphere;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Opens a connection to the MySQL database.
// The password is NOT written in this file. It is read from backend/db.properties,
// a local file that is never pushed to GitHub (see .gitignore).
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/skillsphere";
    private static final String CONFIG_FILE = "db.properties";

    // Throws SQLException if the connection fails, so callers can handle it cleanly.
    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
        } catch (IOException e) {
            throw new SQLException("Could not read " + CONFIG_FILE
                    + ". Run the program from the backend folder and create db.properties "
                    + "(copy db.properties.example).");
        }

        String username = props.getProperty("db.username", "root");
        String password = props.getProperty("db.password", "");
        return DriverManager.getConnection(URL, username, password);
    }

    // Quick standalone test: run this file directly to check the connection works.
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connected to database successfully!");
        } catch (SQLException e) {
            System.out.println("Failed to connect to database: " + e.getMessage());
        }
    }
}
