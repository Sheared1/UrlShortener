// Function to inject CSS styles
function addNavbarStyles() {
    const styleElement = document.createElement('style');
    styleElement.textContent = `
        .navbar {
            background-color: #333;
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 8px;
            text-align: center;
        }

        .navbar a {
            color: white;
            text-decoration: none;
            padding: 10px 20px;
            margin: 0 5px;
            border-radius: 4px;
        }

        .navbar a:hover {
            background-color: #555;
        }

        #authLinks {
            display: inline;
        }

        #logoutLink {
            display: none;
        }
    `;
    document.head.appendChild(styleElement);
}

function createNavbar() {
    const navbar = document.createElement('div');
    navbar.className = 'navbar';
    navbar.innerHTML = `
        <a href="index.html">Home</a>
        <span id="authLinks">
            <a href="login.html">Login</a>
            <a href="register.html">Register</a>
        </span>
        <a href="#" onclick="logout()" id="logoutLink">Logout</a>
    `;

    //Insert the navbar at the start of the body
    document.body.insertBefore(navbar, document.body.firstChild);
}

function updateNavbar() {
    const token = localStorage.getItem('jwtToken');
    const authLinks = document.getElementById('authLinks');
    const logoutLink = document.getElementById('logoutLink');

    if (token) {
        authLinks.style.display = 'none';
        logoutLink.style.display = 'inline';
    } else {
        authLinks.style.display = 'inline';
        logoutLink.style.display = 'none';
    }
}

function logout() {
    localStorage.removeItem('jwtToken');
    window.location.href = '/login.html';
}

// Wait for the DOM to be fully loaded before creating and updating the navbar
document.addEventListener('DOMContentLoaded', () => {
    addNavbarStyles();  // Add this line to inject the styles
    createNavbar();
    updateNavbar();
});
