package com.globaltrade.timer;

import jakarta.annotation.Resource;
import jakarta.ejb.*;

import java.io.Serializable;
import java.util.logging.Logger;

@Stateless
public class CustomsDeadlineTimer {

    private static final Logger LOGGER = Logger.getLogger(CustomsDeadlineTimer.class.getName());

    @Resource
    private TimerService timerService;

    @EJB
    private com.globaltrade.service.NotificationService notificationService;

    public void scheduleCustomsClearanceDeadline(Long documentId, long timeoutMs) {
        LOGGER.info("[PROGRAMMATIC TIMER CREATED] Scheduling persistent Customs Clearance Deadline for Doc ID: " + documentId + " in " + timeoutMs + " ms");
        TimerConfig config = new TimerConfig(new CustomsDeadlineData(documentId), true);
        timerService.createSingleActionTimer(timeoutMs, config);
    }

    @Timeout
    public void onCustomsDeadlineExpired(Timer timer) {
        if (timer.getInfo() instanceof CustomsDeadlineData data) {
            LOGGER.severe("[CUSTOMS DEADLINE EXPIRED] Customs Document ID: " + data.getDocumentId() + " failed to receive approval within designated clearance window! Alerting Customs Agent.");
            if (notificationService != null) {
                notificationService.createNotification("Customs Clearance Deadline Approaching", "Customs Document #DOC-" + data.getDocumentId() + " has reached the 48-hr compliance window limit. Action required by Customs Officer.", "CUSTOMS", "WARNING");
            }
        }
    }

    public static class CustomsDeadlineData implements Serializable {
        private final Long documentId;

        public CustomsDeadlineData(Long documentId) {
            this.documentId = documentId;
        }

        public Long getDocumentId() {
            return documentId;
        }
    }
}
