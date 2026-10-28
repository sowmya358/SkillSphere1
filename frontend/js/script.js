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
// ---------- Find Skills: search + rule-based ranking ----------
const searchForm = document.getElementById("searchForm");
const searchResults = document.getElementById("searchResults");

if (searchForm && searchResults) {
    const savedSearchUser = sessionStorage.getItem("user");
    const searchUser = savedSearchUser ? JSON.parse(savedSearchUser) : null;

    searchForm.addEventListener("submit", async function (e) {
        e.preventDefault();
        if (!searchUser) return;

        const submitBtn = searchForm.querySelector("button[type='submit']");
        submitBtn.disabled = true;

        try {
            const response = await fetch(API_URL + "/api/skills/search", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({
                    userId: searchUser.userId,
                    keyword: document.getElementById("searchKeyword").value.trim()
                })
            });

            if (!response.ok) {
                alert(await response.text());
                return;
            }

            const results = await response.json();
            searchResults.innerHTML = "";

            if (results.length === 0) {
                const li = document.createElement("li");
                li.textContent = "No matching skills found.";
                searchResults.appendChild(li);
                return;
            }

                results.forEach(function (r) {
                const li = document.createElement("li");
                // textContent keeps user-entered text from injecting HTML
                let text = r.name;
                if (r.category) text += " (" + r.category + ")";
                text += " - taught by " + r.teacherName;
                text += r.rating === null ? " - no ratings yet" : " - rating " + r.rating + "/5";
                if (r.match) text += " [" + r.match + "]";
                li.textContent = text + " ";

                const requestBtn = document.createElement("button");
                requestBtn.textContent = "Request";
                requestBtn.className = "btn btn-secondary";
                requestBtn.addEventListener("click", function () { sendRequest(r.skillId); });

                li.appendChild(requestBtn);
                searchResults.appendChild(li);
            });
        } catch (error) {
            alert("Could not reach the server. Make sure RegisterServer is running.");
        } finally {
            submitBtn.disabled = false;
        }
    });
}
// ---------- Exchange requests: send, list, accept / reject ----------
const incomingList = document.getElementById("incomingRequests");
const outgoingList = document.getElementById("outgoingRequests");

function getRequestUser() {
    const saved = sessionStorage.getItem("user");
    return saved ? JSON.parse(saved) : null;
}

async function postRequestForm(path, data) {
    return fetch(API_URL + path, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams(data)
    });
}

// Called by the Request button in the search results
async function sendRequest(skillId) {
    const user = getRequestUser();
    if (!user) return;
    try {
        const response = await postRequestForm("/api/requests/create",
                { userId: user.userId, skillId: skillId });
        alert(await response.text());
        if (response.ok) loadRequests();
    } catch (error) {
        alert("Could not reach the server. Make sure RegisterServer is running.");
    }
}

async function answerRequest(requestId, action) {   // action = "accept" or "reject"
    const user = getRequestUser();
    if (!user) return;
    try {
        const response = await postRequestForm("/api/requests/" + action,
                { userId: user.userId, requestId: requestId });
        if (!response.ok) alert(await response.text());
        loadRequests();
    } catch (error) {
        alert("Could not reach the server. Make sure RegisterServer is running.");
    }
}

async function fetchRequests(path) {
    const user = getRequestUser();
    const response = await postRequestForm(path, { userId: user.userId });
    if (!response.ok) {
        alert(await response.text());
        return null;
    }
    return response.json();
}

function renderRequests(listEl, items, isIncoming) {
    listEl.innerHTML = "";
    if (items.length === 0) {
        const li = document.createElement("li");
        li.textContent = "No requests yet.";
        listEl.appendChild(li);
        return;
    }
    items.forEach(function (r) {
        const li = document.createElement("li");
        let text = r.skillName + (isIncoming ? " - requested by " : " - taught by ")
                 + r.otherName + " [" + r.status + "] " + r.createdAt;
        li.textContent = text + " ";

        if (isIncoming && r.status === "PENDING") {
            const acceptBtn = document.createElement("button");
            acceptBtn.textContent = "Accept";
            acceptBtn.className = "btn btn-primary";
            acceptBtn.addEventListener("click", function () { answerRequest(r.requestId, "accept"); });

            const rejectBtn = document.createElement("button");
            rejectBtn.textContent = "Reject";
            rejectBtn.className = "btn btn-secondary";
            rejectBtn.addEventListener("click", function () { answerRequest(r.requestId, "reject"); });

            li.appendChild(acceptBtn);
            li.appendChild(rejectBtn);
        }
                if (isIncoming && r.status === "ACCEPTED") {
            const completeBtn = document.createElement("button");
            completeBtn.textContent = "Mark Completed";
            completeBtn.className = "btn btn-primary";
            completeBtn.addEventListener("click", function () { completeRequest(r.requestId); });
            li.appendChild(completeBtn);
        }
                if (r.status === "COMPLETED") {
            if (r.rating !== null) {
                li.appendChild(document.createTextNode(
                        isIncoming ? "Learner rated you " + r.rating + "/5"
                                   : "You rated " + r.rating + "/5"));
            } else if (!isIncoming) {
                const reviewBtn = document.createElement("button");
                reviewBtn.textContent = "Leave Review";
                reviewBtn.className = "btn btn-primary";
                reviewBtn.addEventListener("click", function () { leaveReview(r.requestId); });
                li.appendChild(reviewBtn);
            }
        }
        listEl.appendChild(li);
    });
}

async function loadRequests() {
    if (!incomingList || !outgoingList || !getRequestUser()) return;
    try {
        const incoming = await fetchRequests("/api/requests/incoming");
        if (incoming) renderRequests(incomingList, incoming, true);
        const outgoing = await fetchRequests("/api/requests/outgoing");
        if (outgoing) renderRequests(outgoingList, outgoing, false);
    } catch (error) {
        alert("Could not reach the server. Make sure RegisterServer is running.");
    }
}

loadRequests();
// Refresh the request lists every 5 seconds so new requests appear without a reload
setInterval(async function () {
    if (!incomingList || !outgoingList || !getRequestUser()) return;
    try {
        const incoming = await fetchRequests("/api/requests/incoming");
        if (incoming) renderRequests(incomingList, incoming, true);
        const outgoing = await fetchRequests("/api/requests/outgoing");
        if (outgoing) renderRequests(outgoingList, outgoing, false);
    } catch (e) {
        // stay quiet if the server is briefly unavailable
    }
}, 5000);
// ---------- Complete a session + keep the credit balance fresh ----------
async function completeRequest(requestId) {
    const user = getRequestUser();
    if (!user) return;
    if (!confirm("Mark this session as completed? Credits will be transferred.")) return;
    try {
        const response = await postRequestForm("/api/requests/complete",
                { userId: user.userId, requestId: requestId });
        alert(await response.text());
        loadRequests();
        refreshCredits();
    } catch (error) {
        alert("Could not reach the server. Make sure RegisterServer is running.");
    }
}

// Asks the server for the real balance, saves it in sessionStorage
// and updates the credits shown on the dashboard.
async function refreshCredits() {
    const user = getRequestUser();
    if (!user) return;
    try {
        const response = await postRequestForm("/api/credits", { userId: user.userId });
        if (!response.ok) return;
        user.credits = parseInt(await response.text(), 10);
        sessionStorage.setItem("user", JSON.stringify(user));

        // CHANGE "userCredits" to the id of the element that shows credits on your dashboard
        const creditsEl = document.getElementById("userCredits");
        if (creditsEl) creditsEl.textContent = user.credits;
    } catch (error) {
        // ignore: the old balance just stays on screen
    }
}

refreshCredits();
// ---------- Reviews ----------
async function leaveReview(requestId) {
    const user = getRequestUser();
    if (!user) return;

    const ratingText = prompt("Rate this session from 1 (poor) to 5 (excellent):");
    if (ratingText === null) return; // cancelled
    const rating = parseInt(ratingText, 10);
    if (isNaN(rating) || rating < 1 || rating > 5) {
        alert("Please enter a whole number from 1 to 5.");
        return;
    }
    const comment = prompt("Optional comment (you can leave this empty):");
    if (comment === null) return;

    try {
        const response = await postRequestForm("/api/reviews/create", {
            userId: user.userId, requestId: requestId,
            rating: rating, comment: comment
        });
        alert(await response.text());
        loadRequests();
    } catch (error) {
        alert("Could not reach the server. Make sure RegisterServer is running.");
    }
}
// ---------- Logout ----------
const logoutLink = document.getElementById("logoutLink");
if (logoutLink) {
    logoutLink.addEventListener("click", function (e) {
        e.preventDefault();
        sessionStorage.removeItem("user");
        window.location.href = "login.html";
    });
}