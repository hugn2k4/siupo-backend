package com.siupo.restaurant.service.product;

import com.siupo.restaurant.dto.request.ProductRequest;
import com.siupo.restaurant.dto.response.ProductResponse;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    Product getProductEntityById(Long id);
    Page<ProductResponse> getAllProducts(User user, int page, int size, String sortBy);
    ProductResponse getProductById(User user, Long id);
    Page<ProductResponse> searchAndFilterProducts(User user, String name, List<Long> categoryIds, Double minPrice, Double maxPrice, int page, int size, String sortBy);
    ProductResponse createProduct(ProductRequest productRequest);
    ProductResponse updateProduct(Long id, ProductRequest productRequest);
    void deleteProductById(Long id);
    ProductResponse updateProductStatus(Long id);
}