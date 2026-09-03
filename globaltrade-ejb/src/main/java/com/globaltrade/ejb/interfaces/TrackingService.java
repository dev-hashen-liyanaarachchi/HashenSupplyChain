package com.globaltrade.ejb.interfaces;

import jakarta.ejb.Local;
import com.globaltrade.entity.TrackingEvent;

import java.util.List;

@Local
public interface TrackingService {
    TrackingEvent addTrackingEvent(String trackingNumber, String location, String description);

    List<TrackingEvent> getTrackingHistory(String trackingNumber);
}
