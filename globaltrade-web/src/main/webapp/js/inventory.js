document.addEventListener('DOMContentLoaded', () => {
    const addProductForm = document.getElementById('addProductForm');
    const addInventoryForm = document.getElementById('addInventoryForm');
    const addCategoryForm = document.getElementById('addCategoryForm');
    const addBrandForm = document.getElementById('addBrandForm');
    const alertBox = document.getElementById('alertBox');

    const invProduct = document.getElementById('invProduct');
    const invVendor = document.getElementById('invVendor');
    const invWarehouse = document.getElementById('invWarehouse');

    const inventoryTableBody = document.getElementById('inventoryTableBody');
    const allProductsTableBody = document.getElementById('allProductsTableBody');
    const categoriesTableBody = document.getElementById('categoriesTableBody');
    const brandsTableBody = document.getElementById('brandsTableBody');

    const btnRefreshManifest = document.getElementById('btnRefreshManifest');
    const btnRefreshProducts = document.getElementById('btnRefreshProducts');

    const getApiBaseUrl = () => {
        const path = window.location.pathname;
        if (path.includes('/globaltrade-web')) {
            return '/globaltrade-web/api';
        }
        return '/api';
    };

    const API_BASE = getApiBaseUrl();

    // TAB SWITCHING LOGIC
    document.querySelectorAll('.tab-btn-item').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.tab-btn-item').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));

            btn.classList.add('active');
            const targetId = btn.getAttribute('data-tab');
            const targetPane = document.getElementById(targetId);
            if (targetPane) targetPane.classList.add('active');
        });
    });

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

    // Load Master Products Catalog & Populates Select
    async function loadProducts() {
        try {
            const res = await fetch(`${API_BASE}/inventory/products`);
            const products = await res.json();

            if (res.ok && Array.isArray(products)) {
                if (products.length === 0) {
                    invProduct.innerHTML = `<option value="">No Products Found - Create One First</option>`;
                    allProductsTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">No products registered yet.</td></tr>`;
                    return;
                }

                invProduct.innerHTML = products.map(p => `
                    <option value="${p.id}">${p.sku} - ${p.name} ($${p.price})</option>
                `).join('');

                allProductsTableBody.innerHTML = products.map(p => `
                    <tr>
                        <td>#${p.id}</td>
                        <td><span class="badge-status badge-active" style="background:#eff6ff; color:#1d4ed8;">${p.sku}</span></td>
                        <td><strong>${p.name}</strong></td>
                        <td><span class="badge-status badge-active" style="background:#f0fdf4; color:#166534;">🏷️ ${p.categoryName || 'General'}</span></td>
                        <td><span class="badge-status badge-active" style="background:#fdf4ff; color:#86198f;">🏢 ${p.brandName || 'Global'}</span></td>
                        <td><strong>$${p.price.toLocaleString(undefined, {minimumFractionDigits: 2})}</strong></td>
                        <td><strong>${p.weightKg || 1.0} kg</strong></td>
                        <td><span class="badge-status badge-active" style="background:#f1f5f9; color:#475569;">HS ${p.hsCode}</span></td>
                        <td>${p.description || '-'}</td>
                        <td><span class="badge-status badge-active">ACTIVE</span></td>
                    </tr>
                `).join('');
            }
        } catch (err) {
            console.error('Error loading products:', err);
        }
    }

    // Load Vendors
    async function loadVendors() {
        try {
            const res = await fetch(`${API_BASE}/inventory/vendors`);
            const vendors = await res.json();

            if (res.ok && Array.isArray(vendors)) {
                invVendor.innerHTML = vendors.map(v => `
                    <option value="${v.id}">${v.companyName} (${v.taxId})</option>
                `).join('');
            }
        } catch (err) {
            console.error('Error loading vendors:', err);
        }
    }

    // Load Warehouses
    async function loadWarehouses() {
        try {
            const res = await fetch(`${API_BASE}/warehouses`);
            const warehouses = await res.json();

            if (res.ok && Array.isArray(warehouses)) {
                invWarehouse.innerHTML = warehouses.map(w => `
                    <option value="${w.id}">${w.name} (${w.countryCode || 'DE'})</option>
                `).join('');
            }
        } catch (err) {
            console.error('Error loading warehouses:', err);
        }
    }

    // Load Inventory Manifest
    async function loadInventories() {
        try {
            const res = await fetch(`${API_BASE}/inventory/items`);
            const items = await res.json();

            if (res.ok && Array.isArray(items)) {
                if (items.length === 0) {
                    inventoryTableBody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">No stock allocations registered in warehouses.</td></tr>`;
                    return;
                }

                inventoryTableBody.innerHTML = items.map(item => {
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
                            <td><span class="badge-status badge-active" style="background:#f1f5f9; color:#475569;">HS ${item.hsCode}</span></td>
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
                inventoryTableBody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--error-color);">Failed to load inventory manifest.</td></tr>`;
            }
        } catch (err) {
            console.error('Error loading inventory items:', err);
            inventoryTableBody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--error-color);">Network error loading inventory manifest.</td></tr>`;
        }
    }

    // Load Categories
    async function loadCategories() {
        try {
            const res = await fetch(`${API_BASE}/inventory/categories`);
            const categories = await res.json();
            if (res.ok && Array.isArray(categories)) {
                const prodCategory = document.getElementById('prodCategory');
                if (prodCategory) {
                    prodCategory.innerHTML = `<option value="">Select Category (Optional)</option>` +
                        categories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
                }
                categoriesTableBody.innerHTML = categories.map(c => `
                    <tr><td>#${c.id}</td><td><strong>${c.name}</strong></td></tr>
                `).join('');
            }
        } catch (err) {
            console.error('Error loading categories:', err);
        }
    }

    // Load Brands
    async function loadBrands() {
        try {
            const res = await fetch(`${API_BASE}/inventory/brands`);
            const brands = await res.json();
            if (res.ok && Array.isArray(brands)) {
                const prodBrand = document.getElementById('prodBrand');
                if (prodBrand) {
                    prodBrand.innerHTML = `<option value="">Select Brand (Optional)</option>` +
                        brands.map(b => `<option value="${b.id}">${b.name}</option>`).join('');
                }
                brandsTableBody.innerHTML = brands.map(b => `
                    <tr><td>#${b.id}</td><td><strong>${b.name}</strong></td></tr>
                `).join('');
            }
        } catch (err) {
            console.error('Error loading brands:', err);
        }
    }

    // Form Handlers
    addProductForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();
        const btn = document.getElementById('btnAddProduct');
        const sku = document.getElementById('prodSku').value.trim();
        const name = document.getElementById('prodName').value.trim();
        const price = parseFloat(document.getElementById('prodPrice').value);
        const weightKg = parseFloat(document.getElementById('prodWeight').value) || 1.0;
        const hsCode = document.getElementById('prodHsCode').value;
        const description = document.getElementById('prodDescription').value.trim();
        const categoryId = parseInt(document.getElementById('prodCategory')?.value) || null;
        const brandId = parseInt(document.getElementById('prodBrand')?.value) || null;

        setLoading(btn, true);

        try {
            const res = await fetch(`${API_BASE}/inventory/products`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({sku, name, price, weightKg, hsCode, description, categoryId, brandId})
            });

            const data = await res.json();

            if (res.ok || res.status === 201) {
                showAlert(`🎉 Master Product "${data.name}" (${data.sku}) created successfully!`, 'success');
                addProductForm.reset();
                loadProducts();
            } else {
                showAlert(data.error || 'Failed to create product.');
            }
        } catch (err) {
            showAlert('Network error creating product.');
        } finally {
            setLoading(btn, false);
        }
    });

    addInventoryForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();
        const btn = document.getElementById('btnAddInventory');
        const productId = parseInt(invProduct.value);
        const vendorId = parseInt(invVendor.value);
        const warehouseId = parseInt(invWarehouse.value);
        const unitPrice = parseFloat(document.getElementById('invUnitPrice').value);
        const availableQty = parseInt(document.getElementById('invAvailableQty').value);
        const reorderThreshold = parseInt(document.getElementById('invReorder').value) || 50;

        setLoading(btn, true);

        try {
            const res = await fetch(`${API_BASE}/inventory/items`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({warehouseId, productId, vendorId, unitPrice, availableQty, reorderThreshold})
            });

            const data = await res.json();

            if (res.ok || res.status === 201) {
                showAlert(`🏢 Stock allocated to ${data.warehouse}! Available Qty: ${data.availableQty}`, 'success');
                addInventoryForm.reset();
                loadInventories();
            } else {
                showAlert(data.error || 'Failed to allocate inventory stock.');
            }
        } catch (err) {
            showAlert('Network error allocating inventory stock.');
        } finally {
            setLoading(btn, false);
        }
    });

    addCategoryForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('catName').value.trim();
        if (!name) return;
        try {
            const res = await fetch(`${API_BASE}/inventory/categories`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({name})
            });
            if (res.ok) {
                addCategoryForm.reset();
                loadCategories();
                showAlert(`Category "${name}" added!`, 'success');
            }
        } catch (err) {
            console.error(err);
        }
    });

    addBrandForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('brandName').value.trim();
        if (!name) return;
        try {
            const res = await fetch(`${API_BASE}/inventory/brands`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({name})
            });
            if (res.ok) {
                addBrandForm.reset();
                loadBrands();
                showAlert(`Brand "${name}" added!`, 'success');
            }
        } catch (err) {
            console.error(err);
        }
    });

    // Load Stock Movement Audit Log (inventory_transactions Table)
    async function loadTransactions() {
        const inventoryTxBody = document.getElementById('inventoryTxBody');
        if (!inventoryTxBody) return;

        try {
            const res = await fetch(`${API_BASE}/inventory/transactions`);
            const txs = await res.json();

            if (res.ok && Array.isArray(txs)) {
                if (txs.length === 0) {
                    inventoryTxBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">No stock movement audit records yet.</td></tr>`;
                    return;
                }

                inventoryTxBody.innerHTML = txs.map(t => {
                    const isOut = t.transactionType === 'OUTBOUND_SHIPMENT' || t.transactionType === 'DAMAGE_LOSS';
                    const badgeBg = isOut ? '#fee2e2' : '#dcfce7';
                    const badgeClr = isOut ? '#991b1b' : '#15803d';
                    const qtyPrefix = isOut ? '-' : '+';

                    return `
                        <tr>
                            <td><strong>#TX-${t.id}</strong></td>
                            <td>
                                <strong>${t.productName}</strong><br>
                                <small style="color:var(--text-muted); font-size:10px;">Depot: ${t.warehouseName}</small>
                            </td>
                            <td><span class="badge-status badge-active" style="background:${badgeBg}; color:${badgeClr}; font-size:10px;">${t.transactionType}</span></td>
                            <td><strong style="color:${isOut ? '#dc2626' : '#059669'};">${qtyPrefix}${Math.abs(t.quantityChanged)} Units</strong></td>
                            <td><small style="color:#334155; font-weight:600;">${t.performedBy}</small></td>
                            <td><small style="color:#64748b;">${t.timestamp ? t.timestamp.replace('T', ' ').substring(0, 16) : 'Recent'}</small></td>
                        </tr>
                    `;
                }).join('');
            } else {
                inventoryTxBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--error-color);">Failed to load audit history.</td></tr>`;
            }
        } catch (err) {
            console.error('Error fetching inventory transactions:', err);
            inventoryTxBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--error-color);">Network error loading audit history.</td></tr>`;
        }
    }

    const btnRefreshTransactions = document.getElementById('btnRefreshTransactions');
    btnRefreshTransactions?.addEventListener('click', loadTransactions);

    btnRefreshManifest?.addEventListener('click', () => {
        loadInventories();
        loadTransactions();
    });
    btnRefreshProducts?.addEventListener('click', loadProducts);

    // Initial Load
    loadProducts();
    loadVendors();
    loadWarehouses();
    loadInventories();
    loadTransactions();
    loadCategories();
    loadBrands();
});
