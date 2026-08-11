package com.noway.service;

import com.noway.entity.Product;
import com.noway.exception.ProductNotFoundException;
import com.noway.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product laptop() {
        return new Product("Laptop", "desc", new BigDecimal("45000"), "Electronics", "/images/laptop.svg", 10, null);
    }

    @Test
    void getProducts_noFilters_returnsAll() {
        when(productRepository.findAll()).thenReturn(List.of(laptop()));

        List<Product> result = productService.getProducts(null, null);

        assertEquals(1, result.size());
    }

    @Test
    void getProducts_searchUsesRepository() {
        when(productRepository.findByNameContainingIgnoreCase("laptop")).thenReturn(List.of(laptop()));

        List<Product> result = productService.getProducts("laptop", "");

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
    }

    @Test
    void getProducts_categoryUsesRepository() {
        when(productRepository.findByCategoryIgnoreCase("electronics")).thenReturn(List.of(laptop()));

        List<Product> result = productService.getProducts(null, "electronics");

        assertEquals(1, result.size());
    }

    @Test
    void getProduct_found_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop()));

        Product product = productService.getProduct(1L);

        assertEquals("Laptop", product.getName());
    }

    @Test
    void getProduct_notFound_throwsProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProduct(99L));
    }
}
