package com.globaltrade.timer;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import com.globaltrade.ejb.interfaces.InventoryService;
import com.globaltrade.service.NotificationService;
import com.globaltrade.entity.Inventory;
import java.util.List;
import java.util.logging.Logger;

@Singleton
@Startup
public class InventoryMonitoringTimer {

    private static final Logger LOGGER = Logger.getLogger(InventoryMonitoringTimer.class.getName());

    @EJB
    private InventoryService inventoryService;

    @EJB
    private NotificationService notificationService;

    // Declarative EJB Timer: Triggers every 6 hours
    @Schedule(hour = "*/6", minute = "0", second = "0", persistent = true, info = "Global Inventory Monitoring Audit")
    public void executeStockMonitoring() {
        LOGGER.info("[DECLARATIVE EJB TIMER] Executing 6-hour inventory monitoring audit...");

        List<Inventory> lowStock = inventoryService.checkLowStockItems();
        for (Inventory item : lowStock) {
            LOGGER.warning("[LOW STOCK ALERT] Product: " + item.getProduct().getName() + " (SKU: " + item.getProduct().getSku() +
                ") at Warehouse: " + item.getWarehouse().getName() + " has " + item.getAvailableQty() + " units available.");
            if (notificationService != null) {
                notificationService.createNotification("Low Stock Reorder Alert", "Product SKU " + item.getProduct().getSku() + " at " + item.getWarehouse().getName() + " reached threshold (" + item.getAvailableQty() + " units). Auto-supplier PO generated.", "INVENTORY", "CRITICAL");
            }
        }
    }
}
