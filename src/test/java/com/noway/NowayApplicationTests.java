package com.noway;

import com.noway.entity.Product;
import com.noway.entity.Role;
import com.noway.entity.User;
import com.noway.repository.ProductRepository;
import com.noway.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class NowayApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        assertNotNull(userRepository);
        assertNotNull(productRepository);
    }

    @Test
    void defaultAdminAccountIsSeededWithBcryptPassword() {
        User admin = userRepository.findByEmail("admin@noway.com").orElseThrow();

        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.getPassword().startsWith("$2"));
        assertTrue(passwordEncoder.matches("Admin@123", admin.getPassword()));
    }

    @Test
    void sampleProductsAreSeeded() {
        List<Product> products = productRepository.findAll();

        assertEquals(5, products.size());
        assertTrue(products.stream().anyMatch(p -> p.getName().equalsIgnoreCase("Laptop")));
        assertTrue(products.stream().anyMatch(p -> p.getCategory().equals("Electronics")));
    }
}
