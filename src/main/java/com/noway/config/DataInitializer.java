package com.noway.config;

import com.noway.entity.Product;
import com.noway.entity.Role;
import com.noway.entity.User;
import com.noway.repository.ProductRepository;
import com.noway.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProductRepository productRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDefaultAdmin();
        seedSampleProducts();
    }

    private void seedDefaultAdmin() {
        if (userRepository.count() == 0) {
            User admin = new User("Admin", "admin@noway.com", passwordEncoder.encode("Admin@123"), Role.ADMIN);
            userRepository.save(admin);
            log.info("Default ADMIN account created -> email: admin@noway.com | password: Admin@123");
        }
    }

    private void seedSampleProducts() {
        if (productRepository.count() == 0) {
            List<Product> products = List.of(
                    new Product("Laptop",
                            "Powerful and lightweight laptop perfect for work, study and entertainment. "
                                    + "Features a fast processor, a crisp display and long battery life.",
                            new BigDecimal("45000"), "Electronics", "/images/laptop.svg", 10, null),
                    new Product("Smartphone",
                            "Feature-packed smartphone with a stunning display, an excellent camera "
                                    + "and all-day battery life.",
                            new BigDecimal("18000"), "Electronics", "/images/smartphone.svg", 20, null),
                    new Product("Headphones",
                            "Comfortable over-ear headphones with rich sound quality and noise isolation "
                                    + "for an immersive listening experience.",
                            new BigDecimal("2500"), "Accessories", "/images/headphones.svg", 30, null),
                    new Product("Keyboard",
                            "Responsive keyboard with durable keys, ideal for typing and gaming.",
                            new BigDecimal("1500"), "Accessories", "/images/keyboard.svg", 25, null),
                    new Product("Smart Watch",
                            "Track your fitness, receive notifications and monitor your health "
                                    + "with this sleek smart watch.",
                            new BigDecimal("5000"), "Wearables", "/images/smartwatch.svg", 15, null)
            );
            productRepository.saveAll(products);
            log.info("Sample products seeded -> {} products created.", products.size());
        }
    }
}
