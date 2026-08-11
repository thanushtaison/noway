package com.noway;

import com.noway.entity.Order;
import com.noway.entity.Product;
import com.noway.repository.OrderRepository;
import com.noway.repository.ProductRepository;
import com.noway.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void unauthenticated_cartPageRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void unauthenticated_checkoutRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void buyerLogin_redirectsToProducts_andCanAccessProtectedPagesButNotRoles() throws Exception {
        authService.register("Buyer", "buyer@test.com", "password123", "password123", "BUYER");

        MockHttpSession session = login("buyer@test.com", "password123", "/products");

        mockMvc.perform(get("/products").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/cart").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/seller").session(session)).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin").session(session)).andExpect(status().isForbidden());
    }

    @Test
    void sellerLogin_redirectsToSeller_andCanAccessSellerDashboard() throws Exception {
        authService.register("Seller", "seller@test.com", "password123", "password123", "SELLER");

        MockHttpSession session = login("seller@test.com", "password123", "/seller");

        mockMvc.perform(get("/seller").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/products").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/admin").session(session)).andExpect(status().isForbidden());
    }

    @Test
    void adminLogin_redirectsToAdmin_andCanAccessAdminDashboard() throws Exception {
        MockHttpSession session = login("admin@noway.com", "Admin@123", "/admin");

        mockMvc.perform(get("/admin").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/products").session(session)).andExpect(status().isOk());
    }

    @Test
    void invalidLogin_showsError() throws Exception {
        mockMvc.perform(formLogin("/login").user("nobody@noway.com").password("wrongpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    void productSearchApi_returnsMatchingProducts() throws Exception {
        mockMvc.perform(get("/api/products").param("search", "laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void categoryFilterApi_returnsMatchingProducts() throws Exception {
        mockMvc.perform(get("/api/products").param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void fullCheckoutFlow_createsOrderAndReducesStock() throws Exception {
        authService.register("Checkout Buyer", "co@test.com", "password123", "password123", "BUYER");
        MockHttpSession session = login("co@test.com", "password123", "/products");

        Product laptop = productRepository.findByNameContainingIgnoreCase("laptop").get(0);
        int stockBefore = laptop.getStock();
        int ordersBefore = orderRepository.findAll().size();

        mockMvc.perform(post("/api/cart").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": " + laptop.getId() + ", \"quantity\": 2}"))
                .andExpect(status().isOk());

        MvcResult checkoutResult = mockMvc.perform(post("/checkout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertNotNull(checkoutResult.getResponse().getRedirectedUrl());
        assertTrue(checkoutResult.getResponse().getRedirectedUrl().startsWith("/order-success?orderId="));

        assertEquals(stockBefore - 2, productRepository.findById(laptop.getId()).orElseThrow().getStock());
        assertEquals(ordersBefore + 1, orderRepository.findAll().size());

        mockMvc.perform(get("/orders").session(session)).andExpect(status().isOk());
    }

    @Test
    void checkoutWithEmptyCart_doesNotCreateOrder() throws Exception {
        authService.register("Empty Cart Buyer", "empty@test.com", "password123", "password123", "BUYER");
        MockHttpSession session = login("empty@test.com", "password123", "/products");

        int ordersBefore = orderRepository.findAll().size();
        mockMvc.perform(post("/checkout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart?error=empty"));

        assertEquals(ordersBefore, orderRepository.findAll().size());
    }

    @Test
    void oneBuyerCannotViewAnotherBuyersOrder() throws Exception {
        authService.register("Buyer A", "a@test.com", "password123", "password123", "BUYER");
        authService.register("Buyer B", "b@test.com", "password123", "password123", "BUYER");

        MockHttpSession sessionA = login("a@test.com", "password123", "/products");
        Product product = productRepository.findAll().get(0);

        mockMvc.perform(post("/api/cart").session(sessionA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": " + product.getId() + ", \"quantity\": 1}"))
                .andExpect(status().isOk());
        MvcResult checkoutResult = mockMvc.perform(post("/checkout").session(sessionA).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        long orderId = Long.parseLong(checkoutResult.getResponse().getRedirectedUrl()
                .substring("/order-success?orderId=".length()));

        Order order = orderRepository.findById(orderId).orElseThrow();

        MockHttpSession sessionB = login("b@test.com", "password123", "/products");
        mockMvc.perform(get("/orders/" + order.getId()).session(sessionB))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders?error=not-found"));
    }

    @Test
    void duplicateRegistration_isRejected() throws Exception {
        authService.register("Dup User", "dupuser@test.com", "password123", "password123", "BUYER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dup User\",\"email\":\"dupuser@test.com\","
                                + "\"password\":\"password123\",\"confirmPassword\":\"password123\",\"role\":\"BUYER\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    private MockHttpSession login(String email, String password, String expectedRedirectUrl) throws Exception {
        MvcResult result = mockMvc.perform(formLogin("/login").user(email).password(password))
                .andExpect(authenticated())
                .andExpect(redirectedUrl(expectedRedirectUrl))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
