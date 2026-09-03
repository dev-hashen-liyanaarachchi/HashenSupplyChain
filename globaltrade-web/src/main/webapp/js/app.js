document.addEventListener('DOMContentLoaded', () => {
    const loginTab = document.getElementById('loginTab');
    const registerTab = document.getElementById('registerTab');
    const vendorTab = document.getElementById('vendorTab');

    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const vendorForm = document.getElementById('vendorForm');

    const authTabs = document.getElementById('authTabs');
    const appContainer = document.getElementById('appContainer');
    const alertBox = document.getElementById('alertBox');

    // Auto detect API Base URL
    const getApiBaseUrl = () => {
        const path = window.location.pathname;
        if (path.includes('/globaltrade-web')) {
            return '/globaltrade-web/api';
        }
        return '/api';
    };

    const API_BASE = getApiBaseUrl();

    // Tab Switching
    loginTab?.addEventListener('click', () => switchTab('login'));
    registerTab?.addEventListener('click', () => switchTab('register'));
    vendorTab?.addEventListener('click', () => switchTab('vendor'));

    function switchTab(tab) {
        hideAlert();
        appContainer.classList.remove('wide');

        loginTab.classList.toggle('active', tab === 'login');
        registerTab.classList.toggle('active', tab === 'register');
        vendorTab.classList.toggle('active', tab === 'vendor');

        loginForm.style.display = tab === 'login' ? 'block' : 'none';
        registerForm.style.display = tab === 'register' ? 'block' : 'none';
        vendorForm.style.display = tab === 'vendor' ? 'block' : 'none';
    }

    // Helper: Show Alert
    function showAlert(message, type = 'error') {
        alertBox.className = `alert alert-${type}`;
        alertBox.innerHTML = `
            <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="${type === 'error' ? 'M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z' : 'M5 13l4 4L19 7'}"/>
            </svg>
            <span>${message}</span>
        `;
        alertBox.style.display = 'flex';
    }

    function hideAlert() {
        alertBox.style.display = 'none';
    }

    function setLoading(btn, isLoading) {
        const spinner = btn.querySelector('.spinner');
        const text = btn.querySelector('.btn-text');
        if (isLoading) {
            btn.disabled = true;
            if (spinner) spinner.style.display = 'inline-block';
            if (text) text.style.opacity = '0.6';
        } else {
            btn.disabled = false;
            if (spinner) spinner.style.display = 'none';
            if (text) text.style.opacity = '1';
        }
    }

    // Session Storage
    function saveSession(data) {
        localStorage.setItem('gt_access_token', data.accessToken || data.token || '');
        localStorage.setItem('gt_refresh_token', data.refreshToken || '');
        localStorage.setItem('gt_username', data.username || '');
        const roles = data.roles || (data.role ? [data.role] : ['CUSTOMER']);
        localStorage.setItem('gt_roles', JSON.stringify(Array.isArray(roles) ? roles : [roles]));
    }

    // Redirect to Role Dedicated Portal Page
    function redirectToRolePortal(role) {
        const r = (role || '').toUpperCase();
        if (r === 'ADMIN' || r === 'SYSTEM_ADMIN') {
            window.location.href = 'admin.html';
        } else if (r === 'VENDOR' || r === 'VENDOR_REP') {
            window.location.href = 'vendor.html';
        } else if (r === 'WAREHOUSE_MANAGER') {
            window.location.href = 'warehouse.html';
        } else if (r === 'LOGISTICS_OFFICER' || r === 'LOGISTICS_COORDINATOR') {
            window.location.href = 'logistics.html';
        } else if (r === 'CUSTOMS_OFFICER' || r === 'CUSTOMS_AGENT') {
            window.location.href = 'customs.html';
        } else if (r === 'FINANCE_OFFICER') {
            window.location.href = 'finance.html';
        } else {
            window.location.href = 'customer.html';
        }
    }

    // 1. LOGIN FORM SUBMIT
    loginForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const btn = loginForm.querySelector('button[type="submit"]');
        const username = document.getElementById('loginUsername').value.trim();
        const password = document.getElementById('loginPassword').value.trim();

        if (!username || !password) {
            showAlert('Please enter both username and password.');
            return;
        }

        setLoading(btn, true);

        try {
            const response = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username, password})
            });

            const data = await response.json();

            if (response.ok) {
                saveSession(data);
                const rawRoles = data.roles || ['CUSTOMER'];
                const userRole = Array.isArray(rawRoles) ? (rawRoles[0] || 'CUSTOMER') : rawRoles;
                showAlert('Login successful! Loading dedicated portal...', 'success');
                setTimeout(() => redirectToRolePortal(userRole), 800);
            } else {
                showAlert(data.message || data.error || 'Authentication failed. Please check credentials.');
            }
        } catch (err) {
            showAlert('Network or Server Connection Error. Make sure GlassFish is running.');
            console.error('Login error:', err);
        } finally {
            setLoading(btn, false);
        }
    });

    // 2. USER SIGN-UP FORM SUBMIT
    registerForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const btn = registerForm.querySelector('button[type="submit"]');
        const username = document.getElementById('regUsername').value.trim();
        const email = document.getElementById('regEmail').value.trim();
        const password = document.getElementById('regPassword').value.trim();
        const role = document.getElementById('regRole').value;

        if (!username || !email || !password) {
            showAlert('Please fill in all required fields.');
            return;
        }

        setLoading(btn, true);

        try {
            const response = await fetch(`${API_BASE}/auth/register`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username, email, password, role})
            });

            const data = await response.json();

            if (response.ok || response.status === 201) {
                saveSession(data);
                showAlert(`Account created for ${role}! Loading dedicated portal...`, 'success');
                setTimeout(() => redirectToRolePortal(role), 800);
            } else {
                showAlert(data.error || data.message || 'Registration failed.');
            }
        } catch (err) {
            showAlert('Network or Server Error during registration.');
            console.error('Registration error:', err);
        } finally {
            setLoading(btn, false);
        }
    });

    // 3. VENDOR REGISTER FORM SUBMIT
    vendorForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const btn = vendorForm.querySelector('button[type="submit"]');
        const companyName = document.getElementById('venCompanyName').value.trim();
        const username = document.getElementById('venUsername').value.trim();
        const taxId = document.getElementById('venTaxId').value.trim();
        const email = document.getElementById('venEmail').value.trim();
        const password = document.getElementById('venPassword').value.trim();

        if (!companyName || !username || !taxId || !email || !password) {
            showAlert('Please fill in all vendor registration details.');
            return;
        }

        setLoading(btn, true);

        try {
            const response = await fetch(`${API_BASE}/auth/register`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    username: username,
                    email: email,
                    password: password,
                    role: 'VENDOR'
                })
            });

            const data = await response.json();

            if (response.ok || response.status === 201) {
                localStorage.setItem('gt_vendor_company', companyName);
                localStorage.setItem('gt_vendor_taxid', taxId);
                saveSession(data);
                showAlert(`Vendor "${companyName}" registered! Redirecting to Vendor Portal...`, 'success');
                setTimeout(() => window.location.href = 'vendor.html', 800);
            } else {
                showAlert(data.error || 'Vendor registration failed.');
            }
        } catch (err) {
            showAlert('Network Error during vendor registration.');
        } finally {
            setLoading(btn, false);
        }
    });
});
