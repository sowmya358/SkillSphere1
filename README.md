# SkillSphere

A skill-exchange platform for students. Students list skills they can teach,
find skills they want to learn, and pay for sessions with credits.

## Features
- Register and log in (passwords stored as salted SHA-256 hashes)
- Add, edit and delete your own skills
- Search skills offered by other students, ranked by fixed rules
  (exact name 100, name starts with 70, name contains 50, category 30, description 10;
  ties broken by the teacher's average rating)
- Send exchange requests; the teacher accepts or rejects them
- Mark a session completed: 10 credits move from learner to teacher in one
  database transaction
- Learners review completed sessions (1 to 5 stars, one review per session)
- Admin page: list all users, remove inappropriate skills

## Tech stack
- Frontend: HTML, CSS, JavaScript
- Backend: Java (built-in `com.sun.net.httpserver`), JDBC
- Database: MySQL

## Setup
1. Install JDK 11 or newer and MySQL 8.
2. Create the database: run `database/schema.sql` in MySQL.
3. Copy `db.properties.example` to `db.properties` and fill in your MySQL details.
4. Build, from the `backend` folder (PowerShell):

       javac -encoding UTF-8 -cp "lib/mysql-connector-j-26.7.0.jar" -d out src/skillsphere/*.java

5. Start the server and keep the window open:

       java -cp "out;lib/mysql-connector-j-26.7.0.jar" skillsphere.RegisterServer

6. Open `frontend/index.html` in a browser.
7. For an admin account: register normally, then run
   `UPDATE users SET role = 'ADMIN' WHERE email = 'your@email';`
   and open `frontend/admin.html`.

## Project structure
- `frontend/` pages and scripts (`script.js`, `admin.js`)
- `backend/src/skillsphere/` DAO classes (database), handler classes (HTTP endpoints), models
- `database/schema.sql` full database definition

## Known simplifications
- The server trusts the user id sent by the browser (no session tokens).
- Credits per session are fixed at 10.
- Completed-session credits are not refunded if an admin deletes the skill later.