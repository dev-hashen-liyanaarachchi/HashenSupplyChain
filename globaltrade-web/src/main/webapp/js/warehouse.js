document.addEventListener('DOMContentLoaded', () => {
    const warehouseForm = document.getElementById('warehouseForm');
    const alertBox = document.getElementById('alertBox');
    const whTableBody = document.getElementById('whTableBody');
    const btnRefresh = document.getElementById('btnRefreshWarehouses');

    const getApiBaseUrl = () => {
        const path = window.location.pathname;
        if (path.includes('/globaltrade-web')) {
            return '/globaltrade-web/api/warehouses';
        }
        return '/api/warehouses';
    };

    const API_URL = getApiBaseUrl();

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

    // Load Warehouses Table
    async function loadWarehouses() {
        try {
            const response = await fetch(API_URL);
            const data = await response.json();

            if (response.ok && Array.isArray(data)) {
                if (data.length === 0) {
                    whTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">No warehouses registered yet.</td></tr>`;
                    return;
                }

                whTableBody.innerHTML = data.map(w => `
                    <tr>
                        <td>#WH-${w.id}</td>
                        <td><strong>${w.name}</strong></td>
                        <td>${w.street || ''}, ${w.city || ''} (${w.country || ''})</td>
                        <td>${(w.maxCapacity || 50000).toLocaleString()} Units</td>
                        <td><span class="badge-status badge-active">OPERATIONAL</span></td>
                        <td>
                            <button type="button" class="btn-secondary btn-delete" data-id="${w.id}" style="padding:4px 8px; font-size:11px; color:#ef4444; background:#fef2f2;">
                                Delete
                            </button>
                        </td>
                    </tr>
                `).join('');

                // Attach Delete Event Handlers
                document.querySelectorAll('.btn-delete').forEach(btn => {
                    btn.addEventListener('click', async () => {
                        const id = btn.getAttribute('data-id');
                        if (confirm(`Are you sure you want to delete Warehouse #${id}?`)) {
                            await deleteWarehouse(id);
                        }
                    });
                });
            } else {
                whTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--error-color);">Failed to load warehouses.</td></tr>`;
            }
        } catch (err) {
            console.error('Error fetching warehouses:', err);
            whTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--error-color);">Network error loading warehouses.</td></tr>`;
        }
    }

    // Delete Warehouse
    async function deleteWarehouse(id) {
        try {
            const response = await fetch(`${API_URL}/${id}`, {method: 'DELETE'});
            if (response.ok) {
                showAlert(`Warehouse #${id} deleted successfully.`, 'success');
                loadWarehouses();
            } else {
                showAlert(`Failed to delete warehouse #${id}.`);
            }
        } catch (err) {
            showAlert(`Network error while deleting warehouse.`);
        }
    }

    // Submit Warehouse Form
    warehouseForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const btn = warehouseForm.querySelector('button[type="submit"]');
        const name = document.getElementById('whName').value.trim();
        const street = document.getElementById('whStreet').value.trim();
        const city = document.getElementById('whCity').value.trim();
        const state = document.getElementById('whState').value.trim();
        const postalCode = document.getElementById('whPostalCode').value.trim();
        const countryCode = document.getElementById('whCountryCode').value;
        const maxCapacity = parseInt(document.getElementById('whMaxCapacity').value) || 50000;

        if (!name || !street || !city || !countryCode) {
            showAlert('Please fill in all required fields.');
            return;
        }

        setLoading(btn, true);

        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    name,
                    street,
                    city,
                    state,
                    postalCode,
                    countryCode,
                    maxCapacity
                })
            });

            const data = await response.json();

            if (response.ok || response.status === 201) {
                showAlert(`Warehouse "${name}" created successfully (ID: #${data.id})!`, 'success');
                warehouseForm.reset();
                loadWarehouses();
            } else {
                showAlert(data.error || 'Failed to create warehouse.');
            }
        } catch (err) {
            showAlert('Network error while adding warehouse.');
            console.error('Create Warehouse error:', err);
        } finally {
            setLoading(btn, false);
        }
    });

    btnRefresh?.addEventListener('click', loadWarehouses);

    // Initial Load
    loadWarehouses();
});
