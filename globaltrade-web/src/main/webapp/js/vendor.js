document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('gt_access_token');
    const username = localStorage.getItem('gt_username') || 'vendor_bosch';
    const company = localStorage.getItem('gt_vendor_company') || 'Bosch Logistics GmbH';
    const taxid = localStorage.getItem('gt_vendor_taxid') || 'DE-TAX-998877';

    const venCompName = document.getElementById('venCompName');
    const venTaxCode = document.getElementById('venTaxCode');
    const venUserAcc = document.getElementById('venUserAcc');
    const venAccessToken = document.getElementById('venAccessToken');

    const venTotalProducts = document.getElementById('venTotalProducts');
    const venTotalStock = document.getElementById('venTotalStock');
    const vendorProductsTableBody = document.getElementById('vendorProductsTableBody');
    const vendorStockTableBody = document.getElementById('vendorStockTableBody');
    const btnRefreshVendorData = document.getElementById('btnRefreshVendorData');

    if (venCompName) venCompName.textContent = company;
    if (venTaxCode) venTaxCode.textContent = `Tax ID / TIN: ${taxid}`;
    if (venUserAcc) venUserAcc.textContent = `Account User: ${username}`;
    if (venAccessToken) venAccessToken.textContent = token || 'No active session token';

    const getApiBaseUrl = () => {
        const path = window.location.pathname;
        if (path.includes('/globaltrade-web')) {
            return '/globaltrade-web/api';
        }
        return '/api';
    };

    const API_BASE = getApiBaseUrl();

    // Fetch Products Catalog from Database REST API
    async function loadVendorProducts() {
        try {
            const res = await fetch(`${API_BASE}/inventory/products`);
            const products = await res.json();

            if (res.ok && Array.isArray(products)) {
                venTotalProducts.textContent = `${products.length} Products`;

                if (products.length === 0) {
                    vendorProductsTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">No products in database catalog.</td></tr>`;
                    return;
                }

                vendorProductsTableBody.innerHTML = products.map(p => `
                    <tr>
                        <td><span class="badge-status badge-active" style="background:#eff6ff; color:#1d4ed8;">${p.sku}</span></td>
                        <td><strong>${p.name}</strong><br><small style="color:var(--text-muted);">${p.description || '-'}</small></td>
                        <td>
                            <span class="badge-status badge-active" style="background:#f0fdf4; color:#166534;">🏷️ ${p.categoryName || 'General'}</span>
                            <span class="badge-status badge-active" style="background:#fdf4ff; color:#86198f;">🏢 ${p.brandName || 'Global'}</span>
                        </td>
                        <td><strong>$${p.price.toLocaleString(undefined, {minimumFractionDigits: 2})}</strong></td>
                        <td><strong>${p.weightKg || 1.0} kg</strong></td>
                        <td><span class="badge-status badge-active" style="background:#f1f5f9; color:#475569;">HS ${p.hsCode}</span></td>
                        <td><span class="badge-status badge-active">ACTIVE</span></td>
                    </tr>
                `).join('');
            } else {
                vendorProductsTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--error-color);">Failed to load vendor products catalog.</td></tr>`;
            }
        } catch (err) {
            console.error('Error loading vendor products:', err);
            vendorProductsTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--error-color);">Network error loading catalog.</td></tr>`;
        }
    }

    // Fetch Warehouse Inventories from Database REST API
    async function loadVendorStockAllocations() {
        try {
            const res = await fetch(`${API_BASE}/inventory/items`);
            const items = await res.json();

            if (res.ok && Array.isArray(items)) {
                let totalStockSum = 0;
                items.forEach(i => totalStockSum += (i.availableQty || 0));
                venTotalStock.textContent = `${totalStockSum} Stock Units`;

                if (items.length === 0) {
                    vendorStockTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">No stock allocations registered in warehouses.</td></tr>`;
                    return;
                }

                vendorStockTableBody.innerHTML = items.map(item => {
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
            } else {
                vendorStockTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--error-color);">Failed to load stock allocations.</td></tr>`;
            }
        } catch (err) {
            console.error('Error loading vendor stock:', err);
            vendorStockTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--error-color);">Network error loading stock allocations.</td></tr>`;
        }
    }

    btnRefreshVendorData?.addEventListener('click', () => {
        loadVendorProducts();
        loadVendorStockAllocations();
    });

    document.getElementById('btnVendorLogout')?.addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'index.html';
    });

    // Initial Load
    loadVendorProducts();
    loadVendorStockAllocations();
});
