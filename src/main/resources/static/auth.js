function getAuthenticatedHeaders() {
    const token = localStorage.getItem('jwtToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

async function makeAuthenticatedRequest(url, options = {}) {
    const headers = getAuthenticatedHeaders();
    options.headers = { ...headers, ...options.headers };

    const response = await fetch(url, options);

    if (response.status === 401) {
        //If token is expired or invalid
        localStorage.removeItem('jwtToken');
        window.location.href = '/login.html';
        return null;
    }

    return response;
}
