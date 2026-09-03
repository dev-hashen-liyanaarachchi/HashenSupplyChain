document.addEventListener('DOMContentLoaded', () => {
    const financeTableBody = document.getElementById('financeTableBody');
    const alertBox = document.getElementById('alertBox');
    const btnRefreshFinance = document.getElementById('btnRefreshFinance');

    const valTotalDuty = document.getElementById('valTotalDuty');
    const cntPendingDuty = document.getElementById('cntPendingDuty');
    const cntCarrierDispatched = document.getElementById('cntCarrierDispatched');

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

    // Load Duty Settlement Queue
    async function loadDutySettlementQueue() {
        try {
            const res = await fetch(`${API_BASE}/finance/ledger`);
            const items = await res.json();

            if (res.ok && Array.isArray(items)) {
                let pendingCount = 0;
                let dispatchedCount = 0;
                let totalDutySum = 0;

                items.forEach(i => {
                    const duty = i.dutyFee || (i.declaredValue ? i.declaredValue * 0.05 : 244.50);
                    if (i.settlementStatus === 'DUTY_SETTLED_AND_DISPATCHED') {
                        dispatchedCount++;
                        totalDutySum += duty;
                    } else {
                        pendingCount++;
                    }
                });

                cntPendingDuty.textContent = pendingCount;
                cntCarrierDispatched.textContent = dispatchedCount;
                valTotalDuty.textContent = `$${totalDutySum.toLocaleString(undefined, {minimumFractionDigits: 2})}`;

                if (items.length === 0) {
                    financeTableBody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--text-muted);">No customs clearance records filed yet.</td></tr>`;
                    return;
                }

                financeTableBody.innerHTML = items.map(i => {
                    const isSettled = i.settlementStatus === 'DUTY_SETTLED_AND_DISPATCHED';
                    const decVal = (i.declaredValue || 4890.00).toLocaleString(undefined, {minimumFractionDigits: 2});
                    const dutyVal = (i.dutyFee || (i.declaredValue ? i.declaredValue * 0.05 : 244.50)).toLocaleString(undefined, {minimumFractionDigits: 2});
                    const route = `${i.originCountry || 'US'} ➔ ${i.destinationCountry || 'LK'}`;
                    const currentCarrier = i.assignedCarrier || 'DHL Express International Air Fleet';

                    return `
                        <tr>
                            <td>
                                <strong>#DOC-${i.id}</strong><br>
                                <span class="badge-status badge-active" style="background:#f3e8ff; color:#6b21a8; font-size:10px;">${i.documentType || 'COMMERCIAL_INVOICE'}</span>
                            </td>
                            <td><strong style="color:#2563eb;">🚚 ${i.trackingNumber}</strong></td>
                            <td>
                                <strong>${route}</strong><br>
                                <small style="color:var(--text-muted); font-size:11px;">Depot: ${i.exporterName || 'Export Hub'}</small>
                            </td>
                            <td><strong>$${decVal}</strong></td>
                            <td><strong style="color:#b45309;">$${dutyVal}</strong></td>
                            <td><span class="badge-status badge-active" style="background:#dcfce7; color:#15803d;">${i.status}</span></td>
                            <td>
                                ${isSettled ? `
                                    <span class="badge-status badge-duty-settled">✅ DUTY SETTLED</span><br>
                                    <small style="color:#2563eb; font-weight:600;">Carrier: ${currentCarrier}</small>
                                ` : `
                                    <span class="badge-status badge-duty-pending">⏳ DUTY PENDING</span>
                                `}
                            </td>
                            <td>
                                <div style="display:flex; flex-direction:column; gap:6px;">
                                    <select class="select-carrier" data-id="${i.id}" style="padding:6px 10px; font-size:11px; font-weight:600; border:1.5px solid #2563eb; border-radius:8px; background:#f0f9ff; color:#1e40af;">
                                        <option value="DHL Express International Air Fleet" ${currentCarrier.includes('DHL') ? 'selected' : ''}>✈️ DHL Express Air Fleet</option>
                                        <option value="FedEx Cargo Networks" ${currentCarrier.includes('FedEx') ? 'selected' : ''}>✈️ FedEx Cargo Networks</option>
                                        <option value="UPS Supply Chain Solutions" ${currentCarrier.includes('UPS') ? 'selected' : ''}>✈️ UPS Global Freight</option>
                                        <option value="Sri Lanka Logistics Ground Fleet" ${currentCarrier.includes('Sri Lanka') ? 'selected' : ''}>🚚 Sri Lanka Ground Carrier</option>
                                    </select>
                                    <button type="button" class="btn-primary btn-settle-duty" data-id="${i.id}" style="width:100%; margin-top:0; padding:6px 10px; font-size:11px; ${isSettled ? 'background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);' : 'background: linear-gradient(135deg, #059669 0%, #047857 100%);'}">
                                        ${isSettled ? '🔄 Re-Assign Carrier & Update' : '💳 Settle Duty & Handover Cargo'}
                                    </button>
                                </div>
                            </td>
                        </tr>
                    `;
                }).join('');

                // Attach button click handlers
                document.querySelectorAll('.btn-settle-duty').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const id = parseInt(btn.getAttribute('data-id'));
                        const carrierSelect = document.querySelector(`.select-carrier[data-id="${id}"]`);
                        const carrierName = carrierSelect ? carrierSelect.value : 'DHL Express International Air Fleet';
                        settleDutyAndHandover(id, carrierName);
                    });
                });
            } else {
                financeTableBody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--error-color);">Failed to load duty settlement ledger.</td></tr>`;
            }
        } catch (err) {
            console.error('Error fetching duty settlement queue:', err);
            financeTableBody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--error-color);">Network error fetching duty settlement ledger.</td></tr>`;
        }
    }

    // Settle Duty & Handover to Carrier
    async function settleDutyAndHandover(documentId, carrierName) {
        hideAlert();
        try {
            const res = await fetch(`${API_BASE}/finance/settle`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    documentId,
                    carrierName,
                    financeOfficer: 'Chief Financial Officer Perera (#409)'
                })
            });

            const data = await res.json();

            if (res.ok) {
                showAlert(`💳 Import duty settled for Document #DOC-${documentId}! Cargo handed over to ${carrierName} for final delivery.`, 'success');
                if (typeof window.triggerSystemToast === 'function') {
                    window.triggerSystemToast('💳 Duty Settled & Carrier Handover', `Cargo #DOC-${documentId} handed over to ${carrierName}!`, 'SUCCESS');
                }
                loadDutySettlementQueue();
            } else {
                showAlert(data.error || 'Failed to settle duty payment.', 'error');
            }
        } catch (err) {
            showAlert('Network error settling duty payment.', 'error');
        }
    }

    // Load Payments Ledger (payments Table)
    async function loadPaymentsTable() {
        const paymentsTableBody = document.getElementById('paymentsTableBody');
        if (!paymentsTableBody) return;

        try {
            const res = await fetch(`${API_BASE}/finance/payments`);
            const payments = await res.json();

            if (res.ok && Array.isArray(payments)) {
                if (payments.length === 0) {
                    paymentsTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">No payment transactions recorded yet.</td></tr>`;
                    return;
                }

                paymentsTableBody.innerHTML = payments.map(p => `
                    <tr>
                        <td>
                            <strong style="color:#059669;">#PAY-${p.id}</strong><br>
                            <small style="color:var(--text-muted); font-size:10px;">Ref: ${p.transactionReference}</small>
                        </td>
                        <td><strong style="color:#2563eb;">📦 ${p.orderNumber}</strong></td>
                        <td><strong>${p.customerName}</strong></td>
                        <td><span class="badge-status badge-active" style="background:#e0f2fe; color:#0369a1; font-size:10px;">💳 ${p.paymentMethod}</span></td>
                        <td><strong style="color:#059669; font-size:13px;">$${(p.amount || 4890.00).toFixed(2)}</strong></td>
                        <td><small style="color:#64748b;">${p.timestamp ? p.timestamp.replace('T', ' ').substring(0, 16) : 'Recent'}</small></td>
                        <td><span class="badge-status badge-active" style="background:#dcfce7; color:#15803d;">✅ ${p.paymentStatus}</span></td>
                    </tr>
                `).join('');
            } else {
                paymentsTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--error-color);">Failed to load payment transactions.</td></tr>`;
            }
        } catch (err) {
            console.error('Error fetching payments ledger:', err);
            paymentsTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--error-color);">Network error fetching payments.</td></tr>`;
        }
    }

    const btnRefreshPayments = document.getElementById('btnRefreshPayments');
    btnRefreshPayments?.addEventListener('click', loadPaymentsTable);

    btnRefreshFinance?.addEventListener('click', () => {
        loadDutySettlementQueue();
        loadPaymentsTable();
    });

    // Initial Load
    loadDutySettlementQueue();
    loadPaymentsTable();
});
