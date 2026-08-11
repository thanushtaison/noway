package com.noway.controller;

import com.noway.dto.ApiResponse;
import com.noway.dto.LoginRequest;
import com.noway.dto.RegisterRequest;
import com.noway.entity.User;
import com.noway.exception.BadRequestException;
import com.noway.exception.UserAlreadyExistsException;
import com.noway.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "error", required = false) String error,
                            @RequestParam(name = "logout", required = false) String logout,
                            @RequestParam(name = "registered", required = false) String registered,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("logout", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("success", "Account created successfully. Please log in.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(defaultValue = "BUYER") String role,
                           Model model) {
        try {
            authService.register(name, email, password, confirmPassword, role);
            return "redirect:/login?registered=true";
        } catch (BadRequestException | UserAlreadyExistsException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("role", role);
            return "register";
        }
    }

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<ApiResponse> registerApi(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful. Please log in."));
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> loginApi(@Valid @RequestBody LoginRequest request,
                                      HttpServletRequest httpRequest,
                                      HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        SecurityContext context = new SecurityContextImpl(authentication);
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, httpRequest, httpResponse);
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Login successful. Role: " + user.getRole()));
    }

    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<ApiResponse> logoutApi(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully."));
    }
}
