package com.example.servicemonitor.service;

import com.example.servicemonitor.domain.AppUser;
import com.example.servicemonitor.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public DbUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser u = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!u.isEnabled()) {
            throw new UsernameNotFoundException("User disabled: " + username);
        }

        String role = "ROLE_" + u.getRole().name();
        return new User(u.getUsername(), u.getPasswordHash(), List.of(new SimpleGrantedAuthority(role)));
    }
}
