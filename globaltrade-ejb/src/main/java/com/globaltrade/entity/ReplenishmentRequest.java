package com.globaltrade.entity;

import jakarta.persistence.*;
import com.globaltrade.enums.ReplenishmentStatus;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "replenishment_requests")
public class ReplenishmentRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Column(name = "requested_qty", nullable = false)
    private Integer requestedQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReplenishmentStatus status = ReplenishmentStatus.REQUESTED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ReplenishmentRequest() {}

    public ReplenishmentRequest(Inventory inventory, Integer requestedQty) {
        this.inventory = inventory;
        this.requestedQty = requestedQty;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public Integer getRequestedQty() { return requestedQty; }
    public void setRequestedQty(Integer requestedQty) { this.requestedQty = requestedQty; }

    public ReplenishmentStatus getStatus() { return status; }
    public void setStatus(ReplenishmentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
