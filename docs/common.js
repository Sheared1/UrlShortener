//Function to inject CSS styles
function addNavbarStyles() {
    const styleElement = document.createElement('style');
    styleElement.textContent = `

        .email-verification-banner {
            width: 800px;
            position: relative;
            left: 50%;
            transform: translateX(-50%);
            background-color: #fff3cd;
            color: #856404;
            padding: 10px;
            margin: 0 0 20px;
            border: 1px solid #ffeeba;
            border-radius: 4px;
            display: none;
        }
        .resend-button {
            background-color: #007bff;
            color: white;
            border: none;
            padding: 5px 15px;
            border-radius: 4px;
            cursor: pointer;
            margin-left: 10px;
        }
        .resend-button:hover {
            background-color: #0056b3;
        }

        body {
            font-family: Arial, sans-serif;
            max-width: 1200px;
            margin: 0 auto;  /* Simplified margin */
            padding: 0;      /* Remove padding */
            text-align: center;
        }
        .navbar {
            width: 800px;
            position: relative;
            left: 50%;
            transform: translateX(-50%);
            background-color: #333;
            padding: 15px;
            margin: 40px 0 20px;
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

        #profileLinkNav {
            display: none;
        }

        #myUrlsLink {
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
        <span id="myUrlsLink">
            <a href="myurls-loader.html">My Urls</a>
        </span>
        <a href="#" id="profileLinkNav">Profile</a>
        <a href="/admin.html" id="adminLink" style="display: none;">Admin</a>
        <a href="#" onclick="logout()" id="logoutLink">Logout</a>
    `;

    //Insert the navbar at the start of the body
    document.body.insertBefore(navbar, document.body.firstChild);
}

async function getUserRoles() {
    const token = localStorage.getItem('jwtToken');
    if (!token) return [];
    try {
        const response = await fetch('https://sheared1.github.io/api/users/get-user-roles', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) return [];
        const data = await response.json();
        let roles = [];
        if (Array.isArray(data.roles)) {
            roles = data.roles;
        } else if (typeof data.roles === 'string') {
            roles = [data.roles];
        } else if (data.roles && typeof data.roles === 'object') {
            // fallback for object/map style
            roles = Object.keys(data.roles).filter(key => data.roles[key]);
        }
        // Ensure all roles start with ROLE_
        return roles.map(role => role.startsWith('ROLE_') ? role : 'ROLE_' + role);
    } catch (e) {
        console.error('Error fetching roles:', e);
        return [];
    }
}

function updateNavbar() {
    const token = localStorage.getItem('jwtToken');
    const authLinks = document.getElementById('authLinks');
    const logoutLink = document.getElementById('logoutLink');
    const myUrlsLink = document.getElementById('myUrlsLink');

    if (token) {
        authLinks.style.display = 'none';
        logoutLink.style.display = 'inline';
        myUrlsLink.style.display = 'inline';
        if (profileLinkNav) profileLinkNav.style.display = 'inline';
    } else {
        authLinks.style.display = 'inline';
        logoutLink.style.display = 'none';
        myUrlsLink.style.display = 'none';
        if (profileLinkNav) profileLinkNav.style.display = 'none';
    }
}

function logout() {
    localStorage.removeItem('jwtToken');
    window.location.href = '/login.html';
}

function createVerificationBanner() {
    const banner = document.createElement('div');
    banner.className = 'email-verification-banner';
    banner.id = 'emailVerificationBanner';
    banner.innerHTML = `
        <span>Please verify your email</span>
        <button class="resend-button" onclick="resendVerificationEmail()">Resend verification email</button>
        <span id="emailSentMessage" style="display: none; margin-top: 10px; color: #28a745;">Email has been sent.</span>
    `;

    // Insert after navbar
    const navbar = document.querySelector('.navbar');
    navbar.parentNode.insertBefore(banner, navbar.nextSibling);

    return banner;
}

async function checkEmailVerificationStatus() {
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch('https://sheared1.github.io/api/users/email-verification-status', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const data = await response.json();

        let banner = document.getElementById('emailVerificationBanner');

        if (!data.emailVerified) {
            if (!banner) {
                banner = createVerificationBanner();
            }
            banner.style.display = 'block';
        } else if (banner) {
            banner.style.display = 'none';
        }

    } catch (error) {
        console.error('Error checking email verification status:', error);
    }
}

async function resendVerificationEmail() {
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch('https://sheared1.github.io/api/email/resend-verification-email', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const messageElement = document.getElementById('emailSentMessage');

        //Handle rate limiting
        if (response.status === 429) {
            messageElement.style.color = '#dc3545'; // Red color for error
            messageElement.textContent = 'Rate limit exceeded. Try again later.';
            messageElement.style.display = 'block';

            setTimeout(() => {
                messageElement.style.display = 'none';
                messageElement.style.color = '#28a745'; // Reset color back to success green
                messageElement.textContent = 'Email has been sent.'; // Reset text
            }, 3000);
            return;
        }

        else if (response.status === 400) {
            messageElement.style.color = '#dc3545'; // Red color for error
            messageElement.textContent = 'Email is already verified. Please refresh the page.';
            messageElement.style.display = 'block';

            setTimeout(() => {
                messageElement.style.display = 'none';
                messageElement.style.color = '#28a745'; // Reset color back to success green
                messageElement.textContent = 'Email has been sent.'; // Reset text
            }, 3000);
            return;
        }

        else if (response.ok) {
            messageElement.style.display = 'block';

            // Hide the message after 3 seconds
            setTimeout(() => {
                messageElement.style.display = 'none';
            }, 3000);
        }
    } catch (error) {
        console.error('Error resending verification email:', error);
    }
}

function handleProfileLinkClick(event) {
    event.preventDefault();
    const token = localStorage.getItem('jwtToken');

    if (!token) {
        alert('You need to be logged in to access your profile.');
        window.location.href = '/login.html';
        return;
    }

    fetch('/api/users/profile', {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            // Try to get more specific error from response body if available
            return response.json().then(err => {
                 throw new Error(err.message || 'Failed to check email verification status. Status: ' + response.status);
            }).catch(() => { // Fallback if response body is not JSON or empty
                 throw new Error('Failed to check email verification status. Status: ' + response.status);
            });
        }
        return response.json();
    })
    .then(data => {
        if (data.emailVerified) {
            window.location.href = 'profile.html'; // Redirect to profile page
        } else {
            alert('Please verify your email before accessing your profile.');
        }
    })
    .catch(error => {
        console.error('Error checking email verification for profile access:', error);
        alert('An error occurred while checking your email verification status: ' + error.message);
    });
}



//Wait for the DOM to be fully loaded before creating and updating the navbar
document.addEventListener('DOMContentLoaded', async () => {
    addNavbarStyles();  //Injecting styles
    createNavbar();
    updateNavbar();
    checkEmailVerificationStatus();

    const profileLinkElement = document.getElementById('profileLinkNav');
    if (profileLinkElement) {
        profileLinkElement.addEventListener('click', handleProfileLinkClick);
    } else {
        console.warn('Profile link element (profileLinkNav) not found.');
    }

    //Show admin link if user has ROLE_ADMIN
    const adminLink = document.getElementById('adminLink');
    if (adminLink) {
        const roles = await getUserRoles();
        if (roles.includes('ROLE_ADMIN')) {
            adminLink.style.display = 'inline';
        } else {
            adminLink.style.display = 'none';
        }
    }

});