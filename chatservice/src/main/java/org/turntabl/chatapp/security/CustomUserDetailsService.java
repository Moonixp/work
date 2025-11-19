package org.turntabl.chatapp.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.User;
import org.turntabl.chatapp.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User domainUser;
        try {
            domainUser = userRepository.findByEmailAuth(email);
        } catch (ChatAppException ex) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        String roleName = "ROLE_" + domainUser.getRole();

        return new org.springframework.security.core.userdetails.User(
                domainUser.getEmail(),
                domainUser.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(roleName)));
    }
}
