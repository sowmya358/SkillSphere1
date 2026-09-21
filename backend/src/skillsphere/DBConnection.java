package skillsphere;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/skillsphere";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "sowmya@358"; 

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
        return connection;
    }

    // Quick standalone test: run this file directly to check the connection works.
    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            System.out.println("Connected to database successfully!");
        } else {
            System.out.println("Failed to connect to database.");
        }
    }
}
