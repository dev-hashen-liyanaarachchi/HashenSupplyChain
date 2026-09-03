package com.globaltrade.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.Vendor;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VendorRepository {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public void save(Vendor vendor) {
        em.persist(vendor);
    }

    public Vendor update(Vendor vendor) {
        return em.merge(vendor);
    }

    public Optional<Vendor> findById(Long id) {
        return Optional.ofNullable(em.find(Vendor.class, id));
    }

    public Optional<Vendor> findByTaxId(String taxId) {
        return em.createQuery("SELECT v FROM Vendor v WHERE v.taxIdentificationNumber = :taxId", Vendor.class)
                .setParameter("taxId", taxId)
                .getResultStream()
                .findFirst();
    }

    public List<Vendor> findAll() {
        return em.createQuery("SELECT v FROM Vendor v", Vendor.class).getResultList();
    }
}
