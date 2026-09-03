package com.globaltrade.entity;

import jakarta.persistence.*;
import com.globaltrade.enums.CustomsDocumentStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "customs_documents")
public class CustomsDocument implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    @Column(name = "hs_code", nullable = false, length = 30)
    private String hsCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomsDocumentStatus status = CustomsDocumentStatus.SUBMITTED;

    @Column(name = "inspected_by", length = 50)
    private String inspectedBy;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "declared_value")
    private Double declaredValue = 0.0;

    @Column(name = "duty_fee")
    private Double dutyFee = 0.0;

    @Column(name = "origin_country", length = 10)
    private String originCountry;

    @Column(name = "destination_country", length = 10)
    private String destinationCountry;

    @Column(name = "exporter_name", length = 150)
    private String exporterName;

    @Column(name = "importer_name", length = 150)
    private String importerName;

    @Column(name = "packing_list_items", length = 1000)
    private String packingListItems;

    @Column(name = "clearance_deadline")
    private LocalDateTime clearanceDeadline;

    @Column(name = "settlement_status", length = 50)
    private String settlementStatus = "PENDING_DUTY_SETTLEMENT";

    @Column(name = "assigned_carrier", length = 150)
    private String assignedCarrier;

    @Column(name = "freight_charge")
    private Double freightCharge = 180.00;

    public CustomsDocument() {
    }

    public CustomsDocument(Shipment shipment, String documentType, String hsCode) {
        this.shipment = shipment;
        this.documentType = documentType;
        this.hsCode = hsCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getHsCode() {
        return hsCode;
    }

    public void setHsCode(String hsCode) {
        this.hsCode = hsCode;
    }

    public CustomsDocumentStatus getStatus() {
        return status;
    }

    public void setStatus(CustomsDocumentStatus status) {
        this.status = status;
    }

    public String getInspectedBy() {
        return inspectedBy;
    }

    public void setInspectedBy(String inspectedBy) {
        this.inspectedBy = inspectedBy;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Double getDeclaredValue() {
        return declaredValue;
    }

    public void setDeclaredValue(Double declaredValue) {
        this.declaredValue = declaredValue;
    }

    public Double getDutyFee() {
        return dutyFee;
    }

    public void setDutyFee(Double dutyFee) {
        this.dutyFee = dutyFee;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public String getExporterName() {
        return exporterName;
    }

    public void setExporterName(String exporterName) {
        this.exporterName = exporterName;
    }

    public String getImporterName() {
        return importerName;
    }

    public void setImporterName(String importerName) {
        this.importerName = importerName;
    }

    public String getPackingListItems() {
        return packingListItems;
    }

    public void setPackingListItems(String packingListItems) {
        this.packingListItems = packingListItems;
    }

    public LocalDateTime getClearanceDeadline() {
        return clearanceDeadline;
    }

    public void setClearanceDeadline(LocalDateTime clearanceDeadline) {
        this.clearanceDeadline = clearanceDeadline;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    public String getAssignedCarrier() {
        return assignedCarrier;
    }

    public void setAssignedCarrier(String assignedCarrier) {
        this.assignedCarrier = assignedCarrier;
    }

    public Double getFreightCharge() {
        return freightCharge;
    }

    public void setFreightCharge(Double freightCharge) {
        this.freightCharge = freightCharge;
    }
}
