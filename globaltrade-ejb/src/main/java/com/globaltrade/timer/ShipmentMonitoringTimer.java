package com.globaltrade.timer;

import jakarta.annotation.Resource;
import jakarta.ejb.*;

import java.io.Serializable;
import java.util.logging.Logger;

@Stateless
public class ShipmentMonitoringTimer {

    private static final Logger LOGGER = Logger.getLogger(ShipmentMonitoringTimer.class.getName());

    @Resource
    private TimerService timerService;

    @EJB
    private com.globaltrade.service.NotificationService notificationService;

    public void scheduleTrackingCheck(String trackingNumber, long delayMs) {
        LOGGER.info("[EJB TIMER CREATED] Programmatic single-action timer scheduled for Shipment #" + trackingNumber + " in " + delayMs + " ms");
        TimerConfig config = new TimerConfig(new TrackingData(trackingNumber), true);
        timerService.createSingleActionTimer(delayMs, config);
    }

    @Timeout
    public void onTimeout(Timer timer) {
        if (timer.getInfo() instanceof TrackingData data) {
            LOGGER.info("[EJB TIMER TIMEOUT] Executing status tracking update for Shipment #" + data.getTrackingNumber());
            if (notificationService != null) {
                notificationService.createNotification("Shipment Status Tracking Advanced", "Shipment #" + data.getTrackingNumber() + " tracking checkpoint updated to Next Air Cargo Transit Stop.", "LOGISTICS", "SUCCESS");
            }
        }
    }

    public static class TrackingData implements Serializable {
        private final String trackingNumber;

        public TrackingData(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        public String getTrackingNumber() {
            return trackingNumber;
        }
    }
}
