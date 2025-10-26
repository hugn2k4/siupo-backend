package com.siupo.restaurant.service.product;

import com.siupo.restaurant.dto.ProductDTO;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.model.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    Page<ProductDTO> getAllProducts(int page, int size, String sortBy);
    ProductDTO getProductById(Long id);
    Page<ProductDTO> searchAndFilterProducts(String name, List<Long> categoryIds, Double minPrice, Double maxPrice, int page, int size, String sortBy);
    Product getProductEntityById(Long id);
}