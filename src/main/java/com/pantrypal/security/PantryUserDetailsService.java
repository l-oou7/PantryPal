package com.pantrypal.security;

import com.pantrypal.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PantryUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public PantryUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return users.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .map(PantryUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + email));
    }
}
