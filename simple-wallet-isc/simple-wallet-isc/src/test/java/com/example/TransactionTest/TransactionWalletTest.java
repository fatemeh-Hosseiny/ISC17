package com.example.TransactionTest;

import com.example.model.TransactionWallet;
import com.example.model.Wallet;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionWalletTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testTransactionWalletConstructor_ValidInputs() {
        // Given
        Wallet wallet = new Wallet(); // You would need to create a mock or a valid wallet object
        TransactionWallet.TransactionType type = TransactionWallet.TransactionType.DEPOSIT;
        BigDecimal amount = new BigDecimal("150000.00");

        // When
        TransactionWallet transactionWallet = new TransactionWallet(wallet, type, amount);

        // Then
        assertNotNull(transactionWallet);
        assertEquals(wallet, transactionWallet.getWallet());
        assertEquals(type, transactionWallet.getType());
        assertEquals(amount, transactionWallet.getAmount());
        assertNotNull(transactionWallet.getTransactionDate()); // should not be null
        assertEquals(LocalDateTime.now().getYear(), transactionWallet.getTransactionDate().getYear()); // same year
    }

    @Test
    void testTransactionWallet_Validation_AmountTooLow() {
        // Given
        TransactionWallet transactionWallet = new TransactionWallet();
        transactionWallet.setAmount(new BigDecimal("50000.00")); // Amount below the minimum
        transactionWallet.setWallet(new Wallet()); // Set a valid wallet object
        transactionWallet.setType(TransactionWallet.TransactionType.DEPOSIT);

        // When
        var violations = validator.validate(transactionWallet);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Amount must be at least 100,000", violations.iterator().next().getMessage());
    }

    @Test
    void testTransactionWallet_Validation_AmountTooHigh() {
        // Given
        TransactionWallet transactionWallet = new TransactionWallet();
        transactionWallet.setAmount(new BigDecimal("15000000.00")); // Amount above the maximum
        transactionWallet.setWallet(new Wallet());
        transactionWallet.setType(TransactionWallet.TransactionType.WITHDRAWAL);

        // When
        var violations = validator.validate(transactionWallet);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Amount must not exceed 10,000,000", violations.iterator().next().getMessage());
    }

    @Test
    void testTransactionWallet_Validation_NullAmount() {
        // Given
        TransactionWallet transactionWallet = new TransactionWallet();
        transactionWallet.setAmount(null); // Set amount to null
        transactionWallet.setWallet(new Wallet());
        transactionWallet.setType(TransactionWallet.TransactionType.DEPOSIT);

        // When
        var violations = validator.validate(transactionWallet);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Amount is required", violations.iterator().next().getMessage());
    }

    @Test
    void testTransactionWallet_Validation_NullWallet() {
        // Given
        TransactionWallet transactionWallet = new TransactionWallet();
        transactionWallet.setAmount(new BigDecimal("150000.00")); // Valid amount
        transactionWallet.setWallet(null); // Set wallet to null
        transactionWallet.setType(TransactionWallet.TransactionType.DEPOSIT); // Valid transaction type

        // When
        var violations = validator.validate(transactionWallet);  // Validate

        // Then
        assertFalse(violations.isEmpty());  // Check that there are violations
        assertEquals(1, violations.size());  // We expect exactly 1 violation
        assertEquals("Wallet must not be null", violations.iterator().next().getMessage());  // Validate the violation message for 'wallet'
    }

    @Test
    void testTransactionWallet_Validation_NullTransactionType() {
        // Given
        TransactionWallet transactionWallet = new TransactionWallet();
        transactionWallet.setAmount(new BigDecimal("150000.00"));
        transactionWallet.setWallet(new Wallet());
        transactionWallet.setType(null); // Set transaction type to null

        // When
        var violations = validator.validate(transactionWallet);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Transaction type is required", violations.iterator().next().getMessage());
    }
}
