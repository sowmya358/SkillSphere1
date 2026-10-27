CREATE DATABASE IF NOT EXISTS skillsphere;
USE skillsphere;

-- Drop in reverse order of dependencies (this DELETES all data)
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS exchange_requests;
DROP TABLE IF EXISTS skills;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id  INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(50)  NOT NULL,
    email    VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,              -- "salt:hash", about 69 characters
    credits  INT NOT NULL DEFAULT 100,
    role     VARCHAR(10) NOT NULL DEFAULT 'USER' -- USER or ADMIN
);

CREATE TABLE skills (
    skill_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    skill_name  VARCHAR(100) NOT NULL,
    category    VARCHAR(100),
    description VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE exchange_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    skill_id   INT NOT NULL,
    learner_id INT NOT NULL,
    teacher_id INT NOT NULL,
    status     VARCHAR(10) NOT NULL DEFAULT 'PENDING',  -- PENDING, ACCEPTED, REJECTED, COMPLETED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (skill_id)   REFERENCES skills(skill_id) ON DELETE CASCADE,
    FOREIGN KEY (learner_id) REFERENCES users(user_id)   ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(user_id)   ON DELETE CASCADE
);

CREATE TABLE reviews (
    review_id        INT AUTO_INCREMENT PRIMARY KEY,
    request_id       INT NULL,
    reviewer_id      INT NOT NULL,
    reviewed_user_id INT NOT NULL,
    rating           INT NOT NULL,
    `comment`        VARCHAR(500),
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_review_request (request_id),
    FOREIGN KEY (request_id)       REFERENCES exchange_requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id)      REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5)
);

-- No sample users: passwords are hashed by the app.
-- Register an account on the website, then make it an admin:
-- UPDATE users SET role = 'ADMIN' WHERE email = 'admin@skillsphere.com';