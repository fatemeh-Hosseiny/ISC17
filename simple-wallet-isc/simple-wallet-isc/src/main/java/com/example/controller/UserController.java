package com.example.controller;

import com.example.model.User;
import com.example.model.domain.UserGender;
import com.example.model.domain.UserStatusForMan;
import com.example.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @RequestParam @NotBlank String fullName,
            @RequestParam @NotNull @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,
            @RequestParam @NotBlank String phoneNumber,
            @RequestParam @Email(message = "Email should be valid") @NotBlank String email,
            @RequestParam @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
            @RequestParam @NotNull UserStatusForMan userStatusForMan,
            @RequestParam @NotNull UserGender gender) {

        logger.info("Attempting to register user: {}", email);
        try {
            User newUser = userService.createUser(fullName, dateOfBirth, phoneNumber, email, password, userStatusForMan, gender);
            logger.info("User registered successfully: {}", email);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (IllegalArgumentException e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam @Email(message = "Email should be valid") @NotBlank String email,
            @RequestParam @NotBlank String password,
            @RequestParam @NotBlank String phoneNumber) {

        logger.info("Login attempt for user: {}", email);
        Optional<User> user = userService.login(email, password, phoneNumber);
        if (user.isPresent()) {
            logger.info("Login successful for user: {}", email);
            return ResponseEntity.ok("Login successful!");
        } else {
            logger.warn("Login failed for user: {}. Incorrect credentials.", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed!");
        }
    }


    @GetMapping("/getUser/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }
    @PutMapping("/updateUser/{id}")
    public ResponseEntity<User> updateUser(@PathVariable @Min(1) Long id,
                                           @Valid @RequestBody User updatedUser) {
        updatedUser.setId(id); // Ensure the ID is set for the update
        logger.info("Updating user with ID: {}", id);
        try {
            userService.updateUser(id,updatedUser);
            logger.info("User updated successfully: {}", id);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        try {
            userService.deleteUser(id);
            logger.info("User deleted successfully: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Deletion failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<User> getUserByFullName(@RequestParam String fullName) {
        User user = userService.findUserByFullName(fullName);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    // Global exception handling for validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage)));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
