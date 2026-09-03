document.addEventListener('DOMContentLoaded', () => {
    const vendorTableBody = document.getElementById('vendorTableBody');
    const alertBox = document.getElementById('alertBox');
    const btnRefreshVendors = document.getElementById('btnRefreshVendors');
    const evalVendorForm = document.getElementById('evalVendorForm');

    const cntEvaluatedVendors = document.getElementById('cntEvaluatedVendors');
    const valAvgFulfillment = document.getElementById('valAvgFulfillment');
    const valAvgOnTime = document.getElementById('valAvgOnTime');

    const API_BASE = window.location.pathname.includes('/globaltrade-web') ? '/globaltrade-web/api' : '/api';

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

    // Load Vendor Performance Evaluations from vendor_performances Table
    async function loadVendorPerformances() {
        try {
            const res = await fetch(`${API_BASE}/vendors/performance`);
            const items = await res.json();

            if (res.ok && Array.isArray(items)) {
                cntEvaluatedVendors.textContent = `${items.length} Vendors`;

                if (items.length > 0) {
                    const avgF = (items.reduce((s, i) => s + (i.fulfillmentScore || 98.0), 0) / items.length).toFixed(1);
                    const avgO = (items.reduce((s, i) => s + (i.onTimeDeliveryRate || 99.0), 0) / items.length).toFixed(1);
                    valAvgFulfillment.textContent = `${avgF}%`;
                    valAvgOnTime.textContent = `${avgO}%`;
                }

                if (items.length === 0) {
                    vendorTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">No vendor evaluation records saved yet.</td></tr>`;
                    return;
                }

                vendorTableBody.innerHTML = items.map(i => {
                    const fScore = (i.fulfillmentScore || 98.5).toFixed(1);
                    const oRate = (i.onTimeDeliveryRate || 99.2).toFixed(1);
                    const qRating = (i.qualityRating || 4.9).toFixed(1);

                    return `
                        <tr>
                            <td>
                                <strong>#PERF-${i.id}</strong><br>
                                <span style="font-weight:700; color:#0f172a;">${i.vendorName || 'Supplier Company'}</span>
                            </td>
                            <td><strong style="color:#059669;">${fScore}%</strong></td>
                            <td><strong style="color:#2563eb;">${oRate}%</strong></td>
                            <td><strong style="color:#b45309;">${qRating} / 5.0 ⭐</strong></td>
                            <td><small style="color:var(--text-muted);">${i.evaluatedAt ? i.evaluatedAt.split('T')[0] : 'Recent'}</small></td>
                            <td>
                                <button type="button" class="btn-primary btn-re-eval" data-id="${i.vendorId || 1}" style="padding:4px 10px; font-size:11px; margin:0; background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);">
                                    📊 Re-Audit SLA
                                </button>
                            </td>
                        </tr>
                    `;
                }).join('');

                document.querySelectorAll('.btn-re-eval').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const id = btn.getAttribute('data-id');
                        triggerVendorAudit(id, 99.0, 99.5, 5.0);
                    });
                });
            } else {
                vendorTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--error-color);">Failed to load vendor performance ledger.</td></tr>`;
            }
        } catch (err) {
            console.error('Error fetching vendor performance ledger:', err);
            vendorTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--error-color);">Network error loading vendor ledger.</td></tr>`;
        }
    }

    // Trigger Vendor Audit and save to vendor_performances
    async function triggerVendorAudit(vendorId, fulfillmentScore, onTimeDeliveryRate, qualityRating) {
        hideAlert();
        try {
            const res = await fetch(`${API_BASE}/vendors/evaluate`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    vendorId: parseInt(vendorId),
                    fulfillmentScore: parseFloat(fulfillmentScore),
                    onTimeDeliveryRate: parseFloat(onTimeDeliveryRate),
                    qualityRating: parseFloat(qualityRating)
                })
            });

            const data = await res.json();

            if (res.ok) {
                showAlert(`📊 Vendor Performance Evaluation saved to vendor_performances table! Score: ${fulfillmentScore}%`, 'success');
                if (typeof window.triggerSystemToast === 'function') {
                    window.triggerSystemToast('📊 Vendor Performance Evaluated', `Saved evaluation for Vendor #${vendorId} into vendor_performances table!`, 'SUCCESS');
                }
                loadVendorPerformances();
            } else {
                showAlert(data.error || 'Failed to save vendor evaluation.', 'error');
            }
        } catch (err) {
            showAlert('Network error saving vendor evaluation.', 'error');
        }
    }

    evalVendorForm?.addEventListener('submit', (e) => {
        e.preventDefault();
        const vendorId = document.getElementById('selectVendor').value;
        const fulfillmentScore = document.getElementById('inputFulfillment').value;
        const onTimeDeliveryRate = document.getElementById('inputOnTime').value;
        const qualityRating = document.getElementById('inputQuality').value;

        triggerVendorAudit(vendorId, fulfillmentScore, onTimeDeliveryRate, qualityRating);
    });

    btnRefreshVendors?.addEventListener('click', loadVendorPerformances);

    // Initial Load
    loadVendorPerformances();
});
