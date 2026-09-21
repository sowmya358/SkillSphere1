-- ==========================================================
-- SkillSphere Database Schema
-- Step 2: Database + Backend Foundation
-- ==========================================================

CREATE DATABASE IF NOT EXISTS skillsphere;
USE skillsphere;

-- ----------------------------------------------------------
-- Table: users
-- Stores every registered student/user account
-- ----------------------------------------------------------
CREATE TABLE users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    bio         TEXT,
    credits     INT NOT NULL DEFAULT 10,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------
-- Table: skills
-- Skills a user can teach. One user can have many skills.
-- ----------------------------------------------------------
CREATE TABLE skills (
    skill_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    skill_name  VARCHAR(100) NOT NULL,
    category    VARCHAR(100),
    description TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- Table: requests
-- A learner requests to learn a skill from a teacher.
-- ----------------------------------------------------------
CREATE TABLE requests (
    request_id   INT AUTO_INCREMENT PRIMARY KEY,
    learner_id   INT NOT NULL,
    teacher_id   INT NOT NULL,
    skill_id     INT NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (learner_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- Table: transactions
-- Tracks credit changes for each user (earned/spent)
-- ----------------------------------------------------------
CREATE TABLE transactions (
    transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    credits          INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    description      VARCHAR(255),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- Table: reviews
-- Feedback left by one user about another after a request
-- ----------------------------------------------------------
CREATE TABLE reviews (
    review_id       INT AUTO_INCREMENT PRIMARY KEY,
    request_id      INT NOT NULL,
    reviewer_id     INT NOT NULL,
    reviewed_user_id INT NOT NULL,
    rating          INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    review_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- Table: admins
-- Separate login table for platform administrators
-- ----------------------------------------------------------
CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- ----------------------------------------------------------
-- Sample data (only for testing Step 2's connection)
-- ----------------------------------------------------------
INSERT INTO users (name, email, password, bio, credits)
VALUES ('Test User', 'testuser@example.com', 'testpass123', 'Just a sample account for testing.', 10);

INSERT INTO admins (username, password)
VALUES ('admin', 'admin123');
