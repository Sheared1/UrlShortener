package com.urlshortener.demo.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest user){

        if (userService.findByUsername(user.getUsername()) != null){
            return ResponseEntity.badRequest().body("Error: Username already exists.");
        }
        if (user.getPassword().length() < 8){
            return ResponseEntity.badRequest().body("Error: Password must be at least 8 characters long.");
        }
        if (!user.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")){
            return ResponseEntity.badRequest().body("Error: Password must contain at least one uppercase letter, one lowercase letter, and one number.");
        }
        if (!user.getPassword().matches("^(?=.*[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$")){
            return ResponseEntity.badRequest().body("Error: Password must contain at least one special character.");
        }

        User registeredUser = userService.registerUser(user.getUsername(), user.getPassword(), "USER");

        //Not returning User object because we do not want to expose sensitive information.
        UserResponse userResponse = new UserResponse(registeredUser.getUsername(), registeredUser.getRoles());

        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully.");

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){

        if (userService.findById(id) == null){
            return ResponseEntity.badRequest().body("Error: User does not exist.");
        }

        userService.deleteUser(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User deleted.");

    }

}
