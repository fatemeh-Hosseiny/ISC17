package com.example.service;

import com.example.model.User;
import com.example.model.Wallet;
import com.example.model.domain.UserGender;
import com.example.model.domain.UserStatusForMan;
import com.example.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepo userRepo;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Transactional
    public User createUser(String fullName, LocalDate dateOfBirth, String phoneNumber,
                           String email, String password, UserStatusForMan userStatusForMan,
                           UserGender gender) {
        logger.info("Attempting to create user: {}", email);

        // Validate user status
        if (userStatusForMan != UserStatusForMan.yes && gender != UserGender.Female ) {
            logger.error("User status must be 'yes'. Provided: {}", userStatusForMan);
            throw new IllegalArgumentException("User status must be 'yes'.");
        }

        User newUser = new User(fullName, dateOfBirth, phoneNumber, email,
                password, userStatusForMan, gender);

        // Validate age
        if (!newUser.isEligible()) {
            logger.error("User is not eligible. Must be at least 18 years old. Date of Birth: {}", dateOfBirth);
            throw new IllegalArgumentException("User must be at least 18 years old.");
        }
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.valueOf(10000)); // Start with the minimum balance
        wallet.setUser(newUser);
        newUser.setWallet(wallet);

        User savedUser = userRepo.save(newUser);
        logger.info("User created successfully: {}", savedUser.getEmail());
        return savedUser;
    }
    public Optional<User> login(String email, String password, String phoneNumber) {
        logger.info("Login attempt for email: {}", email);

        // Find the user with email
        Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Validate password and phone number
            if (passwordEncoder.matches(password, user.getPassword()) && user.getPhoneNumber().equals(phoneNumber)
                    ) {
                logger.info("Login successful for user: {}", email);
                return Optional.of(user); // Successful login
            } else {
                logger.warn("Login failed for user: {}. Incorrect password or phone number.", email);
            }
        } else {
            logger.warn("No user found with email: {}", email);
        }

        return Optional.empty(); // Login failed
    }

    public User findUserByFullName(String fullName) {
        logger.info("Searching for user by full name: {}", fullName);
        return (User) userRepo.findByfullName(fullName).orElse(null);
    }

    public User findUserById(Long id) {
        logger.info("Searching for user by ID: {}", id);
        return userRepo.findById(id).orElse(null);
    }
    @Transactional
    public User updateUser(Long id, User updatedUserData) {
        logger.info("Updating user with ID: {}", id);
        User existingUser = findUserById(id);

        if (existingUser == null) {
            logger.error("User update failed: User not found with ID: {}", id);
            throw new IllegalArgumentException("User not found");
        }

        existingUser.setfullName(updatedUserData.getFullName());
        existingUser.setDateOfBirth(updatedUserData.getDateOfBirth());
        existingUser.setPhoneNumber(updatedUserData.getPhoneNumber());
        existingUser.setEmail(updatedUserData.getEmail());
        existingUser.setGender(updatedUserData.getGender());
        existingUser.setUserStatusForMan(updatedUserData.getUserStatusForMan());

        userRepo.save(existingUser);
        logger.info("User updated successfully: {}", existingUser.getId());
        return existingUser;
    }

    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);
        if (userRepo.existsById(id)) {
            userRepo.deleteById(id);
            logger.info("User deleted successfully: {}", id);
        } else {
            logger.error("User deletion failed: User not found with ID: {}", id);
            throw new IllegalArgumentException("User not found");
        }
    }
}
