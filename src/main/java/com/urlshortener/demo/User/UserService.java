package com.urlshortener.demo.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username);
    }
    public User findById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    public User registerUser(String username, String password, String email, String... roles){

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
