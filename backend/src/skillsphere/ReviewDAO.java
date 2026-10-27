package skillsphere;

import java.sql.*;

// DAO for the "reviews" table.
public class ReviewDAO {

    // The learner of a COMPLETED request rates the teacher.
    // Returns "SUCCESS: ...", "FAILED: ..." or "ERROR: ..."
    public String createReview(int requestId, int learnerId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            return "FAILED: Rating must be between 1 and 5";
        }
        String cleanComment = (comment == null) ? "" : comment.trim();
        if (cleanComment.length() > 500) {
            return "FAILED: Comment is too long (max 500 characters)";
        }

        try (Connection conn = DBConnection.getConnection()) {

            // 1. The request must be this learner's and already COMPLETED
            int teacherId;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT teacher_id FROM exchange_requests "
                  + "WHERE request_id = ? AND learner_id = ? AND status = 'COMPLETED'")) {
                stmt.setInt(1, requestId);
                stmt.setInt(2, learnerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) return "FAILED: You can only review your own completed sessions";
                    teacherId = rs.getInt("teacher_id");
                }
            }

            // 2. Insert (the UNIQUE key on request_id blocks a second review)
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO reviews (request_id, reviewer_id, reviewed_user_id, rating, `comment`) "
                  + "VALUES (?, ?, ?, ?, ?)")) {
                stmt.setInt(1, requestId);
                stmt.setInt(2, learnerId);
                stmt.setInt(3, teacherId);
                stmt.setInt(4, rating);
                stmt.setString(5, cleanComment);
                stmt.executeUpdate();
            }
            return "SUCCESS: Review submitted";

        } catch (SQLIntegrityConstraintViolationException e) {
            return "FAILED: You already reviewed this session";
        } catch (SQLException e) {
            System.out.println("Error saving review: " + e.getMessage());
            return "ERROR: Server could not save the review. Check the backend console.";
        }
    }
}