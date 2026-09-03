package com.globaltrade.entity;

import jakarta.persistence.*;
import com.globaltrade.enums.VendorStatus;

import java.io.Serializable;

@Entity
@Table(name = "vendors")
public class Vendor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "tax_identification_number", unique = true, nullable = false, length = 50)
    private String taxIdentificationNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VendorStatus status = VendorStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    public Vendor() {
    }

    public Vendor(String companyName) {
        this.companyName = companyName;
        this.taxIdentificationNumber = "TIN-" + System.currentTimeMillis();
    }

    public Vendor(User user, String companyName, String taxIdentificationNumber, Address address) {
        this.user = user;
        this.companyName = companyName;
        this.taxIdentificationNumber = taxIdentificationNumber;
        this.address = address;
    }

    public String getEmail() {
        try {
            if (user != null && user.getEmail() != null) {
                return user.getEmail();
            }
        } catch (Exception ignored) {
        }
        return "supplier@" + (companyName != null ? companyName.toLowerCase().replaceAll("[^a-z]", "") : "vendor") + ".com";
    }

    public String getName() {
        return companyName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTaxIdentificationNumber() {
        return taxIdentificationNumber;
    }

    public void setTaxIdentificationNumber(String taxIdentificationNumber) {
        this.taxIdentificationNumber = taxIdentificationNumber;
    }

    public VendorStatus getStatus() {
        return status;
    }

    public void setStatus(VendorStatus status) {
        this.status = status;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
