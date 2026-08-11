package com.noway.config;

import com.noway.security.SecurityUtils;
import com.noway.service.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    public GlobalModelAdvice(CartService cartService, SecurityUtils securityUtils) {
        this.cartService = cartService;
        this.securityUtils = securityUtils;
    }

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated()
                && !(auth.getPrincipal() instanceof String);

        if (authenticated) {
            try {
                var user = securityUtils.currentUser();
                model.addAttribute("currentUser", user);
                model.addAttribute("isAuthenticated", true);
                int cartCount = cartService.getCartItems(user).stream()
                        .mapToInt(item -> item.getQuantity())
                        .sum();
                model.addAttribute("cartCount", cartCount);
            } catch (Exception ex) {
                model.addAttribute("isAuthenticated", false);
                model.addAttribute("cartCount", 0);
            }
        } else {
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("cartCount", 0);
        }
    }
}
