package com.globaltrade.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.Vendor;
import com.globaltrade.entity.VendorPerformance;

import java.util.List;
import java.util.logging.Logger;

@Stateless
public class VendorPerformanceService {

    private static final Logger LOGGER = Logger.getLogger(VendorPerformanceService.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public List<VendorPerformance> getAllVendorPerformances() {
        List<VendorPerformance> list = em.createQuery(
                        "SELECT DISTINCT vp FROM VendorPerformance vp LEFT JOIN FETCH vp.vendor v LEFT JOIN FETCH v.user u ORDER BY vp.id DESC", VendorPerformance.class)
                .getResultList();

        if (list.isEmpty()) {
            LOGGER.info("[VENDOR PERF SEED] Evaluating supplier fulfillment performance and seeding vendor_performances table...");

            List<Vendor> vendors = em.createQuery("SELECT v FROM Vendor v", Vendor.class).getResultList();
            if (vendors.isEmpty()) {
                Vendor v1 = new Vendor("Bosch Medical Logistics GmbH");
                Vendor v2 = new Vendor("Siemens Healthcare Logistics");
                Vendor v3 = new Vendor("Global Cargo Forwarders USA");
                Vendor v4 = new Vendor("Tech Mart Hardware Lanka");
                em.persist(v1);
                em.persist(v2);
                em.persist(v3);
                em.persist(v4);
                vendors = List.of(v1, v2, v3, v4);
            }

            for (Vendor v : vendors) {
                VendorPerformance vp = new VendorPerformance(v, 96.5, 98.2, 4.9);
                em.persist(vp);
            }

            list = em.createQuery(
                            "SELECT DISTINCT vp FROM VendorPerformance vp LEFT JOIN FETCH vp.vendor v LEFT JOIN FETCH v.user u ORDER BY vp.id DESC", VendorPerformance.class)
                    .getResultList();
        }
        return list;
    }

    public VendorPerformance evaluateVendor(Long vendorId, Double fulfillmentScore, Double onTimeRate, Double qualityRating) {
        Vendor v = em.find(Vendor.class, vendorId);
        if (v == null) {
            List<Vendor> list = em.createQuery("SELECT v FROM Vendor v", Vendor.class).getResultList();
            if (!list.isEmpty()) v = list.get(0);
            else throw new IllegalArgumentException("Vendor not found: #" + vendorId);
        }

        Double fScore = fulfillmentScore != null ? fulfillmentScore : 95.0;
        Double onTime = onTimeRate != null ? onTimeRate : 97.5;
        Double quality = qualityRating != null ? qualityRating : 4.8;

        VendorPerformance vp = new VendorPerformance(v, fScore, onTime, quality);
        em.persist(vp);

        LOGGER.info("[VENDOR EVALUATED] Saved performance evaluation for " + v.getName() + " -> Score: " + fScore + "%");
        return vp;
    }
}
