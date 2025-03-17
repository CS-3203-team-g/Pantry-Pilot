// Helper function to get a cookie's value by name
function getCookie(name) {
    const nameEQ = name + "=";
    const ca = document.cookie.split(";");
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === " ") c = c.substring(1);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

// Show user login status in the navbar
document.addEventListener('DOMContentLoaded', function() {
    // Set up user login status
    const username = getCookie("username");
    if (username) {
        // Replace the login button with a dropdown showing the username
        const loginBtn = document.getElementById("loginBtn");
        if (loginBtn) {
            const dropdownDiv = document.createElement("div");
            dropdownDiv.className = "dropdown ms-3";
            dropdownDiv.innerHTML =
                '<button class="btn btn-primary dropdown-toggle" type="button" id="userDropdown" data-bs-toggle="dropdown" aria-expanded="false">' +
                username +
                '</button>' +
                '<ul class="dropdown-menu dropdown-menu-end" aria-labelledby="userDropdown">' +
                '<li><a class="dropdown-item" href="/settings">Settings</a></li>' +
                '<li><a class="dropdown-item" href="/logout">Sign Out</a></li>' +
                "</ul>";
            loginBtn.parentNode.replaceChild(dropdownDiv, loginBtn);
        }
    }
});