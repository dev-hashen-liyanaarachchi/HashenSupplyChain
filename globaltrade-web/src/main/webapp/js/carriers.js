document.addEventListener('DOMContentLoaded', () => {
    const carriersTableBody = document.getElementById('carriersTableBody');
    const handoverQueueTableBody = document.getElementById('handoverQueueTableBody');
    const alertBox = document.getElementById('alertBox');
    const btnRefreshCarriers = document.getElementById('btnRefreshCarriers');
    const addCarrierForm = document.getElementById('addCarrierForm');

    const cntTotalCarriers = document.getElementById('cntTotalCarriers');
    const cntArrivedCargo = document.getElementById('cntArrivedCargo');

    const API_BASE = window.location.pathname.includes('/globaltrade-web') ? '/globaltrade-web/api' : '/api';

    let carriersCache = [];

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

    // Load Carriers List from Database
    async function loadCarriers() {
        try {
            const res = await fetch(`${API_BASE}/carriers`);
            const carriers = await res.json();

            if (res.ok && Array.isArray(carriers)) {
                carriersCache = carriers;
                cntTotalCarriers.textContent = `${carriers.length} Active Fleets`;

                if (carriers.length === 0) {
                    carriersTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">No carriers registered.</td></tr>`;
                    return;
                }

                carriersTableBody.innerHTML = carriers.map(c => {
                    const countryBadgeClass = `badge-${(c.countryCode || 'LK').toLowerCase()}`;
                    return `
                        <tr>
                            <td>
                                <strong>#CARRIER-${c.id}</strong><br>
                                <span style="font-weight:700; color:#0f172a;">${c.name}</span>
                            </td>
                            <td><span class="user-badge ${countryBadgeClass}">${c.countryCode || 'LK'}</span></td>
                            <td><span class="badge-status badge-active" style="background:#eff6ff; color:#1d4ed8; font-size:10px;">${c.carrierType || 'ROAD_EXPRESS'}</span></td>
                            <td>
                                <strong>${c.contactPhone || '+94 11 234 5678'}</strong><br>
                                <small style="color:var(--text-muted); font-size:11px;">${c.contactEmail || 'dispatch@company.lk'}</small>
                            </td>
                            <td><strong>${c.fleetSize || '25 Vans'}</strong></td>
                            <td><span class="badge-status badge-active">${c.operatingStatus || 'ACTIVE'}</span></td>
                        </tr>
                    `;
                }).join('');

                // Load Handover Queue
                loadHandoverQueue();
            } else {
                carriersTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--error-color);">Failed to load carriers directory.</td></tr>`;
            }
        } catch (err) {
            console.error('Error fetching carriers directory:', err);
            carriersTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--error-color);">Network error loading carriers.</td></tr>`;
        }
    }

    // Load Destination Country Handover Queue from Finance Ledger
    async function loadHandoverQueue() {
        try {
            const res = await fetch(`${API_BASE}/finance/ledger`);
            const docs = await res.json();

            if (res.ok && Array.isArray(docs)) {
                if (docs.length === 0) {
                    handoverQueueTableBody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:var(--text-muted);">No cargo shipments pending destination handover.</td></tr>`;
                    return;
                }

                handoverQueueTableBody.innerHTML = docs.map(d => {
                    const trackingNo = d.trackingNumber || `TRK-DHL-${90000 + d.id}`;
                    const destCountry = d.destinationCountry || 'LK';

                    // Filter carriers matching destination country (or fallback to LK carriers)
                    const relevantCarriers = carriersCache.filter(c => c.countryCode === destCountry || c.countryCode === 'LK');
                    const optionsHtml = (relevantCarriers.length > 0 ? relevantCarriers : carriersCache).map(c => `
                        <option value="${c.id}">${c.name} (${c.countryCode})</option>
                    `).join('');

                    return `
                        <tr>
                            <td>
                                <strong style="color:#2563eb;">🚚 ${trackingNo}</strong><br>
                                <small style="color:var(--text-muted);">Doc #DOC-${d.id}</small>
                            </td>
                            <td>
                                <strong>Port ${destCountry}</strong><br>
                                <small style="color:#059669; font-weight:600;">Customs Cleared</small>
                            </td>
                            <td>
                                <select class="select-destination-carrier" data-id="${d.id}" style="padding:6px 8px; font-size:11px; font-weight:600; border:1.5px solid #2563eb; border-radius:6px; background:#f0f9ff; color:#1e40af;">
                                    ${optionsHtml}
                                </select>
                            </td>
                            <td>
                                <div style="display:flex; flex-direction:column; gap:4px;">
                                    <input type="text" class="input-driver-name" data-id="${d.id}" placeholder="Driver Name (e.g. K. Perera)" value="Agent K. Perera" style="padding:4px 8px; font-size:11px; border:1px solid #cbd5e1; border-radius:4px;">
                                    <input type="text" class="input-vehicle-no" data-id="${d.id}" placeholder="Vehicle No (e.g. WP-BC-8910)" value="WP-BC-8910" style="padding:4px 8px; font-size:11px; border:1px solid #cbd5e1; border-radius:4px;">
                                </div>
                            </td>
                            <td>
                                <button type="button" class="btn-primary btn-assign-local-carrier" data-id="${d.id}" style="width:100%; margin:0; padding:6px 10px; font-size:11px; background: linear-gradient(135deg, #059669 0%, #047857 100%);">
                                    🚚 Handover Cargo to Carrier
                                </button>
                            </td>
                        </tr>
                    `;
                }).join('');

                // Attach Handover button click handlers
                document.querySelectorAll('.btn-assign-local-carrier').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const id = parseInt(btn.getAttribute('data-id'));
                        const carrierSelect = document.querySelector(`.select-destination-carrier[data-id="${id}"]`);
                        const driverInput = document.querySelector(`.input-driver-name[data-id="${id}"]`);
                        const vehicleInput = document.querySelector(`.input-vehicle-no[data-id="${id}"]`);

                        const carrierId = carrierSelect ? parseInt(carrierSelect.value) : 1;
                        const driverName = driverInput ? driverInput.value.trim() : 'Agent K. Perera';
                        const vehicleNo = vehicleInput ? vehicleInput.value.trim() : 'WP-BC-8910';

                        handoverCargoToCarrier(id, carrierId, driverName, vehicleNo);
                    });
                });
            }
        } catch (err) {
            console.error('Error loading cargo handover queue:', err);
        }
    }

    // Execute Destination Cargo Handover
    async function handoverCargoToCarrier(shipmentId, carrierId, driverName, vehicleNo) {
        hideAlert();
        try {
            const res = await fetch(`${API_BASE}/carriers/handover`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({shipmentId, carrierId, driverName, vehicleNo})
            });

            const data = await res.json();

            if (res.ok) {
                showAlert(`🚚 Cargo Handover Confirmed! Shipment #${data.trackingNumber} handed over to ${data.carrierName} (Driver: ${driverName}, Vehicle: ${vehicleNo}) for final customer delivery!`, 'success');
                if (typeof window.triggerSystemToast === 'function') {
                    window.triggerSystemToast('🚚 Destination Cargo Handover Confirmed', `Shipment handed over to ${data.carrierName}!`, 'SUCCESS');
                }
                loadCarriers();
            } else {
                showAlert(data.error || 'Failed to complete cargo handover.', 'error');
            }
        } catch (err) {
            showAlert('Network error completing cargo handover.', 'error');
        }
    }

    // Submit Add New Carrier Form
    addCarrierForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        const name = document.getElementById('newCarrierName').value.trim();
        const countryCode = document.getElementById('newCarrierCountry').value;
        const carrierType = document.getElementById('newCarrierType').value;
        const contactPhone = document.getElementById('newCarrierPhone').value.trim();
        const contactEmail = document.getElementById('newCarrierEmail').value.trim();
        const fleetSize = document.getElementById('newCarrierFleet').value.trim();

        if (!name || !contactPhone || !contactEmail || !fleetSize) {
            showAlert('Please fill in all required carrier registration fields.', 'error');
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/carriers`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    name,
                    countryCode,
                    carrierType,
                    contactPhone,
                    contactEmail,
                    fleetSize
                })
            });

            const data = await res.json();

            if (res.ok || res.status === 201) {
                showAlert(`➕ New Partner Carrier "${name}" (${countryCode}) successfully saved & registered in database!`, 'success');
                addCarrierForm.reset();
                loadCarriers();
            } else {
                showAlert(data.error || 'Failed to register new carrier.', 'error');
            }
        } catch (err) {
            showAlert('Network error saving carrier to database.', 'error');
        }
    });

    btnRefreshCarriers?.addEventListener('click', loadCarriers);

    // Initial Load
    loadCarriers();
});
