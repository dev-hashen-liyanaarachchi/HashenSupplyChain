package com.globaltrade.timer;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import com.globaltrade.ejb.interfaces.VendorService;
import com.globaltrade.entity.Vendor;
import java.util.List;
import java.util.logging.Logger;

@Singleton
@Startup
public class VendorEvaluationTimer {

    private static final Logger LOGGER = Logger.getLogger(VendorEvaluationTimer.class.getName());

    @EJB
    private VendorService vendorService;

    @EJB
    private com.globaltrade.service.VendorPerformanceService vendorPerformanceService;

    @EJB
    private com.globaltrade.service.NotificationService notificationService;

    // Declarative EJB Timer: Triggers at midnight on the 1st of every month
    @Schedule(dayOfMonth = "1", hour = "0", minute = "0", second = "0", persistent = true, info = "Vendor Monthly Performance Audit")
    public void executeMonthlyVendorRating() {
        LOGGER.info("[DECLARATIVE EJB TIMER] Executing monthly vendor rating & performance evaluation...");

        if (vendorPerformanceService != null) {
            vendorPerformanceService.getAllVendorPerformances();
        }

        if (notificationService != null) {
            notificationService.createNotification("Monthly Vendor SLA Evaluation Completed", "Evaluated delivery SLA completion rates for all active international vendors and updated vendor_performances table.", "VENDOR", "SUCCESS");
        }
    }
}
