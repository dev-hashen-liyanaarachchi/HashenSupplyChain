document.addEventListener('DOMContentLoaded', () => {
    const rolesTableBody = document.getElementById('rolesTableBody');
    const permissionsCheckboxList = document.getElementById('permissionsCheckboxList');
    const alertBox = document.getElementById('alertBox');
    const btnRefreshSecurity = document.getElementById('btnRefreshSecurity');
    const assignPermissionsForm = document.getElementById('assignPermissionsForm');
    const selectRole = document.getElementById('selectRole');

    const cntRoles = document.getElementById('cntRoles');
    const cntPermissions = document.getElementById('cntPermissions');

    const API_BASE = window.location.pathname.includes('/globaltrade-web') ? '/globaltrade-web/api' : '/api';

    let rolesCache = [];
    let permissionsCache = [];

    function showAlert(message, type = 'success') {
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

    // Load Permissions List
    async function loadPermissions() {
        try {
            const res = await fetch(`${API_BASE}/security/permissions`);
            const perms = await res.json();

            if (res.ok && Array.isArray(perms)) {
                permissionsCache = perms;
                cntPermissions.textContent = `${perms.length} Permissions`;

                permissionsCheckboxList.innerHTML = perms.map(p => `
                    <label class="perm-checkbox-item">
                        <input type="checkbox" class="chk-perm" value="${p.name}">
                        <span><strong>${p.name}</strong> - <small style="color:var(--text-muted);">${p.description}</small></span>
                    </label>
                `).join('');
            }
        } catch (err) {
            console.error('Error fetching permissions:', err);
        }
    }

    // Load Roles & Role Permissions Matrix
    async function loadRoles() {
        try {
            const res = await fetch(`${API_BASE}/security/roles`);
            const roles = await res.json();

            if (res.ok && Array.isArray(roles)) {
                rolesCache = roles;
                cntRoles.textContent = `${roles.length} System Roles`;

                // Update select options
                selectRole.innerHTML = roles.map(r => `
                    <option value="${r.id}">${r.name}</option>
                `).join('');

                if (roles.length === 0) {
                    rolesTableBody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:var(--text-muted);">No roles configured.</td></tr>`;
                    return;
                }

                rolesTableBody.innerHTML = roles.map(r => {
                    const perms = r.permissions || [];
                    const permBadges = perms.map(p => `<span class="perm-badge">${p}</span>`).join(' ');

                    return `
                        <tr>
                            <td>
                                <strong>#ROLE-${r.id}</strong><br>
                                <span style="font-weight:700; color:#0f172a;">${r.name}</span>
                            </td>
                            <td>
                                <div style="max-width:340px; flex-wrap:wrap;">
                                    ${permBadges.length > 0 ? permBadges : '<small style="color:var(--text-muted);">No permissions assigned</small>'}
                                </div>
                            </td>
                            <td><strong style="color:#2563eb;">${r.permissionCount || 0} Perms</strong></td>
                            <td>
                                <button type="button" class="btn-primary btn-edit-role-perms" data-id="${r.id}" style="padding:4px 10px; font-size:11px; margin:0; background: linear-gradient(135deg, #ef4444 0%, #b91c1c 100%);">
                                    ⚙️ Edit Permissions
                                </button>
                            </td>
                        </tr>
                    `;
                }).join('');

                // Edit role perms click handler
                document.querySelectorAll('.btn-edit-role-perms').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const roleId = parseInt(btn.getAttribute('data-id'));
                        selectRole.value = roleId;
                        syncCheckboxesForRole(roleId);
                    });
                });

                // Auto-sync checkboxes for selected role
                if (roles.length > 0) {
                    syncCheckboxesForRole(parseInt(selectRole.value));
                }
            } else {
                rolesTableBody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:var(--error-color);">Failed to load roles matrix.</td></tr>`;
            }
        } catch (err) {
            console.error('Error fetching roles matrix:', err);
            rolesTableBody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:var(--error-color);">Network error loading roles matrix.</td></tr>`;
        }
    }

    function syncCheckboxesForRole(roleId) {
        const role = rolesCache.find(r => r.id === roleId);
        if (!role) return;

        const assignedPerms = role.permissions || [];
        document.querySelectorAll('.chk-perm').forEach(chk => {
            chk.checked = assignedPerms.includes(chk.value);
        });
    }

    selectRole?.addEventListener('change', (e) => {
        syncCheckboxesForRole(parseInt(e.target.value));
    });

    // Save Updated Permissions to role_permissions Table
    assignPermissionsForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const roleId = parseInt(selectRole.value);
        const selectedPerms = Array.from(document.querySelectorAll('.chk-perm:checked')).map(c => c.value);

        try {
            const res = await fetch(`${API_BASE}/security/roles/assign`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    roleId,
                    permissionNames: selectedPerms
                })
            });

            const data = await res.json();

            if (res.ok) {
                showAlert(`🛡️ Role Permissions successfully updated in role_permissions database table for ${data.roleName}! (${data.assignedPermissions.length} Permissions Active)`, 'success');
                if (typeof window.triggerSystemToast === 'function') {
                    window.triggerSystemToast('🛡️ Role Permissions Updated', `Saved ${data.assignedPermissions.length} permissions to role_permissions table for ${data.roleName}!`, 'SUCCESS');
                }
                loadRoles();
            } else {
                showAlert(data.error || 'Failed to update role permissions.', 'error');
            }
        } catch (err) {
            showAlert('Network error updating role permissions.', 'error');
        }
    });

    btnRefreshSecurity?.addEventListener('click', () => {
        loadPermissions().then(loadRoles);
    });

    // Initial Load
    loadPermissions().then(loadRoles);
});
