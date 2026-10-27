package skillsphere;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// DAO for admin features. Reads the "role" column of users.
public class AdminDAO {

    public boolean isAdmin(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM users WHERE user_id = ? AND role = 'ADMIN'")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Returns {userId, name} if email + password belong to an admin, otherwise null
    public String[] login(String email, String password) throws SQLException {
        if (email == null || password == null) return null;
        String sql = "SELECT user_id, name, password, role FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()
                        && "ADMIN".equals(rs.getString("role"))
                        && PasswordUtil.verify(password, rs.getString("password"))) {
                    return new String[] { String.valueOf(rs.getInt("user_id")), rs.getString("name") };
                }
            }
        }
        return null;
    }

    public List<Map<String, Object>> getAllUsers() throws SQLException {
        String sql = "SELECT u.user_id, u.name, u.email, u.credits, u.role, "
                   + "(SELECT COUNT(*) FROM skills s WHERE s.user_id = u.user_id) AS skill_count "
                   + "FROM users u ORDER BY u.user_id";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("userId", rs.getInt("user_id"));
                row.put("name", rs.getString("name"));
                row.put("email", rs.getString("email"));
                row.put("credits", rs.getInt("credits"));
                row.put("role", rs.getString("role"));
                row.put("skillCount", rs.getInt("skill_count"));
                list.add(row);
            }
        }
        return list;
    }

    public List<Map<String, Object>> getAllSkills() throws SQLException {
        String sql = "SELECT s.skill_id, s.skill_name, s.category, s.description, u.name AS teacher_name "
                   + "FROM skills s JOIN users u ON u.user_id = s.user_id ORDER BY s.skill_id";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("skillId", rs.getInt("skill_id"));
                row.put("name", rs.getString("skill_name"));
                row.put("category", rs.getString("category"));
                row.put("description", rs.getString("description"));
                row.put("teacherName", rs.getString("teacher_name"));
                list.add(row);
            }
        }
        return list;
    }

    // Returns "SUCCESS: ...", "FAILED: ..." or "ERROR: ..."
    public String deleteSkill(int skillId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM skills WHERE skill_id = ?")) {
            stmt.setInt(1, skillId);
            return stmt.executeUpdate() > 0 ? "SUCCESS: Skill removed" : "FAILED: Skill not found";
        } catch (SQLException e) {
            System.out.println("Error removing skill: " + e.getMessage());
            return "ERROR: Server could not remove the skill. Check the backend console.";
        }
    }
}