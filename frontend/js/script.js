// ==========================================================
// SkillSphere - Basic JavaScript
// Step 1: No real backend/auth yet. This just gives a smooth
// front-end experience (form handling, logout redirect).
// ==========================================================

// Handle Login form submission (placeholder only)
const loginForm = document.getElementById("loginForm");
if (loginForm) {
    loginForm.addEventListener("submit", function (e) {
        e.preventDefault(); // stop the page from reloading
        // In a later step, this will send data to the backend for real authentication.
        alert("Login functionality will be added in a later development step.");
        window.location.href = "dashboard.html"; // temporarily go to dashboard
    });
}

// Handle Register form submission (placeholder only)
const registerForm = document.getElementById("registerForm");
if (registerForm) {
    registerForm.addEventListener("submit", async function (e) {
        e.preventDefault();

        const fullname = document.getElementById("fullname").value;
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        if (password !== confirmPassword) {
            alert("Passwords do not match. Please try again.");
            return;
        }

        // Send the data to the Java backend running on localhost:8080
        try {
            const response = await fetch("http://localhost:8080/api/register", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ fullname, email, password })
            });

            const resultText = await response.text();

            if (response.ok) {
                alert(resultText);
                window.location.href = "login.html";
            } else {
                alert(resultText);
            }
        } catch (error) {
            alert("Could not reach the server. Make sure RegisterServer.java is running.");
        }
    });
}

// Handle Logout button on dashboard (placeholder only)
const logoutBtn = document.getElementById("logoutBtn");
if (logoutBtn) {
    logoutBtn.addEventListener("click", function () {
        // In a later step, this will clear the user session.
        // For now it just links back to index.html via the href in the HTML.
    });
}
