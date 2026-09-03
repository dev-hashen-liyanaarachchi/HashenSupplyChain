package com.globaltrade.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import com.globaltrade.dto.ProductDTO;
import com.globaltrade.dto.ShipmentDTO;
import jakarta.interceptor.Interceptor;

import java.util.logging.Logger;

@Interceptor
public class VendorValidationInterceptor {

    private static final Logger LOGGER = Logger.getLogger(VendorValidationInterceptor.class.getName());

    @AroundInvoke
    public Object validateVendorPayload(InvocationContext context) throws Exception {
        Object[] params = context.getParameters();
        if (params != null) {
            for (Object param : params) {
                if (param instanceof ProductDTO dto) {
                    LOGGER.info("[VENDOR VALIDATION] Validating Product SKU: " + dto.getSku());
                    if (dto.getPrice() == null || dto.getPrice() <= 0) {
                        throw new IllegalArgumentException("Invalid Product Price: Must be greater than zero.");
                    }
                    if (dto.getHsCode() == null || !dto.getHsCode().matches("^\\d{4}\\.\\d{2}$")) {
                        throw new IllegalArgumentException("Invalid HS Code format: Must be XXXX.XX (e.g. 9018.90).");
                    }
                } else if (param instanceof ShipmentDTO dto) {
                    LOGGER.info("[VENDOR VALIDATION] Validating Shipment HS Code: " + dto.getHsCode());
                    if (dto.getHsCode() == null || !dto.getHsCode().matches("^\\d{4}\\.\\d{2}$")) {
                        throw new IllegalArgumentException("Invalid HS Code format: Must be XXXX.XX (e.g. 9018.90).");
                    }
                }
            }
        }
        return context.proceed();
    }
}
