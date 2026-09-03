package com.globaltrade.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.Inventory;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class InventoryRepository {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public void save(Inventory inventory) {
        em.persist(inventory);
    }

    public Inventory update(Inventory inventory) {
        return em.merge(inventory);
    }

    public Optional<Inventory> findById(Long id) {
        return Optional.ofNullable(em.find(Inventory.class, id));
    }

    public Optional<Inventory> findByWarehouseAndProductWithLock(Long warehouseId, Long productId) {
        return em.createQuery(
                        "SELECT i FROM Inventory i WHERE i.warehouse.id = :wId AND i.product.id = :pId", Inventory.class)
                .setParameter("wId", warehouseId)
                .setParameter("pId", productId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst();
    }

    public List<Inventory> findLowStockItems() {
        return em.createQuery(
                        "SELECT i FROM Inventory i WHERE i.availableQty <= i.reorderThreshold", Inventory.class)
                .getResultList();
    }
}
