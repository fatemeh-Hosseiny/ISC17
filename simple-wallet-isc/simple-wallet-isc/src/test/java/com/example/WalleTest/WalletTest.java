package com.example.WalleTest;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import com.example.model.User;
import com.example.model.Wallet;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.annotation.Validated;

@Validated
public class WalletTest {

    private Wallet wallet;
    private User user;
    private Validator validator;

    @BeforeEach
    public void setUp() {

        user = new User();
        user.setId(1L);
        user.setfullName("John Doe");


        wallet = new Wallet(user, "1234567890", "IR1234567890123456789012");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testWalletCreation_ValidData() {
        // Wallet should be created with a minimum balance of 10,000
        assertNotNull(wallet);
        assertEquals(BigDecimal.valueOf(10000), wallet.getBalance());
        assertEquals(user, wallet.getUser());
        assertNotNull(wallet.getCreationDate());
    }

    @Test
    public void testAddFunds_ValidAmount() {
        wallet.addFunds(BigDecimal.valueOf(5000));
        assertEquals(BigDecimal.valueOf(15000), wallet.getBalance());
    }

    @Test
    public void testAddFunds_InvalidAmount() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            wallet.addFunds(BigDecimal.valueOf(-500));
        });

        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void testWithdrawFunds_ValidAmount() {
        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("20000.00"));  // Initial balance

        BigDecimal withdrawAmount = new BigDecimal("5000.00");

        // Withdraw an amount that doesn't bring the balance below the minimum
        wallet.withdrawFunds(withdrawAmount);

        // Assert that the balance is updated correctly after the withdrawal
        assertEquals(new BigDecimal("15000.00"), wallet.getBalance());
    }

    @Test
    public void testWithdrawFunds_InvalidAmount() {
        // Try to withdraw negative amount
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            wallet.withdrawFunds(BigDecimal.valueOf(-200));
        });
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    public void testWithdrawFunds_ExceedingBalance() {
        // Try to withdraw an amount that brings balance below minimum (10,000)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            wallet.withdrawFunds(BigDecimal.valueOf(11000)); // This will drop below 10,000
        });

        assertEquals("Withdrawal would result in a balance below the minimum of 10,000", exception.getMessage());
    }

    @Test
    public void testLogin_ValidCredentials() {
        boolean result = wallet.login("1234567890", "IR1234567890123456789012");
        assertTrue(result);
    }

    @Test
    public void testLogin_InvalidCredentials() {
        boolean result = wallet.login("9876543210", "IR0987654321098765432109");
        assertFalse(result);
    }
    @Test
    public void testOwnerValidation_NullOwner() {
        wallet.setOwner(null);
        Set<ConstraintViolation<Wallet>> violations = validator.validate(wallet);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    public void testCreationDate_IsSet() {
        assertNotNull(wallet.getCreationDate());
        assertTrue(wallet.getCreationDate().isBefore(LocalDateTime.now()));
    }

    @Test
    public void testValidAccountAndShabaNumber() {
        Wallet wallet = new Wallet();
        wallet.setAccountNumber("1234567890");
        wallet.setShabaNumber("IR1234567890123456789012"); // Corrected valid SHABA number

        User owner = new User();
        wallet.setOwner(String.valueOf(owner));

        Set<ConstraintViolation<Wallet>> violations = validator.validate(wallet);

        if (!violations.isEmpty()) {
            for (ConstraintViolation<Wallet> violation : violations) {
                System.out.println("Violation: " + violation.getPropertyPath() + " - " + violation.getMessage());
            }
        }

        assertTrue(violations.isEmpty(), "Expected no validation violations for valid account number, SHABA number, and owner");
    }
}
