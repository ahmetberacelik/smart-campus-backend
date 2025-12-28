package com.smartcampus.meal.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {
    private Long id;
    private String email;
    private String role;

    // Constructor with userId and role only (backward compatible)
    public CustomUserDetails(Long id, String role) {
        this.id = id;
        this.role = role;
    }

    // Constructor with userId, email and role
    public CustomUserDetails(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    // Constructor with email and role only (for tokens without userId)
    public CustomUserDetails(String email, String role) {
        this.email = email;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        if (email != null) {
            return email;
        }
        return id != null ? id.toString() : null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
