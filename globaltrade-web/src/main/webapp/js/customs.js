document.addEventListener('DOMContentLoaded', () => {
    const customsTableBody = document.getElementById('customsTableBody');
    const alertBox = document.getElementById('alertBox');
    const btnRefreshDocs = document.getElementById('btnRefreshDocs');

    const cntTotalDocs = document.getElementById('cntTotalDocs');
    const cntPending = document.getElementById('cntPending');
    const cntApproved = document.getElementById('cntApproved');
    const cntRejected = document.getElementById('cntRejected');

    const dossierModal = document.getElementById('dossierModal');
    const btnCloseModal = document.getElementById('btnCloseModal');
    const modalDossierContent = document.getElementById('modalDossierContent');
    const btnModalApprove = document.getElementById('btnModalApprove');
    const btnModalHold = document.getElementById('btnModalHold');

    let cachedDocs = [];
    let currentSelectedDocId = null;

    const API_BASE = window.location.pathname.includes('/globaltrade-web') ? '/globaltrade-web/api' : '/api';

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

    // Load Customs Documents Manifest
    async function loadCustomsDocuments() {
        try {
            const res = await fetch(`${API_BASE}/customs/documents`);
            const docs = await res.json();

            if (res.ok && Array.isArray(docs)) {
                cachedDocs = docs;
                let total = docs.length;
                let pending = 0;
                let approved = 0;
                let rejected = 0;

                docs.forEach(d => {
                    if (d.status === 'SUBMITTED' || d.status === 'UNDER_REVIEW' || d.status === 'IN_CUSTOMS') pending++;
                    else if (d.status === 'APPROVED' || d.status === 'CUSTOMS_CLEARED') approved++;
                    else if (d.status === 'REJECTED' || d.status === 'CUSTOMS_HOLD') rejected++;
                });

                cntTotalDocs.textContent = total;
                cntPending.textContent = pending;
                cntApproved.textContent = approved;
                cntRejected.textContent = rejected;

                if (docs.length === 0) {
                    customsTableBody.innerHTML = `<tr><td colspan="9" style="text-align:center; color:var(--text-muted);">No customs declarations submitted yet. Place an International Order to test.</td></tr>`;
                    return;
                }

                customsTableBody.innerHTML = docs.map(d => {
                    const isPending = d.status === 'SUBMITTED' || d.status === 'UNDER_REVIEW' || d.status === 'IN_CUSTOMS';
                    const isApproved = d.status === 'APPROVED' || d.status === 'CUSTOMS_CLEARED';

                    let statusBadgeClass = 'badge-pending';
                    if (isApproved) statusBadgeClass = 'badge-active';
                    else if (!isPending) statusBadgeClass = 'badge-alert';

                    const decVal = (d.declaredValue || 4890.00).toLocaleString(undefined, {minimumFractionDigits: 2});
                    const dutyVal = (d.dutyFee || (d.declaredValue ? d.declaredValue * 0.05 : 244.50)).toLocaleString(undefined, {minimumFractionDigits: 2});
                    const route = `${d.originCountry || 'US'} ➔ ${d.destinationCountry || 'LK'}`;

                    return `
                        <tr>
                            <td>
                                <strong>#DOC-${d.id}</strong><br>
                                <span class="badge-status badge-active" style="background:#f3e8ff; color:#6b21a8; font-size:10px;">${d.documentType || 'COMMERCIAL_INVOICE'}</span>
                            </td>
                            <td><strong style="color:#2563eb;">🚚 ${d.trackingNumber}</strong></td>
                            <td>
                                <strong>${route}</strong><br>
                                <small style="color:var(--text-muted); font-size:11px;">Depot: ${d.exporterName || 'Export Hub'}</small>
                            </td>
                            <td><strong>$${decVal}</strong></td>
                            <td><strong style="color:#b45309;">$${dutyVal}</strong></td>
                            <td><strong style="color:#0f766e;">HS ${d.hsCode}</strong></td>
                            <td><span class="badge-status ${statusBadgeClass}">${d.status}</span></td>
                            <td><small>${d.inspectedBy}</small></td>
                            <td>
                                <div style="display:flex; gap:4px; flex-wrap:wrap;">
                                    <button type="button" class="btn-secondary btn-view-dossier" data-id="${d.id}" style="width:auto; padding:4px 8px; font-size:11px; background:#e0e7ff; color:#3730a3;">
                                        📄 View Dossier
                                    </button>
                                    ${isPending ? `
                                        <button type="button" class="btn-primary btn-approve" data-id="${d.id}" style="width:auto; margin-top:0; padding:4px 8px; font-size:11px; background:#059669;">
                                            ✅ Pass
                                        </button>
                                        <button type="button" class="btn-primary btn-reject" data-id="${d.id}" style="width:auto; margin-top:0; padding:4px 8px; font-size:11px; background:#dc2626;">
                                            ❌ Hold
                                        </button>
                                    ` : ``}
                                </div>
                            </td>
                        </tr>
                    `;
                }).join('');

                // Attach button click handlers
                document.querySelectorAll('.btn-view-dossier').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const id = parseInt(btn.getAttribute('data-id'));
                        openDossierModal(id);
                    });
                });

                document.querySelectorAll('.btn-approve').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const id = parseInt(btn.getAttribute('data-id'));
                        reviewDocument(id, true);
                    });
                });

                document.querySelectorAll('.btn-reject').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const id = parseInt(btn.getAttribute('data-id'));
                        reviewDocument(id, false);
                    });
                });
            } else {
                customsTableBody.innerHTML = `<tr><td colspan="9" style="text-align:center; color:var(--error-color);">Failed to load customs manifest.</td></tr>`;
            }
        } catch (err) {
            console.error('Error loading customs documents:', err);
            customsTableBody.innerHTML = `<tr><td colspan="9" style="text-align:center; color:var(--error-color);">Network error loading customs manifest.</td></tr>`;
        }
    }

    // Open Official Customs Dossier Inspection Modal
    function openDossierModal(docId) {
        const d = cachedDocs.find(x => x.id === docId);
        if (!d) return;

        currentSelectedDocId = docId;
        const decVal = (d.declaredValue || 4890.00).toLocaleString(undefined, {minimumFractionDigits: 2});
        const dutyVal = (d.dutyFee || 244.50).toLocaleString(undefined, {minimumFractionDigits: 2});

        modalDossierContent.innerHTML = `
            <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:12px; padding:18px; font-size:13px;">
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px; margin-bottom:14px; border-bottom:1px dashed #cbd5e1; padding-bottom:12px;">
                    <div>
                        <strong style="color:#64748b; font-size:11px; text-transform:uppercase;">Exporting Origin Depot</strong>
                        <div style="font-weight:700; color:#0f172a; margin-top:2px;">${d.exporterName || 'USA New York Air Cargo Hub'} (${d.originCountry || 'US'})</div>
                    </div>
                    <div>
                        <strong style="color:#64748b; font-size:11px; text-transform:uppercase;">Destination Import Authority</strong>
                        <div style="font-weight:700; color:#0f172a; margin-top:2px;">Sri Lanka Customs Port (${d.destinationCountry || 'LK'})</div>
                    </div>
                </div>

                <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px; margin-bottom:14px; border-bottom:1px dashed #cbd5e1; padding-bottom:12px;">
                    <div>
                        <strong style="color:#64748b; font-size:11px; text-transform:uppercase;">Consignee / Importer Name</strong>
                        <div style="font-weight:600; color:#1e293b;">${d.importerName || 'Consignee Client'}</div>
                    </div>
                    <div>
                        <strong style="color:#64748b; font-size:11px; text-transform:uppercase;">Air Cargo Tracking Ref</strong>
                        <div style="font-weight:700; color:#2563eb;">🚚 ${d.trackingNumber}</div>
                    </div>
                </div>

                <div style="background:#ffffff; border:1px solid #e2e8f0; border-radius:8px; padding:14px; margin-bottom:14px;">
                    <strong style="color:#0f766e; font-size:12px; display:block; margin-bottom:6px;">📦 Commercial Packing List Summary:</strong>
                    <div style="font-weight:600; color:#334155;">${d.packingListItems || '1x Siemens Diagnostic Ultrasound Cardiac Transducer [HS 9018.90]'}</div>
                </div>

                <div style="display:grid; grid-template-columns:1fr 1fr 1fr; gap:10px; background:#eff6ff; border:1px solid #bfdbfe; border-radius:8px; padding:12px;">
                    <div>
                        <small style="color:#1e40af; font-size:10px; font-weight:700; display:block;">HS TARIFF CODE</small>
                        <strong style="color:#1d4ed8; font-size:14px;">HS ${d.hsCode}</strong>
                    </div>
                    <div>
                        <small style="color:#1e40af; font-size:10px; font-weight:700; display:block;">DECLARED GOODS VALUE</small>
                        <strong style="color:#0f172a; font-size:14px;">$${decVal}</strong>
                    </div>
                    <div>
                        <small style="color:#1e40af; font-size:10px; font-weight:700; display:block;">IMPORT DUTY TARIFF (5%)</small>
                        <strong style="color:#b45309; font-size:14px;">$${dutyVal}</strong>
                    </div>
                </div>

                <div style="margin-top:12px; font-size:11px; color:#64748b; text-align:right;">
                    ⏱️ Compliance Window Deadline: <strong>${d.clearanceDeadline || '48 Hours Window'}</strong>
                </div>
            </div>
        `;

        dossierModal.style.display = 'flex';
    }

    btnCloseModal?.addEventListener('click', () => { dossierModal.style.display = 'none'; });

    btnModalApprove?.addEventListener('click', () => {
        if (currentSelectedDocId) {
            dossierModal.style.display = 'none';
            reviewDocument(currentSelectedDocId, true);
        }
    });

    btnModalHold?.addEventListener('click', () => {
        if (currentSelectedDocId) {
            dossierModal.style.display = 'none';
            reviewDocument(currentSelectedDocId, false);
        }
    });

    // Review Customs Document Action
    async function reviewDocument(documentId, approve) {
        hideAlert();
        const officerName = localStorage.getItem('gt_username') || 'Chief Officer Fernando (#8902)';
        const notes = approve ? 'HS Tariff verified, commercial invoice matches cargo' : 'Customs inspection failed: Tariff discrepancy';

        try {
            const res = await fetch(`${API_BASE}/customs/review`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ documentId, officerName, approve, notes })
            });

            const data = await res.json();

            if (res.ok) {
                showAlert(`🛃 Customs Declaration #DOC-${documentId} ${approve ? 'APPROVED & CLEARED' : 'REJECTED & HELD'} by ${data.inspectedBy}!`, approve ? 'success' : 'error');
                loadCustomsDocuments();
            } else {
                showAlert(data.error || 'Customs review action failed.');
            }
        } catch (err) {
            showAlert('Network error performing customs review.');
        }
    }

    btnRefreshDocs?.addEventListener('click', loadCustomsDocuments);

    // Initial Load
    loadCustomsDocuments();
});
