package com.globaltrade.dto;

import java.io.Serializable;

public class VendorDTO implements Serializable {
    private String companyName;
    private String taxIdentificationNumber;
    private Long addressId;

    public VendorDTO() {
    }

    public VendorDTO(String companyName, String taxIdentificationNumber, Long addressId) {
        this.companyName = companyName;
        this.taxIdentificationNumber = taxIdentificationNumber;
        this.addressId = addressId;
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

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
}
