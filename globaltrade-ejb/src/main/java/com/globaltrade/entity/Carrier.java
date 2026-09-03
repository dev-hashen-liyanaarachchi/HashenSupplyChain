package com.globaltrade.entity;

import jakarta.persistence.*;
import com.globaltrade.enums.ShipmentType;

import java.io.Serializable;

@Entity
@Table(name = "carriers")
public class Carrier implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier_type", nullable = false, length = 30)
    private ShipmentType carrierType;

    @Column(name = "api_endpoint", length = 255)
    private String apiEndpoint;

    @Column(name = "country_code", length = 10)
    private String countryCode = "LK";

    @Column(name = "contact_phone", length = 50)
    private String contactPhone = "+94 11 234 5678";

    @Column(name = "contact_email", length = 100)
    private String contactEmail = "dispatch@lankalogistics.lk";

    @Column(name = "fleet_size", length = 100)
    private String fleetSize = "45 Vans & Air Fleet";

    @Column(name = "operating_status", length = 30)
    private String operatingStatus = "ACTIVE";

    public Carrier() {
    }

    public Carrier(String name, ShipmentType carrierType, String countryCode) {
        this.name = name;
        this.carrierType = carrierType;
        this.countryCode = countryCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ShipmentType getCarrierType() {
        return carrierType;
    }

    public void setCarrierType(ShipmentType carrierType) {
        this.carrierType = carrierType;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getFleetSize() {
        return fleetSize;
    }

    public void setFleetSize(String fleetSize) {
        this.fleetSize = fleetSize;
    }

    public String getOperatingStatus() {
        return operatingStatus;
    }

    public void setOperatingStatus(String operatingStatus) {
        this.operatingStatus = operatingStatus;
    }
}
