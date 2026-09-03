document.addEventListener('DOMContentLoaded', () => {
    const productsGrid = document.getElementById('productsGrid');
    const cartItemsList = document.getElementById('cartItemsList');
    const cartCountBadge = document.getElementById('cartCountBadge');
    const cartSubtotal = document.getElementById('cartSubtotal');
    const shippingLabel = document.getElementById('shippingLabel');
    const shippingCostText = document.getElementById('shippingCostText');
    const shippingTypeDesc = document.getElementById('shippingTypeDesc');
    const cartTotal = document.getElementById('cartTotal');
    const alertBox = document.getElementById('alertBox');

    const checkoutForm = document.getElementById('checkoutForm');
    const custWarehouseOrigin = document.getElementById('custWarehouseOrigin');
    const custDestCountry = document.getElementById('custDestCountry');
    const ordersTableBody = document.getElementById('ordersTableBody');
    const btnRefreshCatalog = document.getElementById('btnRefreshCatalog');

    // Smart Auto-Routing UI Elements
    const smartWhName = document.getElementById('smartWhName');
    const autoRoutedFlag = document.getElementById('autoRoutedFlag');
    const autoRoutedName = document.getElementById('autoRoutedName');
    const autoRoutedReason = document.getElementById('autoRoutedReason');
    const autoRoutedBadge = document.getElementById('autoRoutedBadge');

    // Tracking Modal Elements
    const modalOrderTracking = document.getElementById('modalOrderTracking');
    const btnCloseTrackModal = document.getElementById('btnCloseTrackModal');
    const btnDoneTrackModal = document.getElementById('btnDoneTrackModal');
    const btnConfirmDeliveryModal = document.getElementById('btnConfirmDeliveryModal');

    const trackModalOrderRef = document.getElementById('trackModalOrderRef');
    const trackModalStatusBadge = document.getElementById('trackModalStatusBadge');
    const trackModalCustomerDesc = document.getElementById('trackModalCustomerDesc');
    const trackModalStepper = document.getElementById('trackModalStepper');
    const trackModalProductItems = document.getElementById('trackModalProductItems');
    const trackModalCarrierName = document.getElementById('trackModalCarrierName');
    const trackModalOriginDepot = document.getElementById('trackModalOriginDepot');
    const trackModalCustomsStatus = document.getElementById('trackModalCustomsStatus');
    const trackModalDutyStatus = document.getElementById('trackModalDutyStatus');
    const trackModalEstimate = document.getElementById('trackModalEstimate');

    const API_BASE = window.location.pathname.includes('/globaltrade-web') ? '/globaltrade-web/api' : '/api';

    let cart = [];
    let currentWarehouseId = 2; // Default Colombo LK
    let customerOrdersCache = [];
    let currentActiveModalOrderId = null;

    // Pre-defined Warehouse Mapping
    const WAREHOUSES = {
        'LK': {
            id: 2,
            name: 'Colombo Logistics Depot (LK)',
            flag: '🇱🇰',
            country: 'LK',
            desc: 'Optimal fulfillment for South Asian region'
        },
        'DE': {
            id: 1,
            name: 'Frankfurt Central Depot (DE)',
            flag: '🇩🇪',
            country: 'DE',
            desc: 'Central hub for European Union & UK'
        },
        'US': {id: 4, name: 'USA New York Air Hub (US)', flag: '🇺🇸', country: 'US', desc: 'North America express hub'}
    };

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
        if (isLoading) {
            btn.classList.add('loading');
            btn.disabled = true;
        } else {
            btn.classList.remove('loading');
            btn.disabled = false;
        }
    }

    // Auto-Routing Calculation
    function updateWarehouseAutoRouting(destCountry) {
        let bestWhKey = 'LK';
        if (destCountry === 'LK' || destCountry === 'SG' || destCountry === 'JP' || destCountry === 'AU') {
            bestWhKey = 'LK';
        } else if (destCountry === 'DE' || destCountry === 'GB') {
            bestWhKey = 'DE';
        } else if (destCountry === 'US' || destCountry === 'AE') {
            bestWhKey = 'US';
        }

        const wh = WAREHOUSES[bestWhKey];
        currentWarehouseId = wh.id;

        if (custWarehouseOrigin) {
            custWarehouseOrigin.value = `${wh.country}|${wh.id}`;
        }

        if (smartWhName) smartWhName.textContent = `Auto-Assigned Depot: ${wh.name}`;
        if (autoRoutedFlag) autoRoutedFlag.textContent = wh.flag;
        if (autoRoutedName) autoRoutedName.textContent = wh.name;
        if (autoRoutedReason) autoRoutedReason.textContent = wh.desc;
        if (autoRoutedBadge) {
            autoRoutedBadge.textContent = destCountry === wh.country ? '⚡ Same Country Direct' : '🌍 Nearest Regional Hub';
        }
    }

    // Shipping Cost Calculation
    function calculateShippingCost() {
        const destCountry = custDestCountry ? custDestCountry.value : 'LK';
        updateWarehouseAutoRouting(destCountry);

        const warehouseVal = custWarehouseOrigin ? custWarehouseOrigin.value : 'LK|2';
        const originCountry = warehouseVal.split('|')[0];
        const isDomestic = (originCountry === destCountry);

        const totalWeightKg = cart.reduce((sum, item) => sum + ((item.weightKg || 1.0) * item.qty), 0);

        let shippingCost = 0;
        if (isDomestic) {
            shippingCost = 15.00 + (totalWeightKg * 2.50);
            if (shippingLabel) shippingLabel.textContent = 'Domestic Express Freight:';
            if (shippingTypeDesc) shippingTypeDesc.textContent = `🟢 Local Express Ground Dispatch (${originCountry} ➔ ${destCountry})`;
        } else {
            shippingCost = 65.00 + (totalWeightKg * 12.00);
            if (shippingLabel) shippingLabel.textContent = 'Air Cargo Freight & Customs Duty:';
            if (shippingTypeDesc) shippingTypeDesc.textContent = `✈️ Cross-Border DHL/FedEx Air Cargo (${originCountry} ➔ ${destCountry})`;
        }

        const subtotal = cart.reduce((sum, item) => sum + (item.price * item.qty), 0);
        const grandTotal = subtotal + shippingCost;

        if (shippingCostText) shippingCostText.textContent = `$${shippingCost.toFixed(2)}`;
        if (cartSubtotal) cartSubtotal.textContent = `$${subtotal.toFixed(2)}`;
        if (cartTotal) cartTotal.textContent = `$${grandTotal.toFixed(2)}`;

        return {subtotal, shippingCost, grandTotal, isDomestic, originCountry};
    }

    custDestCountry?.addEventListener('change', calculateShippingCost);

    // Load Catalog Products
    async function loadCatalog() {
        try {
            const res = await fetch(`${API_BASE}/inventory/products`);
            const products = await res.json();

            if (res.ok && Array.isArray(products)) {
                if (products.length === 0) {
                    productsGrid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; color: var(--text-muted);">No products found in inventory.</div>`;
                    return;
                }

                productsGrid.innerHTML = products.map(p => {
                    const priceFormatted = (p.price || 0).toFixed(2);
                    const stock = p.availableStock !== undefined ? p.availableStock : 50;
                    const stockClass = stock > 10 ? 'badge-active' : (stock > 0 ? 'badge-alert' : 'badge-alert');

                    return `
                        <div class="product-card">
                            <div>
                                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                                    <span class="user-badge" style="background:#e0f2fe; color:#0369a1; font-size:10px;">${p.sku || 'SKU-LOG'}</span>
                                    <span class="badge-status ${stockClass}" style="font-size:10px;">${stock > 0 ? stock + ' in stock' : 'Out of Stock'}</span>
                                </div>
                                <h4 style="font-size: 14px; font-weight: 700; color: var(--text-dark); margin: 0 0 4px 0;">${p.name}</h4>
                                <p style="font-size: 11px; color: var(--text-muted); margin: 0 0 12px 0; line-height: 1.4;">${p.description || 'High-performance global logistics catalog item'}</p>
                            </div>

                            <div>
                                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                                    <span style="font-size: 16px; font-weight: 700; color: var(--primary-color);">$${priceFormatted}</span>
                                    <span style="font-size: 11px; color: #64748b; background: #f1f5f9; padding: 2px 6px; border-radius: 4px;">📦 ${p.weightKg || 1.2} kg</span>
                                </div>

                                <div style="display: flex; gap: 8px;">
                                    <div style="display: flex; border: 1px solid var(--border-color); border-radius: 6px; overflow: hidden; width: 80px;">
                                        <button type="button" class="btn-qty-sub" data-id="${p.id}" style="width: 24px; border:none; background:#f1f5f9; cursor:pointer;">-</button>
                                        <input type="number" id="qty_${p.id}" value="1" min="1" max="${stock}" style="width: 32px; border:none; text-align:center; font-size:12px;">
                                        <button type="button" class="btn-qty-add" data-id="${p.id}" style="width: 24px; border:none; background:#f1f5f9; cursor:pointer;">+</button>
                                    </div>

                                    <button type="button" class="btn-primary btn-add-cart" data-id="${p.id}" data-sku="${p.sku}" data-name="${p.name}" data-price="${p.price}" data-weight="${p.weightKg || 1.0}" style="flex:1; margin-top:0; padding:6px 10px; font-size:12px;">
                                        🛒 Add to Cart
                                    </button>
                                </div>
                            </div>
                        </div>
                    `;
                }).join('');

                // Quantity selector button handlers
                document.querySelectorAll('.btn-qty-sub').forEach(b => {
                    b.addEventListener('click', () => {
                        const id = b.getAttribute('data-id');
                        const inp = document.getElementById(`qty_${id}`);
                        if (inp && parseInt(inp.value) > 1) inp.value = parseInt(inp.value) - 1;
                    });
                });

                document.querySelectorAll('.btn-qty-add').forEach(b => {
                    b.addEventListener('click', () => {
                        const id = b.getAttribute('data-id');
                        const inp = document.getElementById(`qty_${id}`);
                        if (inp) inp.value = parseInt(inp.value) + 1;
                    });
                });

                // Attach Add to Cart Click Handlers
                document.querySelectorAll('.btn-add-cart').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const id = parseInt(btn.getAttribute('data-id'));
                        const sku = btn.getAttribute('data-sku');
                        const name = btn.getAttribute('data-name');
                        const price = parseFloat(btn.getAttribute('data-price'));
                        const weightKg = parseFloat(btn.getAttribute('data-weight')) || 1.0;
                        const qtyInput = document.getElementById(`qty_${id}`);
                        const qty = qtyInput ? (parseInt(qtyInput.value) || 1) : 1;

                        addToCart(id, sku, name, price, weightKg, qty);
                    });
                });
            } else {
                productsGrid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; color: var(--error-color);">Failed to load Tech Mart catalog.</div>`;
            }
        } catch (err) {
            console.error('Error loading storefront catalog:', err);
            productsGrid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; color: var(--error-color);">Network error loading catalog.</div>`;
        }
    }

    // Cart Operations
    function addToCart(id, sku, name, price, weightKg, qtyToAdd = 1) {
        const existing = cart.find(item => item.id === id);
        if (existing) {
            existing.qty += qtyToAdd;
        } else {
            cart.push({id, sku, name, price, weightKg, qty: qtyToAdd});
        }
        calculateShippingCost();
        showAlert(`Added ${qtyToAdd}x "${name}" (${(weightKg * qtyToAdd).toFixed(2)} kg) to shopping cart!`, 'success');
    }

    function removeFromCart(id) {
        cart = cart.filter(item => item.id !== id);
        calculateShippingCost();
    }

    function updateCartUI() {
        if (cartCountBadge) cartCountBadge.textContent = `${cart.reduce((s, i) => s + i.qty, 0)} Items`;

        if (cart.length === 0) {
            cartItemsList.innerHTML = `
                <p style="font-size: 13px; color: var(--text-muted); text-align: center; padding: 16px 0;">
                    Your shopping cart is empty.<br>Choose quantity & click "Add to Cart" to start.
                </p>
            `;
            return;
        }

        cartItemsList.innerHTML = cart.map(item => `
            <div class="cart-item-row">
                <div>
                    <strong style="color: var(--text-dark);">${item.name}</strong><br>
                    <small style="color: var(--text-muted);">$${item.price.toFixed(2)} × ${item.qty}</small>
                </div>
                <div style="display: flex; align-items: center; gap: 8px;">
                    <span style="font-weight: 700; color: var(--primary-color);">$${(item.price * item.qty).toFixed(2)}</span>
                    <button type="button" class="btn-remove-item" data-id="${item.id}" style="background:none; border:none; color:#ef4444; cursor:pointer; font-weight:700;">✕</button>
                </div>
            </div>
        `).join('');

        document.querySelectorAll('.btn-remove-item').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = parseInt(btn.getAttribute('data-id'));
                removeFromCart(id);
            });
        });
    }

    // Load Customer Orders History
    async function loadCustomerOrders() {
        try {
            const res = await fetch(`${API_BASE}/storefront/orders`);
            const orders = await res.json();

            if (res.ok && Array.isArray(orders)) {
                customerOrdersCache = orders;

                if (orders.length === 0) {
                    ordersTableBody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">No placed orders yet. Submit your first order above!</td></tr>`;
                    return;
                }

                ordersTableBody.innerHTML = orders.map(o => {
                    const trackingNo = o.trackingNumber || `TRK-DHL-${89100 + o.id}`;
                    const originCode = o.originCountryCode || 'DE';
                    const whName = WAREHOUSES[originCode] ? WAREHOUSES[originCode].name.split(' ')[0] : 'Frankfurt';
                    const isDom = (originCode === o.destinationCountry);
                    const isDelivered = (o.status === 'DELIVERED');

                    let statusBadgeClass = 'badge-active';
                    if (isDelivered) statusBadgeClass = 'badge-active';
                    else if (o.status === 'SHIPPED' || o.status === 'IN_TRANSIT') statusBadgeClass = 'badge-active';
                    else if (o.status === 'IN_CUSTOMS' || o.status === 'CUSTOMS_HOLD') statusBadgeClass = 'badge-alert';

                    const carrierName = o.carrierName || (isDom ? '🚚 Sri Lanka Ground Carrier' : '✈️ DHL Express International Air Fleet');

                    return `
                        <tr class="order-row-clickable" data-id="${o.id}" style="cursor: pointer;">
                            <td>
                                <strong>#${o.orderNumber || ('ORD-2026-00' + o.id)}</strong><br>
                                <small style="color:#2563eb; font-weight:600;">🚚 ${trackingNo}</small>
                            </td>
                            <td>
                                <strong>${o.customerName || 'Customer Client'}</strong><br>
                                <small style="color:var(--text-muted);">${o.email || '-'}</small>
                            </td>
                            <td>
                                <strong>${originCode}</strong> (${whName}) ➔ <strong>${o.destinationCountry}</strong><br>
                                <small style="color:${isDom ? '#047857' : '#2563eb'}; font-weight:600;">
                                    ${carrierName}
                                </small>
                            </td>
                            <td>$${(o.shippingCost || 0).toFixed(2)}</td>
                            <td><strong>$${(o.totalAmount || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}</strong></td>
                            <td>${o.deliveryEstimate || (isDom ? '1-2 Days' : '3-5 Business Days')}</td>
                            <td><span class="badge-status ${statusBadgeClass}">${isDelivered ? '✅ DELIVERED' : o.status}</span></td>
                            <td>
                                <div style="display:flex; gap:6px; flex-wrap:wrap;">
                                    <button type="button" class="btn-primary btn-track-journey" data-id="${o.id}" style="padding: 4px 10px; font-size: 11px; margin: 0; background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);">
                                        🔍 Track Journey
                                    </button>
                                    ${!isDelivered ? `
                                        <button type="button" class="btn-primary btn-mark-received" data-id="${o.id}" style="padding: 4px 10px; font-size: 11px; margin: 0; background: linear-gradient(135deg, #059669 0%, #047857 100%);">
                                            📦 Mark Received
                                        </button>
                                    ` : `
                                        <span style="font-size:11px; color:#059669; font-weight:700;">✓ Received</span>
                                    `}
                                </div>
                            </td>
                        </tr>
                    `;
                }).join('');

                // Attach click handlers to open Tracking Modal & Mark Received
                document.querySelectorAll('.btn-track-journey').forEach(btn => {
                    btn.addEventListener('click', (e) => {
                        e.stopPropagation();
                        const id = parseInt(btn.getAttribute('data-id'));
                        openTrackingModal(id);
                    });
                });

                document.querySelectorAll('.btn-mark-received').forEach(btn => {
                    btn.addEventListener('click', (e) => {
                        e.stopPropagation();
                        const id = parseInt(btn.getAttribute('data-id'));
                        markOrderDelivered(id);
                    });
                });

                document.querySelectorAll('.order-row-clickable').forEach(row => {
                    row.addEventListener('click', () => {
                        const id = parseInt(row.getAttribute('data-id'));
                        openTrackingModal(id);
                    });
                });
            }
        } catch (err) {
            console.error('Error loading customer orders history:', err);
        }
    }

    // Mark Order Delivered / Received
    async function markOrderDelivered(orderId) {
        hideAlert();
        try {
            const res = await fetch(`${API_BASE}/storefront/orders/status`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({orderId, status: 'DELIVERED'})
            });

            const data = await res.json();

            if (res.ok) {
                showAlert(`🎉 Delivery Confirmed! Order #${orderId} marked as RECEIVED & DELIVERED!`, 'success');
                if (typeof window.triggerSystemToast === 'function') {
                    window.triggerSystemToast('🎉 Package Received & Confirmed', `Order #${orderId} marked as RECEIVED by customer!`, 'SUCCESS');
                }
                loadCustomerOrders();
                if (modalOrderTracking.style.display === 'flex' && currentActiveModalOrderId === orderId) {
                    openTrackingModal(orderId);
                }
            } else {
                showAlert(data.error || 'Failed to confirm delivery.', 'error');
            }
        } catch (err) {
            showAlert('Network error confirming delivery status.', 'error');
        }
    }

    // Open Complete End-to-End Tracking Dossier Modal
    function openTrackingModal(orderId) {
        const order = customerOrdersCache.find(o => o.id === orderId);
        if (!order) return;

        currentActiveModalOrderId = orderId;
        const isDom = (order.originCountryCode === order.destinationCountry);
        const carrier = order.carrierName || (isDom ? '🚚 Sri Lanka Ground Carrier' : '✈️ DHL Express International Air Fleet');
        const trackingNo = order.trackingNumber || `TRK-DHL-${89100 + order.id}`;
        const isDelivered = (order.status === 'DELIVERED');

        trackModalOrderRef.textContent = `#${order.orderNumber || ('ORD-2026-00' + order.id)}`;
        trackModalStatusBadge.textContent = isDelivered ? '✅ DELIVERED' : order.status;
        trackModalCustomerDesc.textContent = `Customer: ${order.customerName || 'Customer Client'} | Tracking Ref: ${trackingNo}`;

        trackModalCarrierName.textContent = carrier;
        const driverNameElem = document.getElementById('trackModalDriverName');
        const vehicleNoElem = document.getElementById('trackModalVehicleNo');
        if (driverNameElem) driverNameElem.textContent = order.driverName || 'Agent K. Perera';
        if (vehicleNoElem) vehicleNoElem.textContent = order.vehicleNo || 'WP-BC-8910';

        trackModalOriginDepot.textContent = `${WAREHOUSES[order.originCountryCode || 'US'] ? WAREHOUSES[order.originCountryCode || 'US'].name : 'Export Air Hub'} (${order.originCountryCode || 'US'})`;
        trackModalCustomsStatus.textContent = order.status === 'PROCESSING' ? 'IN_CUSTOMS_PROCESSING' : 'APPROVED & PASSED';
        trackModalDutyStatus.textContent = 'DUTY SETTLED BY FINANCE';
        trackModalEstimate.textContent = isDelivered ? 'Delivered to Customer' : (order.deliveryEstimate || (isDom ? '1-2 Business Days' : '3-5 Business Days'));

        // Products Breakdown
        if (order.items && order.items.length > 0) {
            trackModalProductItems.innerHTML = order.items.map(i => `
                <div style="display:flex; justify-content:space-between; padding:4px 0; border-bottom:1px dashed #cbd5e1;">
                    <span><strong>${i.quantity || 1}x</strong> ${i.productName || 'Siemens Diagnostic Ultrasound Transducer'}</span>
                    <strong>$${((i.unitPrice || 4890.00) * (i.quantity || 1)).toFixed(2)}</strong>
                </div>
            `).join('');
        } else {
            trackModalProductItems.innerHTML = `
                <div style="display:flex; justify-content:space-between; padding:4px 0;">
                    <span><strong>1x</strong> Medical Diagnostic Ultrasound Transducer [HS 9018.90]</span>
                    <strong>$${(order.totalAmount || 4890.00).toFixed(2)}</strong>
                </div>
            `;
        }

        // Generate 5-Step Stepper State
        const isStep1 = true;
        const isStep2 = order.status !== 'PROCESSING';
        const isStep3 = order.status !== 'PROCESSING' && order.status !== 'PACKED';
        const isStep4 = order.status === 'SHIPPED' || order.status === 'IN_TRANSIT' || order.status === 'DELIVERED';
        const isStep5 = order.status === 'DELIVERED';

        trackModalStepper.innerHTML = `
            <div style="display:flex; align-items:center; gap:6px; font-size:11px; font-weight:700; color:${isStep1 ? '#059669' : '#64748b'};">
                <span style="width:22px; height:22px; border-radius:50%; background:${isStep1 ? '#059669' : '#e2e8f0'}; color:#fff; display:flex; align-items:center; justify-content:center;">✓</span>
                1. Depot Pick-Pack
            </div>
            <span style="color:#cbd5e1;">➔</span>
            <div style="display:flex; align-items:center; gap:6px; font-size:11px; font-weight:700; color:${isStep2 ? '#059669' : '#64748b'};">
                <span style="width:22px; height:22px; border-radius:50%; background:${isStep2 ? '#059669' : '#e2e8f0'}; color:#fff; display:flex; align-items:center; justify-content:center;">${isStep2 ? '✓' : '2'}</span>
                2. Customs Pass
            </div>
            <span style="color:#cbd5e1;">➔</span>
            <div style="display:flex; align-items:center; gap:6px; font-size:11px; font-weight:700; color:${isStep3 ? '#059669' : '#64748b'};">
                <span style="width:22px; height:22px; border-radius:50%; background:${isStep3 ? '#059669' : '#e2e8f0'}; color:#fff; display:flex; align-items:center; justify-content:center;">${isStep3 ? '✓' : '3'}</span>
                3. Duty Settled
            </div>
            <span style="color:#cbd5e1;">➔</span>
            <div style="display:flex; align-items:center; gap:6px; font-size:11px; font-weight:700; color:${isStep4 ? '#059669' : '#64748b'};">
                <span style="width:22px; height:22px; border-radius:50%; background:${isStep4 ? '#059669' : '#e2e8f0'}; color:#fff; display:flex; align-items:center; justify-content:center;">${isStep4 ? '✓' : '4'}</span>
                4. Carrier Dispatch
            </div>
            <span style="color:#cbd5e1;">➔</span>
            <div style="display:flex; align-items:center; gap:6px; font-size:11px; font-weight:700; color:${isStep5 ? '#059669' : '#2563eb'};">
                <span style="width:22px; height:22px; border-radius:50%; background:${isStep5 ? '#059669' : '#2563eb'}; color:#fff; display:flex; align-items:center; justify-content:center;">${isStep5 ? '✓' : '5'}</span>
                5. ${isStep5 ? 'Delivered' : 'Awaiting Delivery'}
            </div>
        `;

        if (btnConfirmDeliveryModal) {
            if (isDelivered) {
                btnConfirmDeliveryModal.style.display = 'none';
            } else {
                btnConfirmDeliveryModal.style.display = 'block';
                btnConfirmDeliveryModal.onclick = () => markOrderDelivered(orderId);
            }
        }

        // Fetch Real-Time Tracking Events from tracking_events table
        loadTrackingEventsForModal(orderId);

        modalOrderTracking.style.display = 'flex';
    }

    async function loadTrackingEventsForModal(orderId) {
        const trackModalEventsList = document.getElementById('trackModalEventsList');
        if (!trackModalEventsList) return;

        try {
            const res = await fetch(`${API_BASE}/tracking/events`);
            const events = await res.json();

            if (res.ok && Array.isArray(events)) {
                if (events.length === 0) {
                    trackModalEventsList.innerHTML = `<div style="text-align:center; color:var(--text-muted);">No tracking events recorded yet.</div>`;
                    return;
                }

                trackModalEventsList.innerHTML = events.slice(0, 5).map(e => `
                    <div style="display:flex; justify-content:space-between; align-items:flex-start; padding:6px 10px; background:#fff; border:1px solid #cbd5e1; border-radius:6px;">
                        <div>
                            <strong style="color:#2563eb;">📍 ${e.location || 'Logistics Terminal'}</strong>
                            <p style="margin:2px 0 0 0; color:#475569;">${e.description}</p>
                        </div>
                        <small style="color:#94a3b8; font-size:10px;">${e.timestamp ? e.timestamp.replace('T', ' ').substring(0, 16) : 'Recent'}</small>
                    </div>
                `).join('');
            }
        } catch (err) {
            console.error('Error fetching tracking events for modal:', err);
        }
    }

    btnCloseTrackModal?.addEventListener('click', () => {
        modalOrderTracking.style.display = 'none';
    });
    btnDoneTrackModal?.addEventListener('click', () => {
        modalOrderTracking.style.display = 'none';
    });

    checkoutForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        if (cart.length === 0) {
            showAlert('Your shopping cart is empty! Please select product quantity and add to cart before checkout.');
            return;
        }

        const btn = document.getElementById('btnCheckout');
        const customerName = document.getElementById('custName').value.trim();
        const prefix = document.getElementById('custPhonePrefix').value;
        const rawPhone = document.getElementById('custPhone').value.trim();
        const email = document.getElementById('custEmail').value.trim();
        const address = document.getElementById('custAddress').value.trim();
        const warehouseVal = custWarehouseOrigin ? custWarehouseOrigin.value : 'DE|1';
        const parts = warehouseVal.split('|');
        const originCountryCode = parts[0] || 'DE';
        currentWarehouseId = parseInt(parts[1]) || 1;

        const destinationCountryCode = custDestCountry.value;
        const paymentMethod = document.getElementById('custPayment').value;

        if (!customerName || !rawPhone || !email || !address) {
            showAlert('Please fill in all customer name, phone number, and address fields.');
            return;
        }

        const fullPhone = `${prefix} ${rawPhone}`;

        setLoading(btn, true);

        try {
            const res = await fetch(`${API_BASE}/storefront/checkout`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    customerName,
                    phone: fullPhone,
                    email,
                    street: address,
                    city: address.split(',')[1]?.trim() || 'Colombo',
                    postalCode: '00100',
                    destinationCountryCode,
                    originCountryCode,
                    warehouseId: currentWarehouseId,
                    paymentMethod,
                    items: cart.map(i => ({productId: i.id, qty: i.qty}))
                })
            });

            const data = await res.json();

            if (res.ok || res.status === 201) {
                showAlert(`🎉 Order #${data.orderNumber} placed & confirmed! Shipment Tracking: ${data.trackingNumber || 'TRK-DHL-89100'} | Total: $${data.totalAmount.toFixed(2)} (${data.carrierName})`, 'success');

                // Reset cart
                cart = [];
                updateCartUI();
                loadCustomerOrders();
            } else {
                showAlert(data.error || 'Checkout failed. Please try again.');
            }
        } catch (err) {
            showAlert('Network error during checkout processing.');
            console.error('Checkout error:', err);
        } finally {
            setLoading(btn, false);
        }
    });

    btnRefreshCatalog?.addEventListener('click', () => {
        loadCatalog();
        loadCustomerOrders();
    });

    // Initial Load & Auto-Routing Calculation
    calculateShippingCost();
    loadCatalog();
    loadCustomerOrders();
});
