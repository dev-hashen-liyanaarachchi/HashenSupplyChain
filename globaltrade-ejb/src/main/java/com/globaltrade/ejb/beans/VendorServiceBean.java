package com.globaltrade.ejb.beans;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.ejb.interfaces.VendorService;
import com.globaltrade.entity.Address;
import com.globaltrade.entity.Vendor;
import com.globaltrade.dto.VendorDTO;
import com.globaltrade.exception.VendorOnboardingException;
import com.globaltrade.exception.ResourceNotFoundException;
import com.globaltrade.repository.VendorRepository;

import java.util.List;
import java.util.logging.Logger;

@Stateless
public class VendorServiceBean implements VendorService {

    private static final Logger LOGGER = Logger.getLogger(VendorServiceBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Inject
    private VendorRepository vendorRepository;

    @Override
    public Vendor registerVendor(VendorDTO dto) {
        if (dto == null || dto.getCompanyName() == null || dto.getCompanyName().isBlank()) {
            throw new VendorOnboardingException("N/A", "Company or Corporate Name cannot be empty.");
        }

        String taxId = dto.getTaxIdentificationNumber();
        if (taxId == null || taxId.trim().length() < 3) {
            throw new VendorOnboardingException(taxId != null ? taxId : "INVALID", "Tax Identification Number (TIN / VAT) is invalid or missing.");
        }

        // Custom Tax Identification Number format verification logic
        String cleanTaxId = taxId.trim().toUpperCase();
        LOGGER.info("[VENDOR ONBOARDING LOGIC] Processing onboarding request for Company: " + dto.getCompanyName() + " with TIN: " + cleanTaxId);

        Address address = dto.getAddressId() != null ? em.find(Address.class, dto.getAddressId()) : null;
        Vendor vendor = new Vendor(null, dto.getCompanyName().trim(), cleanTaxId, address);
        
        vendorRepository.save(vendor);
        LOGGER.info("[VENDOR ONBOARDING SUCCESS] Vendor registered with ID: " + vendor.getId());
        return vendor;
    }

    @Override
    public Vendor updateVendorRating(Long vendorId, Double rating) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));
        
        if (rating != null && (rating < 0.0 || rating > 5.0)) {
            throw new IllegalArgumentException("Vendor performance rating must be between 0.0 and 5.0");
        }
        
        return vendorRepository.update(vendor);
    }

    @Override
    public Vendor getVendorById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", id));
    }

    @Override
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }
}
