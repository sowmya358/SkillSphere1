// ==========================================================
// SkillSphere - JavaScript
// Register and Login are connected to the Java backend (localhost:8080).
// The logged-in user is remembered in sessionStorage until logout
// or until the browser tab is closed.
// ==========================================================

const API_URL = "http://localhost:8080";

// ---------- Login (REAL) ----------
const loginForm = document.getElementById("loginForm");
if (loginForm) {
    loginForm.addEventListener("submit", async function (e) {
        e.preventDefault();

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        // Disable the button so the form is not submitted twice
        const submitBtn = loginForm.querySelector("button[type='submit']");
        submitBtn.disabled = true;

        try {
            const response = await fetch(API_URL + "/api/login", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ email, password })
            });

            if (response.ok) {
                // Backend returns JSON: { userId, name, email, credits }
                const user = await response.json();
                sessionStorage.setItem("user", JSON.stringify(user));
                window.location.href = "dashboard.html";
            } else {
                // 401 = wrong email/password, 500 = server/database problem
                alert(await response.text());
            }
        } catch (error) {
            alert("Could not reach the server. Make sure RegisterServer is running.");
        } finally {
            submitBtn.disabled = false;
        }
    });
}

// ---------- Register (REAL) ----------
const registerForm = document.getElementById("registerForm");
if (registerForm) {
    registerForm.addEventListener("submit", async function (e) {
        e.preventDefault();

        const fullname = document.getElementById("fullname").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        if (password !== confirmPassword) {
            alert("Passwords do not match. Please try again.");
            return;
        }

        const submitBtn = registerForm.querySelector("button[type='submit']");
        submitBtn.disabled = true;

        try {
            const response = await fetch(API_URL + "/api/register", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ fullname, email, password })
            });

            const resultText = await response.text();
            alert(resultText);

            if (response.ok) {
                window.location.href = "login.html";
            }
        } catch (error) {
            alert("Could not reach the server. Make sure RegisterServer is running.");
        } finally {
            submitBtn.disabled = false;
        }
    });
}

// ---------- Dashboard: show the real logged-in user ----------
const welcomeName = document.getElementById("welcomeName");
if (welcomeName) {
    const savedUser = sessionStorage.getItem("user");

    if (!savedUser) {
        // Nobody is logged in, so send them to the login page
        window.location.href = "login.html";
    } else {
        const user = JSON.parse(savedUser);
        // textContent (not innerHTML) so a name can never inject HTML
        welcomeName.textContent = user.name;
        document.getElementById("profileName").textContent = user.name;
        document.getElementById("profileEmail").textContent = user.email;
        document.getElementById("profileCredits").textContent = user.credits;
    }
}

// ---------- Logout: clear the saved user ----------
const logoutBtn = document.getElementById("logoutBtn");
if (logoutBtn) {
    logoutBtn.addEventListener("click", function () {
        sessionStorage.removeItem("user");
        // The link's href="index.html" then takes the user home
    });
}
// ---------- Skills: list, add, edit, delete ----------
const skillList = document.getElementById("skillList");
const skillForm = document.getElementById("skillForm");

if (skillList && skillForm) {
    const savedUser = sessionStorage.getItem("user");
    const currentUser = savedUser ? JSON.parse(savedUser) : null;

    // Small helper: POST form data to the backend
    async function postForm(path, data) {
        return fetch(API_URL + path, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams(data)
        });
    }

    async function loadSkills() {
        try {
            const response = await postForm("/api/skills/list", { userId: currentUser.userId });
            if (!response.ok) {
                alert(await response.text());
                return;
            }
            const skills = await response.json();
            skillList.innerHTML = "";

            if (skills.length === 0) {
                const li = document.createElement("li");
                li.textContent = "No skills added yet.";
                skillList.appendChild(li);
                return;
            }

            skills.forEach(function (skill) {
                const li = document.createElement("li");
                // textContent, not innerHTML, so skill names can never inject HTML
                let label = skill.name;
                if (skill.category) label += " (" + skill.category + ")";
                li.textContent = label + " ";

                const editBtn = document.createElement("button");
                editBtn.textContent = "Edit";
                editBtn.className = "btn btn-secondary";
                editBtn.addEventListener("click", function () { editSkill(skill); });

                const deleteBtn = document.createElement("button");
                deleteBtn.textContent = "Delete";
                deleteBtn.className = "btn btn-secondary";
                deleteBtn.addEventListener("click", function () { deleteSkill(skill); });

                li.appendChild(editBtn);
                li.appendChild(deleteBtn);
                skillList.appendChild(li);
            });
        } catch (error) {
            alert("Could not reach the server. Make sure RegisterServer is running.");
        }
    }

    async function editSkill(skill) {
        const name = prompt("Skill name:", skill.name);
        if (name === null) return; // cancelled
        const category = prompt("Category:", skill.category);
        if (category === null) return;
        const description = prompt("Description:", skill.description);
        if (description === null) return;

        try {
            const response = await postForm("/api/skills/update", {
                userId: currentUser.userId, skillId: skill.skillId,
                name, category, description
            });
            alert(await response.text());
            if (response.ok) loadSkills();
        } catch (error) {
            alert("Could not reach the server. Make sure RegisterServer is running.");
        }
    }

    async function deleteSkill(skill) {
        if (!confirm("Delete the skill \"" + skill.name + "\"?")) return;
        try {
            const response = await postForm("/api/skills/delete", {
                userId: currentUser.userId, skillId: skill.skillId
            });
            if (response.ok) {
                loadSkills();
            } else {
                alert(await response.text());
            }
        } catch (error) {
            alert("Could not reach the server. Make sure RegisterServer is running.");
        }
    }

    skillForm.addEventListener("submit", async function (e) {
        e.preventDefault();

        const submitBtn = skillForm.querySelector("button[type='submit']");
        submitBtn.disabled = true;

        try {
            const response = await postForm("/api/skills/add", {
                userId: currentUser.userId,
                name: document.getElementById("skillName").value.trim(),
                category: document.getElementById("skillCategory").value.trim(),
                description: document.getElementById("skillDescription").value.trim()
            });

            if (response.ok) {
                skillForm.reset();
                loadSkills();
            } else {
                alert(await response.text());
            }
        } catch (error) {
            alert("Could not reach the server. Make sure RegisterServer is running.");
        } finally {
            submitBtn.disabled = false;
        }
    });

    // The "show real user" block above already redirects to login if nobody
    // is logged in, so only load skills when we have a user.
    if (currentUser) loadSkills();
}