package com.example.controller;

import com.example.model.User;
import com.example.model.Wallet;
import com.example.service.UserService;
import com.example.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallets")
@Validated
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;


    @PostMapping("/{userId}")

    public ResponseEntity<Wallet> createWallet(
            @PathVariable Long userId,
            @RequestParam String accountNumber,
            @RequestParam String shabaNumber) {

        if ( userService.findUserById(userId) != null ) {
            logger.error("User found for userId: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        else {
            User user = new User();
            Wallet newWallet = walletService.createWallet(user, accountNumber, shabaNumber);
            logger.info("Created wallet for userId: {}", userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(newWallet);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam Long walletId,
            @RequestParam @NotBlank String accountNumber,
            @RequestParam @NotBlank String shabaNumber) {
        boolean success = walletService.login(walletId, accountNumber, shabaNumber);
        String message = success ? "Login successful!" : "Login failed!";
        HttpStatus status = success ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;

        logger.info("Login attempt for walletId: {} - {}", walletId, message);
        return ResponseEntity.status(status).body(message);
    }

    // Get the balance of a wallet
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long walletId) {
        BigDecimal balance = walletService.getBalance(walletId);
        if (balance == null) {
            logger.error("Wallet not found for walletId: {}", walletId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        logger.info("Fetched balance for walletId: {}", walletId);
        return ResponseEntity.ok(balance);
    }

    // Add funds to the wallet
    @PostMapping("/{walletId}/addFunds")
    public ResponseEntity<Wallet> addFunds(@PathVariable Long walletId, @Valid @RequestBody BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Invalid amount for adding funds: {}", amount);
            throw new IllegalArgumentException("Amount must be positive.");
        }

        Wallet updatedWallet = walletService.addFunds(walletId, amount);
        logger.info("Added funds to walletId: {} - Amount: {}", walletId, amount);
        return ResponseEntity.ok(updatedWallet);
    }

    // Withdraw funds from the wallet
    @PostMapping("/{walletId}/withdrawFunds")
    public ResponseEntity<Wallet> withdrawFunds(@PathVariable Long walletId, @Valid @RequestBody BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Invalid amount for withdrawing funds: {}", amount);
            throw new IllegalArgumentException("Amount must be positive.");
        }

        Wallet updatedWallet = walletService.withdrawFunds(walletId, amount);
        logger.info("Withdrew funds from walletId: {} - Amount: {}", walletId, amount);
        return ResponseEntity.ok(updatedWallet);
    }
    @PostMapping("/{userId}/foundUser")
    public ResponseEntity<String> foundUser(@PathVariable Long userId,
                                            @RequestParam String accountNumber,
                                            @RequestParam String shabaNumber){

        User user = userService.findUserById(userId);
        if ( user == null ) {
            logger.error("User not found for userId: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found for userId: " + userId);


        }
        else {
            logger.info("User found for userId: {}", userId);
            return ResponseEntity.status(HttpStatus.OK).body("User found with id: " + userId);
        }
    }

    // Global exception handling for IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        logger.error("Error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
