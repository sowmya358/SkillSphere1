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