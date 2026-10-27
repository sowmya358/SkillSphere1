# SkillSphere
Learn • Share • Connect — a skill-exchange web app for students.

## Tech
HTML/CSS/JS frontend, plain Java (HttpServer + JDBC) backend, MySQL database.

## Setup
1. Run `database/skillsphere.sql` in MySQL.
2. Download the MySQL Connector/J jar into `backend/lib/`.
3. Set your MySQL password in `backend/src/skillsphere/DBConnection.java`.
4. From `backend/` (PowerShell):
   javac -encoding UTF-8 -cp "lib/<jar>.jar" -d out src/skillsphere/*.java
   java -cp "out;lib/<jar>.jar" skillsphere.RegisterServer
5. Open `frontend/index.html` in a browser.