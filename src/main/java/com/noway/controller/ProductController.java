package com.noway.controller;

import com.noway.dto.ProductResponse;
import com.noway.exception.ProductNotFoundException;
import com.noway.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public String productsPage(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String category,
                               @RequestParam(name = "error", required = false) String error,
                               Model model) {
        model.addAttribute("products", productService.getProducts(search, category));
        model.addAttribute("categories", productService.getCategories());
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("selectedCategory", category == null ? "" : category);
        if (error != null) {
            model.addAttribute("error", "Product not found.");
        }
        return "products";
    }

    @GetMapping("/products/{id}")
    public String productDetailsPage(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("product", productService.getProduct(id));
        } catch (ProductNotFoundException ex) {
            return "redirect:/products?error=not-found";
        }
        return "product-details";
    }

    @GetMapping("/api/products")
    @ResponseBody
    public List<ProductResponse> productsApi(@RequestParam(required = false) String search,
                                             @RequestParam(required = false) String category) {
        return productService.getProducts(search, category).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ProductResponse productApi(@PathVariable Long id) {
        return ProductResponse.from(productService.getProduct(id));
    }
}
