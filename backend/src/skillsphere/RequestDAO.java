package skillsphere;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO for the "exchange_requests" table.
public class RequestDAO {
        private static final int SESSION_COST = 10;   // credits per completed session

    // Learner asks for a skill. Returns "SUCCESS: ...", "FAILED: ..." or "ERROR: ..."
    public String createRequest(int skillId, int learnerId) {
        try (Connection conn = DBConnection.getConnection()) {

            // 1. Find the skill and its owner (the teacher)
            int teacherId;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT user_id FROM skills WHERE skill_id = ?")) {
                stmt.setInt(1, skillId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) return "FAILED: Skill not found";
                    teacherId = rs.getInt("user_id");
                }
            }

            if (teacherId == learnerId) {
                return "FAILED: You cannot request your own skill";
            }

            // 2. Block duplicates (an open or accepted request already exists)
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT 1 FROM exchange_requests "
                  + "WHERE skill_id = ? AND learner_id = ? AND status IN ('PENDING','ACCEPTED')")) {
                stmt.setInt(1, skillId);
                stmt.setInt(2, learnerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return "FAILED: You already have a request for this skill";
                }
            }

            // 3. Insert
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO exchange_requests (skill_id, learner_id, teacher_id) VALUES (?, ?, ?)")) {
                stmt.setInt(1, skillId);
                stmt.setInt(2, learnerId);
                stmt.setInt(3, teacherId);
                stmt.executeUpdate();
            }
            return "SUCCESS: Request sent";

        } catch (SQLIntegrityConstraintViolationException e) {
            return "FAILED: User or skill does not exist";
        } catch (SQLException e) {
            System.out.println("Error creating request: " + e.getMessage());
            return "ERROR: Server could not send the request. Check the backend console.";
        }
    }

    // incoming = true  -> requests TO this user (as teacher), shows the learner's name
    // incoming = false -> requests FROM this user (as learner), shows the teacher's name
    public List<ExchangeRequest> getRequests(int userId, boolean incoming) throws SQLException {
        String sql = "SELECT r.request_id, s.skill_name, u.name AS other_name, r.status, r.created_at "
                   + "FROM exchange_requests r "
                   + "JOIN skills s ON s.skill_id = r.skill_id "
                   + "JOIN users u ON u.user_id = " + (incoming ? "r.learner_id " : "r.teacher_id ")
                   + "WHERE " + (incoming ? "r.teacher_id" : "r.learner_id") + " = ? "
                   + "ORDER BY r.request_id DESC";
        List<ExchangeRequest> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String created = rs.getString("created_at");
                    if (created != null && created.length() > 16) {
                        created = created.substring(0, 16);   // "2026-10-27 21:35"
                    }
                    list.add(new ExchangeRequest(
                            rs.getInt("request_id"), rs.getString("skill_name"),
                            rs.getString("other_name"), rs.getString("status"), created));
                }
            }
        }
        return list;
    }

    // Teacher accepts or rejects. Only the teacher of that request can do it,
    // and only while it is still PENDING.
    public String respond(int requestId, int teacherId, boolean accept) {
        String sql = "UPDATE exchange_requests SET status = ? "
                   + "WHERE request_id = ? AND teacher_id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accept ? "ACCEPTED" : "REJECTED");
            stmt.setInt(2, requestId);
            stmt.setInt(3, teacherId);
            int rows = stmt.executeUpdate();
            if (rows == 0) return "FAILED: Request not found or already answered";
            return accept ? "SUCCESS: Request accepted" : "SUCCESS: Request rejected";
        } catch (SQLException e) {
            System.out.println("Error answering request: " + e.getMessage());
            return "ERROR: Server could not update the request. Check the backend console.";
        }
    }
        // Teacher marks an ACCEPTED request as completed.
    // Moves SESSION_COST credits learner -> teacher in ONE transaction.
    public String completeSession(int requestId, int teacherId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Lock the request row and check it is ours and still ACCEPTED
                int learnerId;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT learner_id FROM exchange_requests "
                      + "WHERE request_id = ? AND teacher_id = ? AND status = 'ACCEPTED' FOR UPDATE")) {
                    stmt.setInt(1, requestId);
                    stmt.setInt(2, teacherId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "FAILED: Request not found or not in accepted state";
                        }
                        learnerId = rs.getInt("learner_id");
                    }
                }

                // 2. Take credits from the learner (only if they have enough)
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE users SET credits = credits - ? WHERE user_id = ? AND credits >= ?")) {
                    stmt.setInt(1, SESSION_COST);
                    stmt.setInt(2, learnerId);
                    stmt.setInt(3, SESSION_COST);
                    if (stmt.executeUpdate() == 0) {
                        conn.rollback();
                        return "FAILED: The learner does not have enough credits";
                    }
                }

                // 3. Give credits to the teacher
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE users SET credits = credits + ? WHERE user_id = ?")) {
                    stmt.setInt(1, SESSION_COST);
                    stmt.setInt(2, teacherId);
                    stmt.executeUpdate();
                }

                // 4. Mark the request completed
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE exchange_requests SET status = 'COMPLETED' WHERE request_id = ?")) {
                    stmt.setInt(1, requestId);
                    stmt.executeUpdate();
                }

                conn.commit();
                return "SUCCESS: Session completed. " + SESSION_COST + " credits transferred";

            } catch (SQLException e) {
                conn.rollback();   // undo everything if any step failed
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error completing session: " + e.getMessage());
            return "ERROR: Server could not complete the session. Check the backend console.";
        }
    }

    // Current credit balance of a user, or -1 if the user does not exist
    public int getCredits(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT credits FROM users WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("credits") : -1;
            }
        }
    }
}