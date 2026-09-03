package com.globaltrade.ejb.interfaces;

import jakarta.ejb.Local;
import com.globaltrade.entity.Vendor;
import com.globaltrade.dto.VendorDTO;

import java.util.List;

@Local
public interface VendorService {
    Vendor registerVendor(VendorDTO dto);

    Vendor updateVendorRating(Long vendorId, Double rating);

    Vendor getVendorById(Long id);

    List<Vendor> getAllVendors();
}
