package com.pantrypal.config;

import com.pantrypal.security.PantryUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Session-based authentication for a same-origin vanilla JS frontend.
 * <ul>
 *   <li>Passwords are hashed with BCrypt.</li>
 *   <li>Login/registration are JSON endpoints in AuthController; the session cookie is HttpOnly.</li>
 *   <li>CSRF protection is on. The frontend fetches a token from /api/auth/csrf and sends it in the X-CSRF-TOKEN header.</li>
 *   <li>Unauthenticated API calls get 401 (no redirect), so the frontend can send the user to the login page.</li>
 *   <li>Static pages are public; all user data is behind /api/** and scoped to the logged-in user.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager(PantryUserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository contextRepository) throws Exception {
        http
            .securityContext(c -> c.securityContextRepository(contextRepository))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/csrf", "/api/auth/me").permitAll()
                // guests may browse the recipe catalogue from the landing page
                .requestMatchers(HttpMethod.GET, "/api/recipes", "/api/recipes/*", "/api/meta").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll())
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(l -> l
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((req, res, a) -> res.setStatus(HttpStatus.NO_CONTENT.value())));
        return http.build();
    }
}
