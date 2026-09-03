document.addEventListener('DOMContentLoaded', () => {
    const selectWarehouseFilter = document.getElementById('selectWarehouseFilter');
    const warehouseOrdersTableBody = document.getElementById('warehouseOrdersTableBody');
    const depotStockTableBody = document.getElementById('depotStockTableBody');
    const alertBox = document.getElementById('alertBox');
    const btnRefreshOrders = document.getElementById('btnRefreshOrders');

    const metricTotalOrders = document.getElementById('metricTotalOrders');
    const metricPacking = document.getElementById('metricPacking');
    const metricPackedReady = document.getElementById('metricPackedReady');
    const metricDepotStock = document.getElementById('metricDepotStock');

    const getApiBaseUrl = () => {
        const path = window.location.pathname;
        if (path.includes('/globaltrade-web')) {
            return '/globaltrade-web/api';
        }
        return '/api';
    };

    const API_BASE = getApiBaseUrl();

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

    // Load Orders List for Warehouse View
    async function loadWarehouseOrders() {
        const whFilter = selectWarehouseFilter.value; // e.g. "ALL", "LK", "DE", "JP", "US", "SG"

        try {
            const res = await fetch(`${API_BASE}/storefront/orders`);
            const orders = await res.json();

            if (res.ok && Array.isArray(orders)) {
                // Filter orders by warehouse destination / origin match if needed
                let filtered = orders;
                if (whFilter !== 'ALL') {
                    filtered = orders.filter(o => o.destinationCountry === whFilter || o.destinationCountry === 'LK');
                }

                let totalCount = filtered.length;
                let packingCount = 0;
                let packedReadyCount = 0;

                filtered.forEach(o => {
                    if (o.status === 'PICKING_PACKING') packingCount++;
                    else if (o.status === 'PACKED_READY') packedReadyCount++;
                });

                metricTotalOrders.textContent = `${totalCount} Orders`;
                metricPacking.textContent = `${packingCount} Orders`;
                metricPackedReady.textContent = `${packedReadyCount} Orders`;

                if (filtered.length === 0) {
                    warehouseOrdersTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">No allocated orders for this warehouse depot yet. Place an order on customer.html</td></tr>`;
                    return;
                }

                warehouseOrdersTableBody.innerHTML = filtered.map(o => {
                    const itemsSummary = o.items && o.items.length > 0
                        ? o.items.map(i => `${i.productName} (x${i.qty})`).join(', ')
                        : 'Dell XPS 15 Laptop (x1)';

                    let statusClass = 'badge-pending';
                    if (o.status === 'PACKED_READY' || o.status === 'SHIPPED') statusClass = 'badge-active';
                    else if (o.status === 'PICKING_PACKING') statusClass = 'badge-alert';

                    return `
                        <tr>
                            <td><strong style="color:#2563eb;">#${o.orderNumber || 'ORD-2026-901'}</strong></td>
                            <td><strong>${o.customerName}</strong><br><small style="color:var(--text-muted);">${o.email}</small></td>
                            <td><span class="badge-status badge-active" style="background:#eff6ff; color:#1d4ed8;">${o.destinationCountry || 'LK'}</span></td>
                            <td><small style="font-weight:600; color:#334155;">${itemsSummary}</small></td>
                            <td><strong>$${(o.totalAmount || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}</strong></td>
                            <td><span class="badge-status ${statusClass}">${o.status}</span></td>
                            <td>
                                <div style="display:flex; gap:6px; flex-wrap:wrap;">
                                    <button type="button" class="btn-primary btn-pack-start" data-id="${o.id}" style="width:auto; margin-top:0; padding:4px 8px; font-size:11px; background:#d97706;">
                                        📦 Pick & Pack
                                    </button>
                                    <button type="button" class="btn-primary btn-pack-ready" data-id="${o.id}" style="width:auto; margin-top:0; padding:4px 8px; font-size:11px; background:#059669;">
                                        ✅ Packed & Ready
                                    </button>
                                </div>
                            </td>
                        </tr>
                    `;
                }).join('');

                // Attach status update button listeners
                document.querySelectorAll('.btn-pack-start').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const id = parseInt(btn.getAttribute('data-id'));
                        updateStatus(id, 'PICKING_PACKING');
                    });
                });

                document.querySelectorAll('.btn-pack-ready').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const id = parseInt(btn.getAttribute('data-id'));
                        updateStatus(id, 'PACKED_READY');
                    });
                });

            } else {
                warehouseOrdersTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--error-color);">Failed to load warehouse orders.</td></tr>`;
            }
        } catch (err) {
            console.error('Error loading warehouse orders:', err);
            warehouseOrdersTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--error-color);">Network error loading orders.</td></tr>`;
        }
    }

    // Load Single Warehouse Inventory Stock Manifest
    async function loadDepotStock() {
        const whFilter = selectWarehouseFilter.value;

        try {
            const res = await fetch(`${API_BASE}/inventory/items`);
            const items = await res.json();

            if (res.ok && Array.isArray(items)) {
                let filteredItems = items;
                if (whFilter !== 'ALL') {
                    filteredItems = items.filter(i => (i.warehouseName && i.warehouseName.toUpperCase().includes(whFilter)) || whFilter === 'LK');
                }

                let totalStockSum = 0;
                filteredItems.forEach(i => totalStockSum += (i.availableQty || 0));
                metricDepotStock.textContent = `${totalStockSum} Units`;

                if (filteredItems.length === 0) {
                    depotStockTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">No stock items registered for this warehouse depot.</td></tr>`;
                    return;
                }

                depotStockTableBody.innerHTML = filteredItems.map(item => {
                    const isLowStock = item.availableQty <= item.reorderThreshold;
                    return `
                        <tr>
                            <td><strong>${item.warehouseName}</strong></td>
                            <td>
                                <strong>${item.productName}</strong><br>
                                <small style="color:var(--text-muted);">${item.productSku}</small>
                            </td>
                            <td>${item.vendorName}</td>
                            <td><strong>$${item.unitPrice.toLocaleString(undefined, {minimumFractionDigits: 2})}</strong></td>
                            <td><strong style="color: ${isLowStock ? '#ef4444' : '#059669'};">${item.availableQty} Units</strong></td>
                            <td>${item.reorderThreshold} Units</td>
                            <td>
                                <span class="badge-status ${isLowStock ? 'badge-pending' : 'badge-active'}">
                                    ${isLowStock ? 'LOW STOCK ALERT' : 'IN STOCK'}
                                </span>
                            </td>
                        </tr>
                    `;
                }).join('');
            }
        } catch (err) {
            console.error('Error loading depot stock:', err);
        }
    }

    // Update Order Status via REST API
    async function updateStatus(orderId, status) {
        hideAlert();
        try {
            const res = await fetch(`${API_BASE}/storefront/orders/status`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({orderId, status})
            });

            const data = await res.json();

            if (res.ok) {
                showAlert(`🏬 Order #${orderId} status updated to ${status}!`, 'success');
                loadWarehouseOrders();
            } else {
                showAlert(data.error || 'Status update failed.');
            }
        } catch (err) {
            showAlert('Network error updating status.');
        }
    }

    selectWarehouseFilter?.addEventListener('change', () => {
        loadWarehouseOrders();
        loadDepotStock();
    });

    btnRefreshOrders?.addEventListener('click', () => {
        loadWarehouseOrders();
        loadDepotStock();
    });

    // Initial Load
    loadWarehouseOrders();
    loadDepotStock();
});
