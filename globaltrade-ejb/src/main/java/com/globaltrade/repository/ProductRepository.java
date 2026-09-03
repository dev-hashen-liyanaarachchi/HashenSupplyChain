package com.globaltrade.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.Product;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductRepository {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public void save(Product product) {
        em.persist(product);
    }

    public Product update(Product product) {
        return em.merge(product);
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(em.find(Product.class, id));
    }

    public Optional<Product> findBySku(String sku) {
        return em.createQuery("SELECT p FROM Product p WHERE p.sku = :sku", Product.class)
                .setParameter("sku", sku)
                .getResultStream()
                .findFirst();
    }

    public List<Product> findByVendor(Long vendorId) {
        return em.createQuery("SELECT p FROM Product p WHERE p.vendor.id = :vId", Product.class)
                .setParameter("vId", vendorId)
                .getResultList();
    }

    public List<Product> findAll() {
        return em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
    }
}
