package skillsphere;

// A simple "model" class representing one row of the users table.
// Encapsulation: fields are private, accessed only through getters/setters.
public class User {

    private int userId;
    private String name;
    private String email;
    private String password;
    private String bio;
    private int credits;

    // Constructor used when registering a NEW user (no userId or credits yet)
    public User(String name, String email, String password, String bio) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.bio = bio;
    }

    // Empty constructor, useful when building an object piece by piece
    public User() {
    }

    // ----- Getters and Setters -----

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }
}
