package com.globaltrade.dto;

import java.io.Serializable;

public class VendorDTO implements Serializable {
    private String companyName;
    private String taxIdentificationNumber;
    private String email;
    private String phone;
    private String country;
    private String streetAddress;
    private String businessCategory;
    private String username;
    private String password;
    private Long addressId;

    public VendorDTO() {
    }

    public VendorDTO(String companyName, String taxIdentificationNumber, String email, String phone, String country, String streetAddress, String businessCategory, String username, String password) {
        this.companyName = companyName;
        this.taxIdentificationNumber = taxIdentificationNumber;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.streetAddress = streetAddress;
        this.businessCategory = businessCategory;
        this.username = username;
        this.password = password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
}
