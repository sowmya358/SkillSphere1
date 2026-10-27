const API_URL = "http://localhost:8080";  

const loginBox = document.getElementById("adminLoginBox");
const panel = document.getElementById("adminPanel");

function getAdmin() {
    try {
        const saved = sessionStorage.getItem("admin");
        return saved ? JSON.parse(saved) : null;
    } catch (e) {
        return null;
    }
}

async function post(path, data) {
    return fetch(API_URL + path, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams(data)
    });
}

function logout() {
    sessionStorage.removeItem("admin");
    location.reload();
}

// Builds one table row; every cell uses textContent so stored text can't inject HTML
function addRow(tbody, values, button) {
    const tr = document.createElement("tr");
    values.forEach(function (v) {
        const td = document.createElement("td");
        td.textContent = v;
        tr.appendChild(td);
    });
    const last = document.createElement("td");
    if (button) last.appendChild(button);
    if (button !== undefined) tr.appendChild(last);
    tbody.appendChild(tr);
}

async function loadUsers() {
    const admin = getAdmin();
    const response = await post("/api/admin/users", { adminId: admin.userId });
    if (response.status === 403) { logout(); return; }
    if (!response.ok) { alert(await response.text()); return; }

    const users = await response.json();
    const body = document.getElementById("usersBody");
    body.innerHTML = "";
    users.forEach(function (u) {
        addRow(body, [u.userId, u.name, u.email, u.credits, u.role, u.skillCount]);
    });
}

async function loadSkills() {
    const admin = getAdmin();
    const response = await post("/api/admin/skills", { adminId: admin.userId });
    if (response.status === 403) { logout(); return; }
    if (!response.ok) { alert(await response.text()); return; }

    const skills = await response.json();
    const body = document.getElementById("skillsBody");
    body.innerHTML = "";
    skills.forEach(function (s) {
        const btn = document.createElement("button");
        btn.textContent = "Delete";
        btn.className = "btn btn-secondary";
        btn.addEventListener("click", function () { removeSkill(s); });
        addRow(body, [s.skillId, s.name, s.category, s.description, s.teacherName], btn);
    });
}

async function removeSkill(skill) {
    if (!confirm("Remove the skill \"" + skill.name + "\" by " + skill.teacherName
            + "?\nThis also deletes all requests and reviews linked to it.")) return;
    try {
        const response = await post("/api/admin/skills/delete",
                { adminId: getAdmin().userId, skillId: skill.skillId });
        if (!response.ok) alert(await response.text());
        loadSkills();
        loadUsers();
    } catch (error) {
        alert("Could not reach the server. Make sure RegisterServer is running.");
    }
}

async function showPanel() {
    const admin = getAdmin();
    loginBox.style.display = "none";
    panel.style.display = "block";
    document.getElementById("adminName").textContent = admin.name;
    try {
        await loadUsers();
        await loadSkills();
    } catch (error) {
        alert("Could not reach the server. Make sure RegisterServer is running.");
    }
}

document.getElementById("adminLogout").addEventListener("click", logout);

document.getElementById("adminLoginForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    const submitBtn = e.target.querySelector("button[type='submit']");
    submitBtn.disabled = true;
    try {
        const response = await post("/api/admin/login", {
            email: document.getElementById("adminEmail").value.trim(),
            password: document.getElementById("adminPassword").value
        });
        if (!response.ok) {
            alert(await response.text());
            return;
        }
        sessionStorage.setItem("admin", JSON.stringify(await response.json()));
        showPanel();
    } catch (error) {
        alert("Could not reach the server. Make sure RegisterServer is running.");
    } finally {
        submitBtn.disabled = false;
    }
});

if (getAdmin()) showPanel();