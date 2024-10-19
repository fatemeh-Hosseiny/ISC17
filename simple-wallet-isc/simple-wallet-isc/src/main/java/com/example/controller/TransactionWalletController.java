package com.example.controller;

import com.example.model.TransactionWallet;
import com.example.model.Wallet;
import com.example.service.TransactionWalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionWalletController {

    @Autowired
    private TransactionWalletService transactionWalletService;

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<TransactionWallet> deposit(
            @PathVariable Long walletId,
            @Valid @RequestBody BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        else {
        try {

            TransactionWallet transaction = transactionWalletService.deposit(walletId, amount);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
        }
    }

    // Withdraw funds from a wallet
    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<TransactionWallet> withdraw(
            @PathVariable Long walletId,
            @Valid @RequestBody BigDecimal amount) {
        try {
            TransactionWallet transaction = transactionWalletService.withdraw(walletId, amount);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<Wallet> getWalletDetails(@PathVariable Long walletId) {

        try {
            Wallet wallet = transactionWalletService.getWalletDetails(walletId);
            return ResponseEntity.ok(wallet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    // Global exception handling for IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
