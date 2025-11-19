package org.turntabl.auth.security;

// import java.util.Optional;
// import java.util.UUID;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Component;
// import org.turntabl.chatapp.exception.ChatAppException;
// import org.turntabl.chatapp.repository.UserRepository;

// import jakarta.servlet.http.HttpServletRequest;

// @Component
// public class SecurityUtils {

// @Autowired
// private JwtService jwtService;
// @Autowired
// private UserRepository userRepository;
// @Autowired
// private HttpServletRequest request;

// /**
// * Get current authenticated user's Id from JWT token
// */
// public Optional<UUID> getCurrentUserId() {
// String authHeader = request.getHeader("Authorization");
// if (authHeader != null && authHeader.startsWith("Bearer ")) {
// String token = authHeader.substring(7);
// try {
// return Optional.ofNullable(jwtService.extractUserId(token));
// } catch (Exception e) {
// Optional.empty();
// }
// }
// return Optional.empty();
// }

// /**
// * Get current authenticated user's username
// */
// public String getCurrentUsername() {
// Authentication authentication =
// SecurityContextHolder.getContext().getAuthentication();
// if (authentication != null && authentication.getPrincipal() instanceof
// UserDetails) {
// return ((UserDetails) authentication.getPrincipal()).getUsername();
// }
// return null;
// }

// /**
// * Get current user Id by looking up username in database (fallback method)
// */
// public Optional<UUID> getCurrentUserIdFromDatabase() {
// String username = getCurrentUsername();
// if (username != null) {
// try {
// var user = userRepository.findByUsername(username);
// if (user == null) {
// return Optional.empty();
// }
// return Optional.of(user.getId());
// } catch (ChatAppException ex) {
// return Optional.empty();
// }
// }
// return Optional.empty();
// }

// /**
// * Check if current user has specific role
// */
// public boolean hasRole(String role) {
// Authentication authentication =
// SecurityContextHolder.getContext().getAuthentication();
// if (authentication != null) {
// return authentication.getAuthorities().stream()
// .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
// }
// return false;
// }

// /**
// * Check if current user is a MANAGER
// */
// public boolean isManager() {
// return hasRole("MANAGER");
// }

// /**
// * Check if the current user ID matches the given user ID
// */
// public boolean isCurrentUser(UUID userId) {
// return getCurrentUserId().map(uuid -> uuid.equals(userId)).orElse(false);
// }
// }
