package com.globaltrade.dto;

import com.globaltrade.enums.ShipmentType;

import java.io.Serializable;

public class ShipmentDTO implements Serializable {
    private Long orderId;
    private Long carrierId;
    private ShipmentType shipmentType;
    private Long originWarehouseId;
    private Long destinationAddressId;
    private String hsCode;

    public ShipmentDTO() {
    }

    public ShipmentDTO(Long orderId, ShipmentType shipmentType, Long originWarehouseId, Long destinationAddressId, String hsCode) {
        this.orderId = orderId;
        this.shipmentType = shipmentType;
        this.originWarehouseId = originWarehouseId;
        this.destinationAddressId = destinationAddressId;
        this.hsCode = hsCode;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Long carrierId) {
        this.carrierId = carrierId;
    }

    public ShipmentType getShipmentType() {
        return shipmentType;
    }

    public void setShipmentType(ShipmentType shipmentType) {
        this.shipmentType = shipmentType;
    }

    public Long getOriginWarehouseId() {
        return originWarehouseId;
    }

    public void setOriginWarehouseId(Long originWarehouseId) {
        this.originWarehouseId = originWarehouseId;
    }

    public Long getDestinationAddressId() {
        return destinationAddressId;
    }

    public void setDestinationAddressId(Long destinationAddressId) {
        this.destinationAddressId = destinationAddressId;
    }

    public String getHsCode() {
        return hsCode;
    }

    public void setHsCode(String hsCode) {
        this.hsCode = hsCode;
    }
}
