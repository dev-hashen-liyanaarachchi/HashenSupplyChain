package com.globaltrade.ejb.interfaces;

import jakarta.ejb.Local;
import com.globaltrade.entity.Inventory;
import com.globaltrade.dto.InventoryDTO;
import com.globaltrade.exception.InventoryException;
import java.util.List;

@Local
public interface InventoryService {
    void reserveStock(Long warehouseId, Long productId, int quantity) throws InventoryException;
    void deductStock(Long warehouseId, Long productId, int quantity) throws InventoryException;
    Inventory addInventory(InventoryDTO dto);
    List<Inventory> checkLowStockItems();
}
