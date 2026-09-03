package com.globaltrade.timer;

import jakarta.annotation.Resource;
import jakarta.ejb.*;

import java.io.Serializable;
import java.util.logging.Logger;

@Stateless
public class RouteOptimizationTimer {

    private static final Logger LOGGER = Logger.getLogger(RouteOptimizationTimer.class.getName());

    @Resource
    private TimerService timerService;

    public void scheduleRouteOptimization(Long shipmentId, long intervalMs) {
        LOGGER.info("[PROGRAMMATIC TIMER CREATED] Scheduling dynamic shipping route recalculation for Shipment ID: " + shipmentId);
        TimerConfig config = new TimerConfig(new RouteData(shipmentId), false);
        timerService.createSingleActionTimer(intervalMs, config);
    }

    @Timeout
    public void onRouteRecalculationTimeout(Timer timer) {
        if (timer.getInfo() instanceof RouteData data) {
            LOGGER.info("[ROUTE OPTIMIZATION EXECUTED] Recalculated optimal transit path for Shipment ID: " + data.getShipmentId());
        }
    }

    public static class RouteData implements Serializable {
        private final Long shipmentId;

        public RouteData(Long shipmentId) {
            this.shipmentId = shipmentId;
        }

        public Long getShipmentId() {
            return shipmentId;
        }
    }
}
