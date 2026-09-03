package com.globaltrade.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.Warehouse;
import com.globaltrade.entity.Address;
import com.globaltrade.entity.Country;
import java.util.List;
import java.util.Optional;

@Stateless
public class WarehouseService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public Warehouse createWarehouse(String name, String streetLine1, String city, String state, String postalCode, String countryCode, Integer maxCapacity) {
        String ccode = (countryCode != null && !countryCode.isBlank()) ? countryCode.toUpperCase() : "LK";
        
        Country country = em.createQuery("SELECT c FROM Country c WHERE c.code = :code", Country.class)
                .setParameter("code", ccode)
                .getResultStream().findFirst()
                .orElseGet(() -> {
                    Country c = new Country(ccode, ccode.equals("DE") ? "Germany" : ccode.equals("LK") ? "Sri Lanka" : "United States");
                    em.persist(c);
                    return c;
                });

        Address address = new Address(
                (streetLine1 != null && !streetLine1.isBlank()) ? streetLine1 : "Main Logistics Highway",
                (city != null && !city.isBlank()) ? city : "Colombo",
                (state != null && !state.isBlank()) ? state : "Western",
                (postalCode != null && !postalCode.isBlank()) ? postalCode : "00100",
                country
        );
        em.persist(address);

        Warehouse warehouse = new Warehouse(
                name,
                address,
                (maxCapacity != null && maxCapacity > 0) ? maxCapacity : 50000
        );
        warehouse.setCurrentCapacity(0);
        em.persist(warehouse);

        return warehouse;
    }

    public List<Warehouse> getAllWarehouses() {
        return em.createQuery("SELECT DISTINCT w FROM Warehouse w LEFT JOIN FETCH w.address a LEFT JOIN FETCH a.country", Warehouse.class)
                .getResultList();
    }

    public Optional<Warehouse> getWarehouseById(Long id) {
        Warehouse warehouse = em.find(Warehouse.class, id);
        return Optional.ofNullable(warehouse);
    }

    public Warehouse updateWarehouse(Long id, String name, Integer currentCapacity, Integer maxCapacity) {
        Warehouse warehouse = em.find(Warehouse.class, id);
        if (warehouse != null) {
            if (name != null && !name.isBlank()) warehouse.setName(name);
            if (currentCapacity != null) warehouse.setCurrentCapacity(currentCapacity);
            if (maxCapacity != null && maxCapacity > 0) warehouse.setMaxCapacity(maxCapacity);
            em.merge(warehouse);
        }
        return warehouse;
    }

    public boolean deleteWarehouse(Long id) {
        Warehouse warehouse = em.find(Warehouse.class, id);
        if (warehouse != null) {
            em.remove(warehouse);
            return true;
        }
        return false;
    }
}
