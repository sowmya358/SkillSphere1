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
    registerForm.addEventListener("submit", function (e) {
        e.preventDefault();

        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        // Simple client-side check just for user experience, not real validation
        if (password !== confirmPassword) {
            alert("Passwords do not match. Please try again.");
            return;
        }

        // In a later step, this will send data to the backend to create an account.
        alert("Registration functionality will be added in a later development step.");
        window.location.href = "login.html";
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
