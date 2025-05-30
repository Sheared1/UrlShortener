package com.urlshortener.demo.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<User> findWithFilters(
            String id,
            String username,
            String email,
            String createdAt,
            String active,
            Pageable pageable
    ) {
        Specification<User> spec = Specification.where(null);

        if (id != null && !id.isEmpty()) {
            try {
                Long userId = Long.parseLong(id);
                spec = spec.and((root, query, cb) -> cb.equal(root.get("id"), userId));
            } catch (NumberFormatException ignored) {}
        }
        if (username != null && !username.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }
        if (email != null && !email.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (createdAt != null && !createdAt.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.function("DATE_FORMAT", String.class, root.get("createdAt"), cb.literal("%Y-%m-%d %H:%i:%s")), "%" + createdAt + "%"));
        }
        if (active != null && !active.isEmpty()) {
            if (active.equalsIgnoreCase("Yes")) {
                spec = spec.and((root, query, cb) -> cb.isTrue(root.get("active")));
            } else if (active.equalsIgnoreCase("No")) {
                spec = spec.and((root, query, cb) -> cb.isFalse(root.get("active")));
            }
        }

        // Always sort by createdAt descending
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        return userRepository.findAll(spec, pageable);
    }

    public ResponseEntity<?> toggleActive(Long id, boolean isActive) {

        logger.info("Toggling active status for user with ID: " + id);

        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setActive(isActive);
            userRepository.save(user);
            return ResponseEntity.ok().body(Map.of("message", "User active status updated successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }

    }

    public ResponseEntity<?> updateLastLoginTime(String username) {

        logger.info("Updating last login time for user: " + username);

        User user = userRepository.findByUsername(username);
        if (user != null){
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }
        else {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }
        return ResponseEntity.ok().body(Map.of("message", "User last login time updated successfully"));
    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username);
    }
    public User findById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    public User registerUser(String username, String password, String email, String... roles){

        logger.info("Registering new user with username: {} and email {}", username, email);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setEmailVerified(false);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        if (roles == null || roles.length == 0){
            user.setRoles(new HashSet<>(Arrays.asList("USER"))); //Setting USER role by default.
        }
        else {
            user.setRoles(new HashSet<>(Arrays.asList(roles)));
        }

        return userRepository.save(user);

    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void setEmailVerified(User user, boolean verified) {
        user.setEmailVerified(verified);
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

}
