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

        // Searches skills offered by OTHER users and ranks them with simple rules:
    //   exact name 100, name starts with 70, name contains 50,
    //   category contains 30, description contains 10.
    // Ties are broken by the teacher's average review rating.
    public List<SkillMatch> searchSkills(String keyword, int excludeUserId) throws SQLException {
        String kw = (keyword == null) ? "" : keyword.trim().toLowerCase();
        // Escape LIKE wildcards so a typed % or _ is treated as plain text
        String safe = kw.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        String starts = safe + "%";
        String contains = "%" + safe + "%";

        String sql = "SELECT s.skill_id, s.user_id, u.name AS teacher_name, "
                + "s.skill_name, s.category, s.description, "
                + "(SELECT AVG(r.rating) FROM reviews r WHERE r.reviewed_user_id = s.user_id) AS avg_rating, "
                + "(CASE WHEN LOWER(s.skill_name) = ? THEN 100 "
                + "      WHEN LOWER(s.skill_name) LIKE ? THEN 70 "
                + "      WHEN LOWER(s.skill_name) LIKE ? THEN 50 "
                + "      WHEN LOWER(s.category) LIKE ? THEN 30 "
                + "      WHEN LOWER(s.description) LIKE ? THEN 10 "
                + "      ELSE 0 END) AS score "
                + "FROM skills s JOIN users u ON u.user_id = s.user_id "
                + "WHERE s.user_id <> ? "
                + "AND (LOWER(s.skill_name) LIKE ? OR LOWER(s.category) LIKE ? OR LOWER(s.description) LIKE ?) "
                + "ORDER BY score DESC, avg_rating DESC, s.skill_id "
                + "LIMIT 50";

        List<SkillMatch> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, kw);
            stmt.setString(2, starts);
            stmt.setString(3, contains);
            stmt.setString(4, contains);
            stmt.setString(5, contains);
            stmt.setInt(6, excludeUserId);
            stmt.setString(7, contains);
            stmt.setString(8, contains);
            stmt.setString(9, contains);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double rating = rs.getDouble("avg_rating");
                    Double avg = rs.wasNull() ? null : rating;
                    list.add(new SkillMatch(
                            rs.getInt("skill_id"), rs.getInt("user_id"),
                            rs.getString("teacher_name"), rs.getString("skill_name"),
                            rs.getString("category"), rs.getString("description"),
                            avg, rs.getInt("score")));
                }
            }
        }
        return list;
    }
    
    // Null-safe trim for optional fields
    private String clean(String s) {
        return (s == null) ? "" : s.trim();
    }
}