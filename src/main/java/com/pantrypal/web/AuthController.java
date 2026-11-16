package com.pantrypal.web;

import com.pantrypal.model.User;
import com.pantrypal.security.PantryUserDetails;
import com.pantrypal.service.UserService;
import com.pantrypal.web.dto.AuthDtos;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Registration, login and session info. Logout is POST /api/auth/logout (handled by Spring Security). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;
    private final AuthenticationManager authManager;
    private final SecurityContextRepository contextRepository;

    public AuthController(UserService users, AuthenticationManager authManager, SecurityContextRepository contextRepository) {
        this.users = users;
        this.authManager = authManager;
        this.contextRepository = contextRepository;
    }

    /** The frontend calls this first and sends the token back in the returned header on every change request. */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDtos.CurrentUser> register(@Valid @RequestBody AuthDtos.RegisterRequest req,
                                                         HttpServletRequest request, HttpServletResponse response) {
        User u = users.register(req);
        signIn(u.getEmail(), req.password(), request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(users.current(u.getId()));
    }

    @PostMapping("/login")
    public AuthDtos.CurrentUser login(@Valid @RequestBody AuthDtos.LoginRequest req,
                                      HttpServletRequest request, HttpServletResponse response) {
        PantryUserDetails me = signIn(req.email().trim().toLowerCase(), req.password(), request, response);
        return users.current(me.getId());
    }

    /** 200 with the user when logged in, 401 otherwise. */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal PantryUserDetails me) {
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        }
        return ResponseEntity.ok(users.current(me.getId()));
    }

    private PantryUserDetails signIn(String email, String password, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = authManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        request.getSession(true);
        request.changeSessionId(); // protects against session fixation
        contextRepository.saveContext(ctx, request, response);
        return (PantryUserDetails) auth.getPrincipal();
    }
}
