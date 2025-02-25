package com.mindary.aichat.security;

import com.mindary.aichat.models.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class SystemUserDetails implements UserDetails {

    private final UUID id;
    private final String role; // Or Collection<? extends GrantedAuthority> authorities
    private final Collection<? extends GrantedAuthority> authorities;

    public SystemUserDetails(UUID id, String role) {
        this.id = id;
        this.role = role;
        this.authorities = List.of(new SimpleGrantedAuthority(role)); // Initialize authorities
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return id.toString();
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

    public UUID getId() {
        return id;
    }

    public String getRole() { return role; }
}
