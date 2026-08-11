package com.noway.service;

import com.noway.entity.Product;
import com.noway.exception.ProductNotFoundException;
import com.noway.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProducts(String search, String category) {
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (hasSearch && hasCategory) {
            return productRepository.findByNameContainingIgnoreCaseAndCategoryIgnoreCase(search.trim(), category.trim());
        }
        if (hasSearch) {
            return productRepository.findByNameContainingIgnoreCase(search.trim());
        }
        if (hasCategory) {
            return productRepository.findByCategoryIgnoreCase(category.trim());
        }
        return productRepository.findAll();
    }

    public List<String> getCategories() {
        return productRepository.findDistinctCategories();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findAll().stream().limit(4).toList();
    }
}
