package skillsphere;

// Model class representing one row of the skills table.
public class Skill {

    private int skillId;
    private int userId;
    private String name;
    private String category;
    private String description;

    public Skill() {
    }

    public Skill(int userId, String name, String category, String description) {
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.description = description;
    }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}