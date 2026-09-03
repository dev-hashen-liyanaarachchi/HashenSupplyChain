package com.globaltrade.ejb.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.ejb.interfaces.ProductService;
import com.globaltrade.entity.Product;
import com.globaltrade.entity.Category;
import com.globaltrade.entity.Brand;
import com.globaltrade.dto.ProductDTO;

import java.util.List;

@Stateless
public class ProductServiceBean implements ProductService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Override
    public Product createProduct(ProductDTO dto) {
        Category category = dto.getCategoryId() != null ? em.find(Category.class, dto.getCategoryId()) : null;
        Brand brand = dto.getBrandId() != null ? em.find(Brand.class, dto.getBrandId()) : null;

        Product product = new Product(dto.getSku(), dto.getName(), dto.getPrice(), dto.getHsCode());
        product.setDescription(dto.getDescription());
        product.setCategory(category);
        product.setBrand(brand);

        em.persist(product);
        return product;
    }

    @Override
    public Product getProductBySku(String sku) {
        if (sku == null || sku.isBlank()) return null;
        List<Product> list = em.createQuery("SELECT p FROM Product p WHERE p.sku = :sku", Product.class)
                .setParameter("sku", sku)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Product> getAllProducts() {
        return em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
    }

    @Override
    public Product updateProduct(Long id, ProductDTO dto) {
        Product product = em.find(Product.class, id);
        if (product == null) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }

        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setHsCode(dto.getHsCode());
        product.setDescription(dto.getDescription());

        return em.merge(product);
    }
}
