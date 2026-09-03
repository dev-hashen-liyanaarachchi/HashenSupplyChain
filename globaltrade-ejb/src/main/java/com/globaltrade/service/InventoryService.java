package com.globaltrade.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.*;
import com.globaltrade.enums.*;

import java.util.*;

@Stateless
public class InventoryService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @EJB
    private ShippingRateService shippingRateService;

    @EJB
    private ShipmentItemService shipmentItemService;

    @EJB
    private TrackingEventService trackingEventService;

    public List<com.globaltrade.enums.HsCodeCategory> getAllHsCodes() {
        return List.of(com.globaltrade.enums.HsCodeCategory.values());
    }

    public List<Vendor> getAllVendors() {
        List<Vendor> list = em.createQuery("SELECT DISTINCT v FROM Vendor v LEFT JOIN FETCH v.user", Vendor.class).getResultList();
        if (list.isEmpty()) {
            User u = new User("vendor_bosch", "vendor123", "sales@bosch.de", null);
            em.persist(u);
            Vendor v = new Vendor(u, "Bosch Logistics GmbH", "DE-TAX-998877", null);
            v.setStatus(VendorStatus.ACTIVE);
            em.persist(v);
            list = List.of(v);
        }
        return list;
    }

    public List<Category> getAllCategories() {
        List<Category> list = em.createQuery("SELECT c FROM Category c ORDER BY c.name ASC", Category.class).getResultList();
        if (list.isEmpty()) {
            em.persist(new Category("Medical Equipment"));
            em.persist(new Category("Power & Electrical"));
            em.persist(new Category("IT & Hardware"));
            em.persist(new Category("Pharmaceuticals"));
            em.persist(new Category("Automotive"));
            list = em.createQuery("SELECT c FROM Category c ORDER BY c.name ASC", Category.class).getResultList();
        }
        return list;
    }

    public Category createCategory(String name) {
        Category c = new Category(name);
        em.persist(c);
        return c;
    }

    public List<Brand> getAllBrands() {
        List<Brand> list = em.createQuery("SELECT b FROM Brand b ORDER BY b.name ASC", Brand.class).getResultList();
        if (list.isEmpty()) {
            em.persist(new Brand("Siemens Healthineers"));
            em.persist(new Brand("Bosch Healthcare"));
            em.persist(new Brand("Dell Enterprise"));
            em.persist(new Brand("Pfizer Global"));
            list = em.createQuery("SELECT b FROM Brand b ORDER BY b.name ASC", Brand.class).getResultList();
        }
        return list;
    }

    public Brand createBrand(String name) {
        Brand b = new Brand(name);
        em.persist(b);
        return b;
    }

    public Product createProduct(String sku, String name, Double price, Double weightKg, String hsCode, String description, Long categoryId, Long brandId) {
        Category category = (categoryId != null) ? em.find(Category.class, categoryId) : null;
        Brand brand = (brandId != null) ? em.find(Brand.class, brandId) : null;

        String validHs = (hsCode != null && !hsCode.isBlank()) ? hsCode : "9018.90";
        Product product = new Product(sku, name, price, weightKg, validHs);
        product.setDescription(description);
        product.setCategory(category);
        product.setBrand(brand);

        em.persist(product);
        return product;
    }

    public List<Product> getAllProducts() {
        List<Product> list = em.createQuery("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand", Product.class).getResultList();
        if (list.isEmpty()) {
            Category catIt = getAllCategories().stream().filter(c -> c.getName().contains("IT")).findFirst().orElse(null);
            Brand brandDell = getAllBrands().stream().filter(b -> b.getName().contains("Dell")).findFirst().orElseGet(() -> createBrand("Dell Enterprise"));

            Product p1 = new Product("DELL-XPS-15-9530", "Dell XPS 15 9530 Core i9 32GB 1TB RTX 4070", 2499.00, 1.92, "8471.30");
            p1.setDescription("15.6 inch OLED 3.5K Touchscreen Enterprise Workstation Laptop with Nvidia RTX 4070");
            p1.setCategory(catIt);
            p1.setBrand(brandDell);
            em.persist(p1);

            Product p2 = new Product("DELL-PREC-7680", "Dell Precision 7680 Mobile Workstation", 3250.00, 2.60, "8471.30");
            p2.setDescription("High-performance ISV certified CAD & AI engineering laptop with Intel Core i7 64GB RAM");
            p2.setCategory(catIt);
            p2.setBrand(brandDell);
            em.persist(p2);

            Product p3 = new Product("DELL-LAT-5540", "Dell Latitude 5540 Business Laptop", 1199.00, 1.61, "8471.30");
            p3.setDescription("15.6 FHD Anti-Glare Business Laptop with Thunderbolt 4 and SmartCard security");
            p3.setCategory(catIt);
            p3.setBrand(brandDell);
            em.persist(p3);

            list = em.createQuery("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand", Product.class).getResultList();
        }
        return list;
    }

    public Product getProductById(Long id) {
        if (id == null) return null;
        return em.find(Product.class, id);
    }

    public Inventory addInventory(Long warehouseId, Long productId, Long vendorId, Double unitPrice, Integer availableQty, Integer reorderThreshold) {
        Warehouse warehouse = em.find(Warehouse.class, warehouseId);
        Product product = em.find(Product.class, productId);

        if (warehouse == null || product == null) {
            throw new IllegalArgumentException("Invalid Warehouse ID or Product ID");
        }

        Vendor vendor = null;
        if (vendorId != null) {
            vendor = em.find(Vendor.class, vendorId);
        }
        if (vendor == null) {
            List<Vendor> vlist = getAllVendors();
            vendor = vlist.get(0);
        }

        Double price = (unitPrice != null && unitPrice > 0) ? unitPrice : product.getPrice();

        Inventory inventory = new Inventory(
                warehouse,
                product,
                vendor,
                price,
                (availableQty != null && availableQty >= 0) ? availableQty : 100,
                (reorderThreshold != null && reorderThreshold >= 0) ? reorderThreshold : 50
        );

        em.persist(inventory);

        warehouse.setCurrentCapacity(warehouse.getCurrentCapacity() + inventory.getAvailableQty());
        em.merge(warehouse);

        return inventory;
    }

    public List<Inventory> getAllInventories() {
        List<Inventory> list = em.createQuery("SELECT DISTINCT i FROM Inventory i LEFT JOIN FETCH i.warehouse w LEFT JOIN FETCH w.address a LEFT JOIN FETCH a.country c LEFT JOIN FETCH i.product p LEFT JOIN FETCH i.vendor v", Inventory.class)
                .getResultList();
        if (list.isEmpty()) {
            List<Product> products = getAllProducts();
            List<Warehouse> warehouses = em.createQuery("SELECT w FROM Warehouse w LEFT JOIN FETCH w.address a LEFT JOIN FETCH a.country c", Warehouse.class).getResultList();
            if (warehouses.isEmpty()) {
                if (warehouseService == null) warehouseService = new WarehouseService();
                Warehouse wDe = warehouseService.createWarehouse("Germany Hamburg Export Hub", "100 Export Port Highway", "Hamburg", "Logistics Sector", "20095", "DE", 50000);
                Warehouse wLk = warehouseService.createWarehouse("Sri Lanka Colombo Logistics Depot", "45 Baseline Rd", "Colombo", "Western Sector", "00100", "LK", 30000);
                Warehouse wUs = warehouseService.createWarehouse("USA New York Air Cargo Center", "500 JFK Express Way", "New York", "NY Cargo Hub", "11430", "US", 60000);
                Warehouse wJp = warehouseService.createWarehouse("Japan Tokyo Cargo Hub", "12 Haneda Airport West", "Tokyo", "Kanto Sector", "144-0041", "JP", 45000);
                warehouses = List.of(wDe, wLk, wUs, wJp);
            }

            Vendor vendor = getAllVendors().get(0);
            for (int idx = 0; idx < products.size(); idx++) {
                Product p = products.get(idx);
                Warehouse targetWh = warehouses.get(idx % warehouses.size());
                Inventory inv = new Inventory(targetWh, p, vendor, p.getPrice(), 150, 20);
                em.persist(inv);
            }
            list = em.createQuery("SELECT DISTINCT i FROM Inventory i LEFT JOIN FETCH i.warehouse w LEFT JOIN FETCH w.address a LEFT JOIN FETCH a.country c LEFT JOIN FETCH i.product p LEFT JOIN FETCH i.vendor v", Inventory.class).getResultList();
        }
        return list;
    }

    public ShippingRateService.ShippingQuote getShippingQuote(String originCountryCode, String destinationCountryCode, double totalWeightKg) {
        if (shippingRateService == null) {
            shippingRateService = new ShippingRateService();
        }
        return shippingRateService.calculateShippingQuote(originCountryCode, destinationCountryCode, totalWeightKg);
    }

    // Place Storefront Order with Weight-based Shipping & Customs Calculation
    @EJB
    private WarehouseService warehouseService;

    public Map<String, Object> placeStorefrontOrder(
            String customerName,
            String phone,
            String email,
            String street,
            String city,
            String postalCode,
            String destinationCountryCode,
            Long warehouseId,
            String paymentMethod,
            List<Map<String, Object>> items
    ) {
        return placeStorefrontOrder(customerName, phone, email, street, city, postalCode, destinationCountryCode, "DE", warehouseId, paymentMethod, items);
    }

    public Map<String, Object> placeStorefrontOrder(
            String customerName,
            String phone,
            String email,
            String street,
            String city,
            String postalCode,
            String destinationCountryCode,
            String originCountryCodeReq,
            Long warehouseId,
            String paymentMethod,
            List<Map<String, Object>> items
    ) {
        String uname = (customerName != null && !customerName.isBlank()) ? customerName : "Storefront Customer";
        String uemail = (email != null && !email.isBlank()) ? email : "customer@globaltrade.com";

        User user = em.createQuery("SELECT u FROM User u WHERE u.username = :uname OR u.email = :uemail", User.class)
                .setParameter("uname", uname)
                .setParameter("uemail", uemail)
                .getResultStream().findFirst()
                .orElseGet(() -> {
                    User nu = new User(uname.replaceAll("\\s+", "_").toLowerCase(), "pass123", uemail, null);
                    em.persist(nu);
                    return nu;
                });

        Customer customer = em.createQuery("SELECT c FROM Customer c WHERE c.user.id = :uid", Customer.class)
                .setParameter("uid", user.getId())
                .getResultStream().findFirst()
                .orElseGet(() -> {
                    Customer nc = new Customer(user, uname, "Client", phone != null ? phone : "+94 77 000 0000", null);
                    em.persist(nc);
                    return nc;
                });

        String destCountry = (destinationCountryCode != null && !destinationCountryCode.isBlank()) ? destinationCountryCode.toUpperCase() : "LK";
        String requestedOrigin = (originCountryCodeReq != null && !originCountryCodeReq.isBlank()) ? originCountryCodeReq.toUpperCase() : "DE";

        Warehouse warehouse = null;
        if (warehouseId != null && warehouseId > 0) {
            warehouse = em.find(Warehouse.class, warehouseId);
            if (warehouse != null && warehouse.getAddress() != null && warehouse.getAddress().getCountry() != null) {
                if (!warehouse.getAddress().getCountry().getCode().equalsIgnoreCase(requestedOrigin)) {
                    warehouse = null;
                }
            }
        }

        if (warehouse == null) {
            List<Warehouse> wList = em.createQuery("SELECT w FROM Warehouse w LEFT JOIN FETCH w.address a LEFT JOIN FETCH a.country c WHERE c.code = :code", Warehouse.class)
                    .setParameter("code", requestedOrigin)
                    .getResultList();
            if (!wList.isEmpty()) {
                warehouse = wList.get(0);
            }
        }

        if (warehouse == null) {
            String whName;
            String whCity;
            if (requestedOrigin.equals("US")) {
                whName = "USA New York Air Cargo Center";
                whCity = "New York";
            } else if (requestedOrigin.equals("JP")) {
                whName = "Japan Tokyo Cargo Hub";
                whCity = "Tokyo";
            } else if (requestedOrigin.equals("DE")) {
                whName = "Germany Hamburg Export Hub";
                whCity = "Hamburg";
            } else if (requestedOrigin.equals("SG")) {
                whName = "Singapore Port Sea Freight Hub";
                whCity = "Singapore";
            } else {
                whName = "Colombo Logistics Depot";
                whCity = "Colombo";
            }

            if (warehouseService == null) {
                warehouseService = new WarehouseService();
            }
            warehouse = warehouseService.createWarehouse(whName, "100 Export Port Highway", whCity, "Logistics Sector", "00100", requestedOrigin, 50000);
        }

        String originCountryCode = requestedOrigin;
        if (warehouse != null && warehouse.getAddress() != null && warehouse.getAddress().getCountry() != null) {
            originCountryCode = warehouse.getAddress().getCountry().getCode();
        }

        String warehouseName = (warehouse != null) ? warehouse.getName() : "Auto-Routed Logistics Hub";

        double subtotal = 0.0;
        double totalWeightKg = 0.0;
        List<Map<String, Object>> processedItems = new ArrayList<>();

        if (items != null) {
            for (Map<String, Object> itemMap : items) {
                Long productId = Long.valueOf(itemMap.get("productId").toString());
                Integer qty = Integer.valueOf(itemMap.get("qty").toString());

                Product p = em.find(Product.class, productId);
                if (p != null) {
                    double lineTotal = p.getPrice() * qty;
                    double itemWeight = (p.getWeightKg() != null ? p.getWeightKg() : 1.0) * qty;
                    subtotal += lineTotal;
                    totalWeightKg += itemWeight;

                    processedItems.add(Map.of(
                            "productId", p.getId(),
                            "sku", p.getSku(),
                            "name", p.getName(),
                            "price", p.getPrice(),
                            "weightKg", p.getWeightKg() != null ? p.getWeightKg() : 1.0,
                            "qty", qty,
                            "lineTotal", lineTotal
                    ));
                }
            }
        }

        if (shippingRateService == null) {
            shippingRateService = new ShippingRateService();
        }
        ShippingRateService.ShippingQuote quote = shippingRateService.calculateShippingQuote(originCountryCode, destCountry, totalWeightKg);

        double shippingCost = quote.getShippingFee();
        boolean isDomestic = quote.isDomestic();
        String shippingType = quote.getShipmentType() + " (" + quote.getCarrierName() + " - " + quote.getWeightTierLabel() + ")";
        String deliveryEstimate = quote.getDeliveryEstimate();

        double totalAmount = subtotal + shippingCost;
        String orderNumber = "ORD-2026-" + (10000 + new Random().nextInt(90000));

        Country country = em.createQuery("SELECT c FROM Country c WHERE c.code = :code", Country.class)
                .setParameter("code", destCountry)
                .getResultStream().findFirst()
                .orElseGet(() -> {
                    Country nc = new Country(destCountry, destCountry);
                    em.persist(nc);
                    return nc;
                });

        Address shippingAddressObj = new Address(
                street != null && !street.isBlank() ? street : "100 Hospital Ave",
                city != null && !city.isBlank() ? city : "Colombo",
                null,
                postalCode != null && !postalCode.isBlank() ? postalCode : "00100",
                phone,
                country
        );
        em.persist(shippingAddressObj);

        // Update Customer table address_id reference & cleanup old address
        Address oldAddress = customer.getAddress();
        customer.setAddress(shippingAddressObj);
        if (phone != null && !phone.isBlank()) {
            customer.setPhone(phone);
        }
        em.merge(customer);

        if (oldAddress != null && oldAddress.getId() != null && !oldAddress.getId().equals(shippingAddressObj.getId())) {
            try {
                Long countWarehouse = em.createQuery("SELECT COUNT(w) FROM Warehouse w WHERE w.address.id = :aid", Long.class)
                        .setParameter("aid", oldAddress.getId())
                        .getSingleResult();
                Long countOrders = em.createQuery("SELECT COUNT(o) FROM Order o WHERE o.shippingAddress.id = :aid", Long.class)
                        .setParameter("aid", oldAddress.getId())
                        .getSingleResult();
                Long countCustomers = em.createQuery("SELECT COUNT(c) FROM Customer c WHERE c.address.id = :aid", Long.class)
                        .setParameter("aid", oldAddress.getId())
                        .getSingleResult();

                if (countWarehouse == 0 && countOrders == 0 && countCustomers == 0) {
                    em.remove(em.contains(oldAddress) ? oldAddress : em.merge(oldAddress));
                }
            } catch (Exception ex) {
                System.err.println("Old address delete notification: " + ex.getMessage());
            }
        }

        Order order = new Order(orderNumber, customer, shippingAddressObj, subtotal, shippingCost, totalAmount);
        order.setStatus(OrderStatus.PROCESSING);
        em.persist(order);

        com.globaltrade.enums.PaymentMethod pMethod = com.globaltrade.enums.PaymentMethod.CREDIT_CARD;
        if (paymentMethod != null && !paymentMethod.isBlank()) {
            try {
                pMethod = com.globaltrade.enums.PaymentMethod.valueOf(paymentMethod.trim().toUpperCase());
            } catch (Exception ex) {
            }
        }
        String txnRef = "TXN-GT-" + System.currentTimeMillis() + "-" + (100 + new Random().nextInt(899));
        com.globaltrade.entity.Payment payment = new com.globaltrade.entity.Payment(order, txnRef, pMethod, totalAmount);
        payment.setPaymentStatus(com.globaltrade.enums.PaymentStatus.COMPLETED);
        em.persist(payment);

        for (Map<String, Object> pItem : processedItems) {
            Long pid = (Long) pItem.get("productId");
            Integer pqty = (Integer) pItem.get("qty");
            Double pPrice = (Double) pItem.get("price");

            Product pr = em.find(Product.class, pid);
            OrderItem oi = new OrderItem(order, pr, pqty, pPrice);
            em.persist(oi);

            // Deduct stock quantity from inventory for assigned warehouse & product
            if (warehouse != null && pr != null) {
                List<Inventory> invList = em.createQuery(
                                "SELECT i FROM Inventory i WHERE i.product.id = :pid AND i.warehouse.id = :wid", Inventory.class)
                        .setParameter("pid", pid)
                        .setParameter("wid", warehouse.getId())
                        .getResultList();

                if (!invList.isEmpty()) {
                    Inventory inv = invList.get(0);
                    int currentQty = inv.getAvailableQty() != null ? inv.getAvailableQty() : 150;
                    int updatedQty = Math.max(0, currentQty - pqty);
                    inv.setAvailableQty(updatedQty);
                    em.merge(inv);

                    InventoryTransaction tx = new InventoryTransaction(inv, InventoryTransactionType.OUTBOUND_SHIPMENT, pqty, "Customer Order #" + orderNumber);
                    em.persist(tx);
                } else {
                    List<Inventory> globalInv = em.createQuery(
                                    "SELECT i FROM Inventory i WHERE i.product.id = :pid", Inventory.class)
                            .setParameter("pid", pid)
                            .getResultList();
                    if (!globalInv.isEmpty()) {
                        Inventory gInv = globalInv.get(0);
                        int currentQty = gInv.getAvailableQty() != null ? gInv.getAvailableQty() : 150;
                        int updatedQty = Math.max(0, currentQty - pqty);
                        gInv.setAvailableQty(updatedQty);
                        em.merge(gInv);

                        InventoryTransaction tx = new InventoryTransaction(gInv, InventoryTransactionType.OUTBOUND_SHIPMENT, pqty, "Customer Order #" + orderNumber);
                        em.persist(tx);
                    }
                }
            }
        }

        String trackingNumber = (isDomestic ? "TRK-LOC-" : "TRK-DHL-") + (10000 + new Random().nextInt(90000));
        ShipmentType stType = isDomestic ? ShipmentType.EXPRESS_COURIER : ShipmentType.AIR_FREIGHT;
        Shipment shipment = new Shipment(
                trackingNumber,
                order,
                stType,
                warehouse,
                shippingAddressObj,
                java.time.LocalDateTime.now().plusDays(isDomestic ? 2 : 5)
        );
        shipment.setStatus(isDomestic ? ShipmentStatus.PREPARING : ShipmentStatus.IN_CUSTOMS);
        em.persist(shipment);

        if (shipmentItemService != null) {
            shipmentItemService.createShipmentItemsForShipment(shipment);
        }

        if (trackingEventService != null) {
            trackingEventService.recordTrackingEvent(shipment, warehouseName, "Cargo packed and submitted for export customs inspection.");
        }

        String itemHsCode = "8471.30";
        StringBuilder packingItemsSummary = new StringBuilder();
        if (processedItems != null && !processedItems.isEmpty()) {
            for (Map<String, Object> pItem : processedItems) {
                Long pid = (Long) pItem.get("productId");
                Integer pqty = (Integer) pItem.get("qty");
                Product pr = em.find(Product.class, pid);
                if (pr != null) {
                    if (packingItemsSummary.length() > 0) packingItemsSummary.append(", ");
                    packingItemsSummary.append(pqty).append("x ").append(pr.getName()).append(" [HS ").append(pr.getHsCode()).append("]");
                    if (itemHsCode.equals("8471.30") && pr.getHsCode() != null && !pr.getHsCode().isBlank()) {
                        itemHsCode = pr.getHsCode();
                    }
                }
            }
        }

        CustomsDocument cdoc = new CustomsDocument(shipment, isDomestic ? "LOCAL_GROUND_MANIFEST" : "COMMERCIAL_INVOICE_PACKING_LIST", itemHsCode);
        cdoc.setStatus(CustomsDocumentStatus.SUBMITTED);
        cdoc.setInspectedBy("Pending Customs Inspection");
        cdoc.setDeclaredValue(subtotal);
        cdoc.setDutyFee(Math.round(subtotal * 0.05 * 100.0) / 100.0);
        cdoc.setOriginCountry(originCountryCode);
        cdoc.setDestinationCountry(destCountry);
        cdoc.setExporterName(warehouseName);
        cdoc.setImporterName(uname + " (" + uemail + ")");
        cdoc.setPackingListItems(packingItemsSummary.length() > 0 ? packingItemsSummary.toString() : "1x Cargo Shipment");
        cdoc.setClearanceDeadline(java.time.LocalDateTime.now().plusHours(48));
        em.persist(cdoc);

        Map<String, Object> res = new HashMap<>();
        res.put("orderId", order.getId());
        res.put("orderNumber", orderNumber);
        res.put("trackingNumber", trackingNumber);
        res.put("customerName", uname);
        res.put("phone", phone != null ? phone : "");
        res.put("email", uemail);
        res.put("shippingAddressId", shippingAddressObj.getId());
        res.put("destinationCountry", destCountry);
        res.put("originCountry", originCountryCode);
        res.put("warehouseName", warehouseName);
        res.put("carrierName", quote.getCarrierName());
        res.put("weightTierLabel", quote.getWeightTierLabel());
        res.put("isDomestic", isDomestic);
        res.put("shippingType", shippingType);
        res.put("totalWeightKg", Math.round(totalWeightKg * 100.0) / 100.0);
        res.put("shippingCost", shippingCost);
        res.put("subtotal", subtotal);
        res.put("totalAmount", totalAmount);
        res.put("deliveryEstimate", deliveryEstimate);
        res.put("orderStatus", "PROCESSING");
        res.put("items", processedItems);

        return res;
    }

    public List<Order> getAllOrders() {
        return em.createQuery("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.customer c LEFT JOIN FETCH c.user u LEFT JOIN FETCH o.shippingAddress a LEFT JOIN FETCH a.country ORDER BY o.id DESC", Order.class)
                .getResultList();
    }

    public List<Map<String, Object>> getOrdersWithShipments() {
        List<Order> orders = getAllOrders();
        List<Shipment> shipments = em.createQuery("SELECT DISTINCT s FROM Shipment s LEFT JOIN FETCH s.order o LEFT JOIN FETCH s.originWarehouse w LEFT JOIN FETCH w.address wa LEFT JOIN FETCH wa.country wc", Shipment.class)
                .getResultList();

        Map<Long, Shipment> shipmentMap = new HashMap<>();
        for (Shipment s : shipments) {
            if (s.getOrder() != null) {
                shipmentMap.put(s.getOrder().getId(), s);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("orderNumber", o.getOrderNumber());
            m.put("customerName", o.getCustomer() != null ? (o.getCustomer().getFirstName() + " " + o.getCustomer().getLastName()) : "Guest Client");
            m.put("email", (o.getCustomer() != null && o.getCustomer().getUser() != null) ? o.getCustomer().getUser().getEmail() : "client@company.com");

            String destCode = (o.getShippingAddress() != null && o.getShippingAddress().getCountry() != null) ? o.getShippingAddress().getCountry().getCode() : "LK";
            m.put("destinationCountry", destCode);
            m.put("subtotal", o.getItemsSubtotal());
            m.put("shippingCost", o.getShippingCost());
            m.put("totalAmount", o.getTotalAmount());
            m.put("status", o.getStatus() != null ? o.getStatus().name() : "PROCESSING");
            m.put("orderDate", o.getCreatedAt() != null ? o.getCreatedAt().toString() : "Recent");

            Shipment s = shipmentMap.get(o.getId());
            String originCode = "DE";
            String originWhName = "Hamburg Export Hub";
            String trackingNumber = "TRK-DHL-" + (10000 + (o.getId().intValue() * 37) % 90000);

            if (s != null) {
                if (s.getTrackingNumber() != null && !s.getTrackingNumber().isBlank()) {
                    trackingNumber = s.getTrackingNumber();
                }
                if (s.getOriginWarehouse() != null) {
                    originWhName = s.getOriginWarehouse().getName();
                    if (s.getOriginWarehouse().getAddress() != null && s.getOriginWarehouse().getAddress().getCountry() != null) {
                        originCode = s.getOriginWarehouse().getAddress().getCountry().getCode();
                    } else if (originWhName.toLowerCase().contains("colombo")) {
                        originCode = "LK";
                    } else if (originWhName.toLowerCase().contains("tokyo") || originWhName.toLowerCase().contains("japan")) {
                        originCode = "JP";
                    } else if (originWhName.toLowerCase().contains("new york") || originWhName.toLowerCase().contains("usa")) {
                        originCode = "US";
                    }
                }
            }

            boolean isDomestic = originCode.equalsIgnoreCase(destCode);
            String carrierName = isDomestic ? "Sri Lanka Logistics Express Ground Fleet" : "DHL Express Air Cargo";
            String driverName = "Agent K. Perera";
            String vehicleNo = "WP-BC-8910";

            if (s != null) {
                if (s.getCarrier() != null && s.getCarrier().getName() != null) {
                    carrierName = s.getCarrier().getName();
                }
                if (s.getDriverName() != null && !s.getDriverName().isBlank()) {
                    driverName = s.getDriverName();
                }
                if (s.getVehicleNo() != null && !s.getVehicleNo().isBlank()) {
                    vehicleNo = s.getVehicleNo();
                }
            }

            m.put("originCountry", originCode);
            m.put("warehouseName", originWhName);
            m.put("trackingNumber", trackingNumber);
            m.put("isDomestic", isDomestic);
            m.put("carrierName", carrierName);
            m.put("driverName", driverName);
            m.put("vehicleNo", vehicleNo);
            m.put("deliveryEstimate", isDomestic ? "1-2 Business Days" : "3-5 Business Days (Customs Inspection)");

            result.add(m);
        }

        return result;
    }

    public Order updateOrderStatus(Long orderId, String newStatusStr) {
        Order order = em.find(Order.class, orderId);
        if (order != null && newStatusStr != null && !newStatusStr.isBlank()) {
            try {
                OrderStatus status = OrderStatus.valueOf(newStatusStr.toUpperCase());
                order.setStatus(status);
                em.merge(order);
            } catch (Exception e) {
                System.err.println("Invalid OrderStatus: " + newStatusStr);
            }
        }
        return order;
    }

    public List<Map<String, Object>> getAllShipments() {
        List<Shipment> list = em.createQuery(
                        "SELECT DISTINCT s FROM Shipment s LEFT JOIN FETCH s.order o LEFT JOIN FETCH s.originWarehouse w LEFT JOIN FETCH w.address wa LEFT JOIN FETCH wa.country wc LEFT JOIN FETCH s.destinationAddress da LEFT JOIN FETCH da.country dac ORDER BY s.id DESC", Shipment.class)
                .getResultList();

        return list.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("trackingNumber", s.getTrackingNumber());
            m.put("orderId", s.getOrder() != null ? s.getOrder().getId() : 0);
            m.put("orderNumber", s.getOrder() != null ? s.getOrder().getOrderNumber() : "ORD-000");
            m.put("originWarehouse", s.getOriginWarehouse() != null ? s.getOriginWarehouse().getName() : "Export Hub");
            m.put("originCountry", (s.getOriginWarehouse() != null && s.getOriginWarehouse().getAddress() != null && s.getOriginWarehouse().getAddress().getCountry() != null) ? s.getOriginWarehouse().getAddress().getCountry().getCode() : "DE");
            m.put("destinationCountry", (s.getDestinationAddress() != null && s.getDestinationAddress().getCountry() != null) ? s.getDestinationAddress().getCountry().getCode() : "LK");
            m.put("shipmentType", s.getShipmentType() != null ? s.getShipmentType().name() : "AIR_FREIGHT");
            m.put("status", s.getStatus() != null ? s.getStatus().name() : "PREPARING");
            m.put("estimatedDelivery", s.getEstimatedDelivery() != null ? s.getEstimatedDelivery().toString() : "3-5 Days");
            return m;
        }).toList();
    }

    public Shipment updateShipmentStatus(Long shipmentId, String newStatusStr) {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment != null && newStatusStr != null && !newStatusStr.isBlank()) {
            try {
                ShipmentStatus status = ShipmentStatus.valueOf(newStatusStr.toUpperCase());
                shipment.setStatus(status);
                em.merge(shipment);

                if (shipment.getOrder() != null) {
                    if (status == ShipmentStatus.IN_TRANSIT || status == ShipmentStatus.OUT_FOR_DELIVERY) {
                        shipment.getOrder().setStatus(OrderStatus.SHIPPED);
                        em.merge(shipment.getOrder());
                    } else if (status == ShipmentStatus.DELIVERED) {
                        shipment.getOrder().setStatus(OrderStatus.DELIVERED);
                        em.merge(shipment.getOrder());
                    }
                }
            } catch (Exception e) {
                System.err.println("Invalid ShipmentStatus: " + newStatusStr);
            }
        }
        return shipment;
    }

    public List<InventoryTransaction> getAllInventoryTransactions() {
        List<InventoryTransaction> list = em.createQuery("SELECT DISTINCT t FROM InventoryTransaction t LEFT JOIN FETCH t.inventory i LEFT JOIN FETCH i.product LEFT JOIN FETCH i.warehouse ORDER BY t.id DESC", InventoryTransaction.class)
                .setMaxResults(50)
                .getResultList();

        if (list.isEmpty()) {
            java.util.logging.Logger.getLogger(InventoryService.class.getName()).info("[INVENTORY TRANSACTION SEED] Auto-seeding initial inventory transaction audit history...");
            List<Inventory> inventories = em.createQuery("SELECT i FROM Inventory i LEFT JOIN FETCH i.product LEFT JOIN FETCH i.warehouse", Inventory.class).getResultList();
            for (Inventory inv : inventories) {
                em.persist(new InventoryTransaction(inv, InventoryTransactionType.INBOUND_RECEIPT, inv.getAvailableQty() != null ? inv.getAvailableQty() : 150, "Initial Inbound Freight Batch"));
            }
            list = em.createQuery("SELECT DISTINCT t FROM InventoryTransaction t LEFT JOIN FETCH t.inventory i LEFT JOIN FETCH i.product LEFT JOIN FETCH i.warehouse ORDER BY t.id DESC", InventoryTransaction.class)
                    .setMaxResults(50)
                    .getResultList();
        }
        return list;
    }
}
