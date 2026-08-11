package com.noway.repository;

import com.noway.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByNameContainingIgnoreCaseAndCategoryIgnoreCase(String name, String category);

    @Query("select distinct p.category from Product p order by p.category asc")
    List<String> findDistinctCategories();
}
