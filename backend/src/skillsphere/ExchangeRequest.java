package skillsphere;

// One row of exchange_requests, plus display names for the dashboard.
public class ExchangeRequest {

    private final int requestId;
    private final String skillName;
    private final String otherName;   // learner's name (incoming) or teacher's name (outgoing)
    private final String status;
    private final String createdAt;
    private final Integer rating;     // review given for this session, null = not reviewed yet

    public ExchangeRequest(int requestId, String skillName, String otherName,
                           String status, String createdAt, Integer rating) {
        this.requestId = requestId;
        this.skillName = skillName;
        this.otherName = otherName;
        this.status = status;
        this.createdAt = createdAt;
        this.rating = rating;
    }

    public int getRequestId() { return requestId; }
    public String getSkillName() { return skillName; }
    public String getOtherName() { return otherName; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public Integer getRating() { return rating; }
}