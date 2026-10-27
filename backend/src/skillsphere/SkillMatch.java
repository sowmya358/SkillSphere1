package skillsphere;

// One search result: a skill plus its teacher and how well it matched.
public class SkillMatch {

    private final int skillId;
    private final int teacherId;
    private final String teacherName;
    private final String name;
    private final String category;
    private final String description;
    private final Double avgRating; // null = the teacher has no reviews yet
    private final int score;

    public SkillMatch(int skillId, int teacherId, String teacherName, String name,
                      String category, String description, Double avgRating, int score) {
        this.skillId = skillId;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.name = name;
        this.category = category;
        this.description = description;
        this.avgRating = avgRating;
        this.score = score;
    }

    public int getSkillId() { return skillId; }
    public int getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public Double getAvgRating() { return avgRating; }
    public int getScore() { return score; }
}