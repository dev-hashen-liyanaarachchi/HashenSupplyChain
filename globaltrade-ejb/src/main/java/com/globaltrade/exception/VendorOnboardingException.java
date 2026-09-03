package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class VendorOnboardingException extends GlobalTradeException {

    private final String taxId;

    public VendorOnboardingException(String taxId, String reason) {
        super("Vendor Onboarding Failed for Tax ID [" + taxId + "]: " + reason);
        this.taxId = taxId;
    }

    public String getTaxId() {
        return taxId;
    }
}
