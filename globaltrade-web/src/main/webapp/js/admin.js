document.addEventListener('DOMContentLoaded', () => {
    const API_BASE = '/globaltrade-web/api';

    // UI Elements
    const kpiTotalOrders = document.getElementById('kpiTotalOrders');
    const kpiTotalRevenue = document.getElementById('kpiTotalRevenue');
    const kpiTotalShipments = document.getElementById('kpiTotalShipments');
    const kpiInCustoms = document.getElementById('kpiInCustoms');
    const alertBox = document.getElementById('alertBox');

    const tblAdminOrders = document.getElementById('tblAdminOrders');
    const filterOrderStatus = document.getElementById('filterOrderStatus');
    const btnRefreshAdminOrders = document.getElementById('btnRefreshAdminOrders');

    const tblWarehousePacking = document.getElementById('tblWarehousePacking');
    const selectWarehouseFilter = document.getElementById('selectWarehouseFilter');

    const tblAdminShipments = document.getElementById('tblAdminShipments');
    const btnRefreshShipments = document.getElementById('btnRefreshShipments');

    const tblAdminCustoms = document.getElementById('tblAdminCustoms');
    const btnRefreshCustoms = document.getElementById('btnRefreshCustoms');

    let cachedOrders = [];
    let cachedShipments = [];
    let cachedCustoms = [];

    // Tab Switching Logic
    const tabBtns = document.querySelectorAll('.admin-tab-btn');
    const tabContents = document.querySelectorAll('.admin-tab-content');

    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            tabBtns.forEach(b => b.classList.remove('active'));
            tabContents.forEach(c => c.classList.remove('active'));

            btn.classList.add('active');
            const targetId = btn.getAttribute('data-tab');
            document.getElementById(targetId)?.classList.add('active');
        });
    });

    function showAlert(msg, type = 'danger') {
        if (!alertBox) return;
        alertBox.style.display = 'block';
        alertBox.className = `alert alert-${type}`;
        alertBox.textContent = msg;
        setTimeout(() => {
            alertBox.style.display = 'none';
        }, 4500);
    }

    // Load Dashboard Stats
    async function loadDashboardStats() {
        try {
            const res = await fetch(`${API_BASE}/admin/dashboard-stats`);
            if (res.ok) {
                const stats = await res.json();
                kpiTotalOrders.textContent = stats.totalOrders || 0;
                kpiTotalRevenue.textContent = `$${(stats.totalRevenue || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}`;
                kpiTotalShipments.textContent = stats.totalShipments || 0;
                kpiInCustoms.textContent = stats.inCustomsCount || 0;
            }
        } catch (err) {
            console.error('Error loading admin dashboard stats:', err);
        }
    }

    // Render Orders Table
    function renderOrdersTable() {
        if (!tblAdminOrders) return;
        const selectedStatus = filterOrderStatus ? filterOrderStatus.value : 'ALL';

        let filtered = cachedOrders;
        if (selectedStatus !== 'ALL') {
            filtered = cachedOrders.filter(o => o.status === selectedStatus);
        }

        if (filtered.length === 0) {
            tblAdminOrders.innerHTML = `<tr><td colspan="8" style="text-align: center; color: #94a3b8;">No orders found for the selected filter.</td></tr>`;
            return;
        }

        tblAdminOrders.innerHTML = filtered.map(o => {
            const trackingNo = o.trackingNumber || `TRK-DHL-${10000 + (o.id * 37) % 90000}`;
            const isDom = o.isDomestic;
            let statusBadge = 'status-processing';
            if (o.status === 'PICKING_PACKING') statusBadge = 'status-picking';
            else if (o.status === 'PACKED_READY') statusBadge = 'status-packed';
            else if (o.status === 'SHIPPED') statusBadge = 'status-shipped';
            else if (o.status === 'DELIVERED') statusBadge = 'status-delivered';

            return `
                <tr>
                    <td>
                        <strong>#${o.orderNumber}</strong><br>
                        <small style="color:#38bdf8; font-weight:600;">🚚 ${trackingNo}</small>
                    </td>
                    <td>
                        <strong>${o.customerName}</strong><br>
                        <small style="color:#94a3b8;">${o.email}</small>
                    </td>
                    <td>
                        <strong>${o.originCountry || 'DE'}</strong> (${o.warehouseName || 'Hub'}) ➔ <strong>${o.destinationCountry}</strong>
                    </td>
                    <td>
                        <small style="color:${isDom ? '#34d399' : '#60a5fa'}; font-weight:600;">
                            ${isDom ? '🟢 Domestic Express' : '✈️ DHL Air Cargo'}
                        </small>
                    </td>
                    <td>$${(o.shippingCost || 0).toFixed(2)}</td>
                    <td><strong>$${(o.totalAmount || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}</strong></td>
                    <td><span class="badge-status ${statusBadge}">${o.status}</span></td>
                    <td>
                        <div style="display:flex; gap:4px; flex-wrap:wrap;">
                            <button class="btn-act btn-act-pack" onclick="updateOrderStatus(${o.id}, 'PICKING_PACKING')">📦 Pick</button>
                            <button class="btn-act btn-act-ready" onclick="updateOrderStatus(${o.id}, 'PACKED_READY')">✅ Pack</button>
                            <button class="btn-act btn-act-ship" onclick="updateOrderStatus(${o.id}, 'SHIPPED')">🚚 Ship</button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    // Render Single Warehouse Packing Queue
    function renderWarehousePackingTable() {
        if (!tblWarehousePacking) return;
        const selectedWh = selectWarehouseFilter ? selectWarehouseFilter.value : 'ALL';

        let filtered = cachedOrders;
        if (selectedWh !== 'ALL') {
            filtered = cachedOrders.filter(o => o.originCountry === selectedWh);
        }

        if (filtered.length === 0) {
            tblWarehousePacking.innerHTML = `<tr><td colspan="7" style="text-align: center; color: #94a3b8;">No orders queued for this warehouse depot.</td></tr>`;
            return;
        }

        tblWarehousePacking.innerHTML = filtered.map(o => {
            let statusBadge = 'status-processing';
            if (o.status === 'PICKING_PACKING') statusBadge = 'status-picking';
            else if (o.status === 'PACKED_READY') statusBadge = 'status-packed';
            else if (o.status === 'SHIPPED') statusBadge = 'status-shipped';

            return `
                <tr>
                    <td><strong>#${o.orderNumber}</strong></td>
                    <td>${o.customerName}</td>
                    <td><strong>${o.originCountry}</strong> (${o.warehouseName})</td>
                    <td><strong>${o.destinationCountry}</strong></td>
                    <td><strong>$${(o.totalAmount || 0).toFixed(2)}</strong></td>
                    <td><span class="badge-status ${statusBadge}">${o.status}</span></td>
                    <td>
                        <div style="display:flex; gap:6px;">
                            <button class="btn-act btn-act-pack" onclick="updateOrderStatus(${o.id}, 'PICKING_PACKING')">📦 Start Pick</button>
                            <button class="btn-act btn-act-ready" onclick="updateOrderStatus(${o.id}, 'PACKED_READY')">✅ Mark Packed</button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    // Load Admin Orders
    async function loadAdminOrders() {
        try {
            const res = await fetch(`${API_BASE}/admin/orders`);
            if (res.ok) {
                cachedOrders = await res.json();
                renderOrdersTable();
                renderWarehousePackingTable();
            }
        } catch (err) {
            console.error('Error fetching admin orders:', err);
        }
    }

    // Update Order Status
    window.updateOrderStatus = async function (orderId, newStatus) {
        try {
            const res = await fetch(`${API_BASE}/admin/orders/status`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({orderId, status: newStatus})
            });

            if (res.ok) {
                showAlert(`Order #${orderId} status updated to ${newStatus}!`, 'success');
                loadAdminOrders();
                loadDashboardStats();
            } else {
                showAlert('Failed to update order status.');
            }
        } catch (err) {
            showAlert('Network error updating order status.');
        }
    };

    // Load Admin Shipments
    async function loadAdminShipments() {
        if (!tblAdminShipments) return;
        try {
            const res = await fetch(`${API_BASE}/admin/shipments`);
            if (res.ok) {
                cachedShipments = await res.json();
                if (cachedShipments.length === 0) {
                    tblAdminShipments.innerHTML = `<tr><td colspan="8" style="text-align: center; color: #94a3b8;">No active freight shipments found.</td></tr>`;
                    return;
                }

                tblAdminShipments.innerHTML = cachedShipments.map(s => `
                    <tr>
                        <td>#SHIP-${s.id}</td>
                        <td><strong style="color:#38bdf8;">🚚 ${s.trackingNumber}</strong></td>
                        <td>#${s.orderNumber}</td>
                        <td><strong>${s.originCountry}</strong> (${s.originWarehouse}) ➔ <strong>${s.destinationCountry}</strong></td>
                        <td><span class="badge-status status-shipped">${s.shipmentType}</span></td>
                        <td>${s.estimatedDelivery}</td>
                        <td><span class="badge-status status-picking">${s.status}</span></td>
                        <td>
                            <button class="btn-act btn-act-ship" onclick="updateShipmentStatus(${s.id}, 'IN_TRANSIT')">✈️ In Transit</button>
                            <button class="btn-act btn-act-ready" onclick="updateShipmentStatus(${s.id}, 'DELIVERED')">✅ Delivered</button>
                        </td>
                    </tr>
                `).join('');
            }
        } catch (err) {
            console.error('Error fetching admin shipments:', err);
        }
    }

    // Update Shipment Status
    window.updateShipmentStatus = async function (shipmentId, newStatus) {
        try {
            const res = await fetch(`${API_BASE}/admin/shipments/status`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({shipmentId, status: newStatus})
            });

            if (res.ok) {
                showAlert(`Shipment #${shipmentId} tracking status updated to ${newStatus}!`, 'success');
                loadAdminShipments();
                loadAdminOrders();
                loadDashboardStats();
            } else {
                showAlert('Failed to update shipment status.');
            }
        } catch (err) {
            showAlert('Network error updating shipment status.');
        }
    };

    // Load Admin Customs Documents
    async function loadAdminCustoms() {
        if (!tblAdminCustoms) return;
        try {
            const res = await fetch(`${API_BASE}/admin/customs`);
            if (res.ok) {
                cachedCustoms = await res.json();
                if (cachedCustoms.length === 0) {
                    tblAdminCustoms.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">No customs clearance documents filed yet.</td></tr>`;
                    return;
                }

                tblAdminCustoms.innerHTML = cachedCustoms.map(c => {
                    const decVal = (c.declaredValue || 4890.00).toLocaleString(undefined, {minimumFractionDigits: 2});
                    const dutyVal = (c.dutyFee || 244.50).toLocaleString(undefined, {minimumFractionDigits: 2});
                    const route = `${c.originCountry || 'US'} ➔ ${c.destinationCountry || 'LK'}`;

                    return `
                        <tr>
                            <td>
                                <strong>#DOC-${c.id}</strong><br>
                                <span class="badge-status status-customs">${c.documentType}</span>
                            </td>
                            <td><strong style="color:#2563eb;">🚚 ${c.trackingNumber}</strong></td>
                            <td>
                                <strong>${route}</strong><br>
                                <small style="color:var(--text-muted);">${c.exporterName}</small>
                            </td>
                            <td>
                                <strong>$${decVal}</strong><br>
                                <small style="color:#b45309;">Duty Tax: $${dutyVal}</small>
                            </td>
                            <td><strong style="color:#0f766e;">HS ${c.hsCode}</strong></td>
                            <td><span class="badge-status ${c.status === 'APPROVED' ? 'status-packed' : 'status-processing'}">${c.status}</span></td>
                            <td><small>${c.inspectedBy}</small></td>
                            <td>
                                <button class="btn-act btn-act-customs" onclick="reviewCustoms(${c.id}, true)">🛡️ Approve Clearance</button>
                            </td>
                        </tr>
                    `;
                }).join('');
            }
        } catch (err) {
            console.error('Error fetching admin customs:', err);
        }
    }

    // Review Customs Document
    window.reviewCustoms = async function (documentId, approve) {
        try {
            const res = await fetch(`${API_BASE}/admin/customs/review`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    documentId,
                    approve,
                    officerName: 'Chief Officer #8902',
                    notes: 'Customs declaration inspected and passed compliance check.'
                })
            });

            if (res.ok) {
                showAlert(`Customs Document #${documentId} APPROVED & cleared!`, 'success');
                loadAdminCustoms();
                loadDashboardStats();
            } else {
                showAlert('Failed to review customs document.');
            }
        } catch (err) {
            showAlert('Network error reviewing customs document.');
        }
    };

    // Event Listeners
    filterOrderStatus?.addEventListener('change', renderOrdersTable);
    selectWarehouseFilter?.addEventListener('change', renderWarehousePackingTable);
    btnRefreshAdminOrders?.addEventListener('click', loadAdminOrders);
    btnRefreshShipments?.addEventListener('click', loadAdminShipments);
    btnRefreshCustoms?.addEventListener('click', loadAdminCustoms);

    // Initial Data Fetch
    loadDashboardStats();
    loadAdminOrders();
    loadAdminShipments();
    loadAdminCustoms();
});
