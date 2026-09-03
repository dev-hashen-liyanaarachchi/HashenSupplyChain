// Global Tab Switching Helper Function (Callable from inline HTML or JS)
window.switchAuthTab = function(tab) {
    const alertBox = document.getElementById('alertBox');
    if (alertBox) {
        alertBox.classList.add('hidden');
        alertBox.style.display = 'none';
    }

    const loginTab = document.getElementById('loginTab');
    const registerTab = document.getElementById('registerTab');
    const vendorTab = document.getElementById('vendorTab');

    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const vendorForm = document.getElementById('vendorForm');

    const activeTabClass = "px-4 py-2 rounded-xl text-xs font-bold transition-all bg-gradient-to-r from-amber-500 to-orange-500 text-white shadow-md shadow-orange-500/25";
    const inactiveTabClass = "px-4 py-2 rounded-xl text-xs font-bold transition-all bg-slate-100 text-slate-700 hover:bg-slate-200";

    if (loginTab) loginTab.className = tab === 'login' ? activeTabClass : inactiveTabClass;
    if (registerTab) registerTab.className = tab === 'register' ? activeTabClass : inactiveTabClass;
    if (vendorTab) vendorTab.className = tab === 'vendor' ? "px-4 py-2 rounded-xl text-xs font-bold transition-all bg-gradient-to-r from-teal-600 to-emerald-600 text-white shadow-md shadow-teal-500/25" : inactiveTabClass;

    if (loginForm) {
        if (tab === 'login') {
            loginForm.classList.remove('hidden');
            loginForm.style.display = 'block';
        } else {
            loginForm.classList.add('hidden');
            loginForm.style.display = 'none';
        }
    }
    if (registerForm) {
        if (tab === 'register') {
            registerForm.classList.remove('hidden');
            registerForm.style.display = 'block';
        } else {
            registerForm.classList.add('hidden');
            registerForm.style.display = 'none';
        }
    }
    if (vendorForm) {
        if (tab === 'vendor') {
            vendorForm.classList.remove('hidden');
            vendorForm.style.display = 'block';
        } else {
            vendorForm.classList.add('hidden');
            vendorForm.style.display = 'none';
        }
    }
};

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

    // Tab Event Listeners
    loginTab?.addEventListener('click', () => window.switchAuthTab('login'));
    registerTab?.addEventListener('click', () => window.switchAuthTab('register'));
    vendorTab?.addEventListener('click', () => window.switchAuthTab('vendor'));

    // Helper: Show Alert
    function showAlert(message, type = 'error') {
        if (!alertBox) return;
        alertBox.className = `p-4 rounded-xl border text-sm font-medium shadow-sm transition-all flex items-center gap-2 ${
            type === 'error' ? 'bg-rose-50 border-rose-200 text-rose-700' : 'bg-emerald-50 border-emerald-200 text-emerald-700'
        }`;
        alertBox.innerHTML = `
            <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="${type === 'error' ? 'M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z' : 'M5 13l4 4L19 7'}"/>
            </svg>
            <span>${message}</span>
        `;
        alertBox.classList.remove('hidden');
        alertBox.style.display = 'flex';
    }

    function hideAlert() {
        if (!alertBox) return;
        alertBox.classList.add('hidden');
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

    // Password Visibility Toggle Helper
    window.togglePasswordVisibility = function(inputId, btn) {
        const input = document.getElementById(inputId);
        if (input) {
            if (input.type === 'password') {
                input.type = 'text';
                btn.textContent = '🙈';
            } else {
                input.type = 'password';
                btn.textContent = '👁️';
            }
        }
    };

    // 2. USER SIGN-UP FORM SUBMIT
    registerForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const btn = registerForm.querySelector('button[type="submit"]');
        const fullName = document.getElementById('regFullName')?.value.trim();
        const username = document.getElementById('regUsername')?.value.trim();
        const email = document.getElementById('regEmail')?.value.trim();
        const phone = document.getElementById('regPhone')?.value.trim();
        const country = document.getElementById('regCountry')?.value;
        const password = document.getElementById('regPassword')?.value.trim();
        const confirmPassword = document.getElementById('regConfirmPassword')?.value.trim();
        const role = document.getElementById('regRole')?.value;

        if (!fullName || !username || !email || !phone || !password) {
            showAlert('Please fill in all required registration fields.');
            return;
        }

        if (password !== confirmPassword) {
            showAlert('Passwords do not match. Please verify your password entry.');
            return;
        }

        setLoading(btn, true);

        try {
            const response = await fetch(`${API_BASE}/auth/register`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username, email, password, role, fullName, phone, country})
            });

            const data = await response.json();

            if (response.ok || response.status === 201) {
                saveSession(data);
                showAlert(`Account created successfully for ${fullName || username}! Loading dedicated portal...`, 'success');
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

    // 3. VENDOR REGISTER & ONBOARDING FORM SUBMIT
    vendorForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const btn = vendorForm.querySelector('button[type="submit"]');
        const companyName = document.getElementById('venCompanyName')?.value.trim();
        const taxId = document.getElementById('venTaxId')?.value.trim();
        const category = document.getElementById('venCategory')?.value;
        const email = document.getElementById('venEmail')?.value.trim();
        const phone = document.getElementById('venPhone')?.value.trim();
        const country = document.getElementById('venCountry')?.value;
        const streetAddress = document.getElementById('venStreetAddress')?.value.trim();
        const username = document.getElementById('venUsername')?.value.trim();
        const password = document.getElementById('venPassword')?.value.trim();

        if (!companyName || !taxId || !email || !phone || !streetAddress || !username || !password) {
            showAlert('Please fill in all corporate vendor onboarding details.');
            return;
        }

        setLoading(btn, true);

        try {
            const response = await fetch(`${API_BASE}/auth/vendor-onboard`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    companyName,
                    taxId,
                    email,
                    phone,
                    country,
                    streetAddress,
                    businessCategory: category,
                    username,
                    password
                })
            });

            const data = await response.json();

            if (response.ok || response.status === 201) {
                localStorage.setItem('gt_vendor_company', companyName);
                localStorage.setItem('gt_vendor_taxid', taxId);
                saveSession(data);
                showAlert(`Corporate Vendor "${companyName}" Onboarded Successfully! Redirecting to Vendor Portal...`, 'success');
                setTimeout(() => window.location.href = 'vendor.html', 800);
            } else {
                showAlert(data.error || 'Vendor onboarding failed.');
            }
        } catch (err) {
            showAlert('Network Error during vendor onboarding.');
            console.error('Vendor onboarding error:', err);
        } finally {
            setLoading(btn, false);
        }
    });
});
