package com.globaltrade.ejb.beans;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.ejb.interfaces.VendorService;
import com.globaltrade.entity.Address;
import com.globaltrade.entity.Vendor;
import com.globaltrade.dto.VendorDTO;
import com.globaltrade.repository.VendorRepository;

import java.util.List;

@Stateless
public class VendorServiceBean implements VendorService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Inject
    private VendorRepository vendorRepository;

    @Override
    public Vendor registerVendor(VendorDTO dto) {
        Address address = dto.getAddressId() != null ? em.find(Address.class, dto.getAddressId()) : null;
        Vendor vendor = new Vendor(null, dto.getCompanyName(), dto.getTaxIdentificationNumber(), address);
        vendorRepository.save(vendor);
        return vendor;
    }

    @Override
    public Vendor updateVendorRating(Long vendorId, Double rating) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found with ID: " + vendorId));
        return vendorRepository.update(vendor);
    }

    @Override
    public Vendor getVendorById(Long id) {
        return vendorRepository.findById(id).orElse(null);
    }

    @Override
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }
}
