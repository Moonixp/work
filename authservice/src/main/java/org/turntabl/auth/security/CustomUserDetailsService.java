package org.turntabl.auth.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.turntabl.auth.exception.AuthException;
import org.turntabl.auth.model.User;
import org.turntabl.auth.repository.AuthRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User domainUser;
        try {
            domainUser = repository.findByEmail(email);
        } catch (AuthException ex) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        String roleName = "ROLE_" + domainUser.getRole();

        return new org.springframework.security.core.userdetails.User(
                domainUser.getEmail(),
                domainUser.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(roleName)));
    }
}
