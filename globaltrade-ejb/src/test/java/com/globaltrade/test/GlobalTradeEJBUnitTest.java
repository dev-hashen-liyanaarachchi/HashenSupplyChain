package com.globaltrade.test;

import com.globaltrade.exception.CustomsException;
import com.globaltrade.exception.InventoryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalTradeEJBUnitTest {

    @Test
    @DisplayName("Test 1: InventoryException Rollback Validation")
    public void testInventoryExceptionRollback() {
        InventoryException exception = assertThrows(
                InventoryException.class,
                () -> {
                    throw new InventoryException("MED-9018-X", 500, 100);
                }
        );

        assertEquals("MED-9018-X", exception.getSku());
        assertEquals(500, exception.getRequestedQty());
        assertEquals(100, exception.getAvailableQty());
    }

    @Test
    @DisplayName("Test 2: CustomsException Trade Compliance Failure")
    public void testCustomsExceptionValidation() {
        CustomsException exception = assertThrows(
                CustomsException.class,
                () -> {
                    throw new CustomsException("9999.00", "Blacklisted HS Code detected.");
                }
        );

        assertEquals("9999.00", exception.getHsCode());
        assertTrue(exception.getMessage().contains("Blacklisted HS Code"));
    }
}
