package com.globaltrade.ejb.beans;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.ejb.interfaces.InventoryService;
import com.globaltrade.entity.Inventory;
import com.globaltrade.entity.Product;
import com.globaltrade.entity.Warehouse;
import com.globaltrade.dto.InventoryDTO;
import com.globaltrade.exception.InventoryException;
import com.globaltrade.repository.InventoryRepository;

import java.util.List;
import java.util.logging.Logger;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class InventoryServiceBean implements InventoryService {

    private static final Logger LOGGER = Logger.getLogger(InventoryServiceBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Inject
    private InventoryRepository inventoryRepository;

    // CMT MANDATORY: Must be executed within caller's active global transaction
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void reserveStock(Long warehouseId, Long productId, int quantity) throws InventoryException {
        LOGGER.info("[CMT MANDATORY] Reserving stock for Warehouse ID: " + warehouseId + ", Product ID: " + productId + ", Qty: " + quantity);

        Inventory inventory = inventoryRepository.findByWarehouseAndProductWithLock(warehouseId, productId)
                .orElse(null);

        if (inventory == null || inventory.getAvailableQty() < quantity) {
            Product product = em.find(Product.class, productId);
            String sku = product != null ? product.getSku() : "UNKNOWN";
            int avail = inventory != null ? inventory.getAvailableQty() : 0;
            throw new InventoryException(sku, quantity, avail);
        }

        inventory.setAvailableQty(inventory.getAvailableQty() - quantity);
        inventory.setReservedQty(inventory.getReservedQty() + quantity);
        inventoryRepository.update(inventory);
        LOGGER.info("[CMT MANDATORY SUCCESS] Stock reserved. Remaining Available: " + inventory.getAvailableQty());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deductStock(Long warehouseId, Long productId, int quantity) throws InventoryException {
        Inventory inventory = inventoryRepository.findByWarehouseAndProductWithLock(warehouseId, productId)
                .orElse(null);

        if (inventory == null || inventory.getReservedQty() < quantity) {
            Product product = em.find(Product.class, productId);
            String sku = product != null ? product.getSku() : "UNKNOWN";
            int reserved = inventory != null ? inventory.getReservedQty() : 0;
            throw new InventoryException(sku, quantity, reserved);
        }

        inventory.setReservedQty(inventory.getReservedQty() - quantity);
        inventoryRepository.update(inventory);
    }

    @Override
    public Inventory addInventory(InventoryDTO dto) {
        Warehouse warehouse = em.find(Warehouse.class, dto.getWarehouseId());
        Product product = em.find(Product.class, dto.getProductId());

        Inventory inventory = new Inventory(warehouse, product, dto.getAvailableQty(), dto.getReorderThreshold());
        inventoryRepository.save(inventory);
        return inventory;
    }

    @Override
    public List<Inventory> checkLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }
}
