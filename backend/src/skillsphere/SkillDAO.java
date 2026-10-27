package skillsphere;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO for the "skills" table. All skill SQL lives here.
// Update and delete always check user_id too, so a user can only
// change their OWN skills.
public class SkillDAO {

    // Returns all skills belonging to one user (throws on database problems)
    public List<Skill> getSkillsByUser(int userId) throws SQLException {
        String sql = "SELECT skill_id, user_id, skill_name, category, description "
                   + "FROM skills WHERE user_id = ? ORDER BY skill_id";
        List<Skill> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Skill s = new Skill();
                    s.setSkillId(rs.getInt("skill_id"));
                    s.setUserId(rs.getInt("user_id"));
                    s.setName(rs.getString("skill_name"));
                    s.setCategory(rs.getString("category"));
                    s.setDescription(rs.getString("description"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    // Returns "SUCCESS: ...", "FAILED: ..." or "ERROR: ..."
    public String addSkill(Skill skill) {
        if (skill.getName() == null || skill.getName().trim().isEmpty()) {
            return "FAILED: Skill name is required";
        }
        String sql = "INSERT INTO skills (user_id, skill_name, category, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, skill.getUserId());
            stmt.setString(2, skill.getName().trim());
            stmt.setString(3, clean(skill.getCategory()));
            stmt.setString(4, clean(skill.getDescription()));
            stmt.executeUpdate();
            return "SUCCESS: Skill added";
        } catch (SQLIntegrityConstraintViolationException e) {
            return "FAILED: User does not exist";   // foreign key on user_id
        } catch (SQLException e) {
            System.out.println("Error adding skill: " + e.getMessage());
            return "ERROR: Server could not save the skill. Check the backend console.";
        }
    }

    public String updateSkill(Skill skill) {
        if (skill.getName() == null || skill.getName().trim().isEmpty()) {
            return "FAILED: Skill name is required";
        }
        String sql = "UPDATE skills SET skill_name = ?, category = ?, description = ? "
                   + "WHERE skill_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, skill.getName().trim());
            stmt.setString(2, clean(skill.getCategory()));
            stmt.setString(3, clean(skill.getDescription()));
            stmt.setInt(4, skill.getSkillId());
            stmt.setInt(5, skill.getUserId());
            int rows = stmt.executeUpdate();
            return rows > 0 ? "SUCCESS: Skill updated" : "FAILED: Skill not found";
        } catch (SQLException e) {
            System.out.println("Error updating skill: " + e.getMessage());
            return "ERROR: Server could not update the skill. Check the backend console.";
        }
    }

    public String deleteSkill(int skillId, int userId) {
        String sql = "DELETE FROM skills WHERE skill_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, skillId);
            stmt.setInt(2, userId);
            int rows = stmt.executeUpdate();
            return rows > 0 ? "SUCCESS: Skill deleted" : "FAILED: Skill not found";
        } catch (SQLException e) {
            System.out.println("Error deleting skill: " + e.getMessage());
            return "ERROR: Server could not delete the skill. Check the backend console.";
        }
    }

    // Null-safe trim for optional fields
    private String clean(String s) {
        return (s == null) ? "" : s.trim();
    }
}