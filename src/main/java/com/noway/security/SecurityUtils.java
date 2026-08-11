package com.noway.security;

import com.noway.entity.User;
import com.noway.exception.UnauthorizedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new UnauthorizedAccessException("You must be logged in to perform this action.");
        }
        return (User) auth.getPrincipal();
    }
}
