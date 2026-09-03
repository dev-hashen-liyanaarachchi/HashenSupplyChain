package com.globaltrade.ejb.interfaces;

import jakarta.ejb.Local;
import com.globaltrade.entity.Product;
import com.globaltrade.dto.ProductDTO;

import java.util.List;

@Local
public interface ProductService {
    Product createProduct(ProductDTO dto);

    Product updateProduct(Long productId, ProductDTO dto);

    Product getProductBySku(String sku);

    List<Product> getAllProducts();
}
