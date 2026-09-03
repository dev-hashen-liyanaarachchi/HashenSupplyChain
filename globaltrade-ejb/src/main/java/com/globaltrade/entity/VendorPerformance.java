package com.globaltrade.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_performances")
public class VendorPerformance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "fulfillment_score", nullable = false)
    private Double fulfillmentScore;

    @Column(name = "on_time_delivery_rate", nullable = false)
    private Double onTimeDeliveryRate;

    @Column(name = "quality_rating", nullable = false)
    private Double qualityRating;

    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private LocalDateTime evaluatedAt = LocalDateTime.now();

    public VendorPerformance() {}

    public VendorPerformance(Vendor vendor, Double fulfillmentScore, Double onTimeDeliveryRate, Double qualityRating) {
        this.vendor = vendor;
        this.fulfillmentScore = fulfillmentScore;
        this.onTimeDeliveryRate = onTimeDeliveryRate;
        this.qualityRating = qualityRating;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public Double getFulfillmentScore() { return fulfillmentScore; }
    public void setFulfillmentScore(Double fulfillmentScore) { this.fulfillmentScore = fulfillmentScore; }

    public Double getOnTimeDeliveryRate() { return onTimeDeliveryRate; }
    public void setOnTimeDeliveryRate(Double onTimeDeliveryRate) { this.onTimeDeliveryRate = onTimeDeliveryRate; }

    public Double getQualityRating() { return qualityRating; }
    public void setQualityRating(Double qualityRating) { this.qualityRating = qualityRating; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
