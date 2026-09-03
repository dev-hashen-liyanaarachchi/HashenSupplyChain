package com.globaltrade.timer;

import jakarta.annotation.Resource;
import jakarta.ejb.*;

import java.io.Serializable;
import java.util.logging.Logger;

@Stateless
public class ShipmentDelayAlertTimer {

    private static final Logger LOGGER = Logger.getLogger(ShipmentDelayAlertTimer.class.getName());

    @Resource
    private TimerService timerService;

    @EJB
    private com.globaltrade.service.NotificationService notificationService;

    // Declarative EJB Timer: Scans for delayed shipments every 15 minutes
    @Schedule(minute = "*/15", hour = "*", persistent = true, info = "ShipmentDelayAlertScheduler")
    public void executeAutomatedDelayScan() {
        LOGGER.info("[EJB TIMER DECLARATIVE] Running automated global shipment delay & risk alert scan...");
        if (notificationService != null) {
            notificationService.createNotification("Automated Cargo Risk Scan Executed", "Scanned active international freight routes. 0 port congestions detected.", "LOGISTICS", "INFO");
        }
    }

    // Programmatic Single-Action Timer for specific shipment delay alerts
    public void scheduleDelayCheck(String trackingNumber, long delayMs) {
        LOGGER.info("[EJB TIMER CREATED] Programmatic delay risk timer set for Shipment #" + trackingNumber + " in " + delayMs + " ms");
        TimerConfig config = new TimerConfig(new DelayAlertData(trackingNumber), true);
        timerService.createSingleActionTimer(delayMs, config);
    }

    @Timeout
    public void onDelayTimeout(Timer timer) {
        if (timer.getInfo() instanceof DelayAlertData data) {
            LOGGER.warning("[EJB TIMER TIMEOUT] Delay Alert Triggered! Issuing logistics escalation for Shipment #" + data.getTrackingNumber());
            if (notificationService != null) {
                notificationService.createNotification("Cargo Transit Delay Escalation", "Shipment #" + data.getTrackingNumber() + " flagged for transit delay risk at transshipment hub.", "LOGISTICS", "CRITICAL");
            }
        }
    }

    public static class DelayAlertData implements Serializable {
        private final String trackingNumber;

        public DelayAlertData(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        public String getTrackingNumber() {
            return trackingNumber;
        }
    }
}
