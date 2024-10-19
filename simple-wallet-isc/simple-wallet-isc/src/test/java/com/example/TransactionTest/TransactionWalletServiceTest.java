package com.example.TransactionTest;

import com.example.model.TransactionWallet;
import com.example.model.Wallet;
import com.example.repository.TransactionWalletRepo;
import com.example.repository.WalletRepo;
import com.example.service.TransactionWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.mockito.Mockito.when;

public class TransactionWalletServiceTest {

    @InjectMocks
    private TransactionWalletService transactionWalletService;

    @Mock
    private TransactionWalletRepo transactionWalletRepo;

    @Mock
    private WalletRepo walletRepo;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setBalance(BigDecimal.valueOf(500000.00)); // Set initial balance for testing
    }

    @Test
    void deposit_Success() {
        // Arrange
        Long walletId = wallet.getId();
        BigDecimal amount = BigDecimal.valueOf(150000.00);

        when(walletRepo.findById(walletId)).thenReturn(Optional.of(wallet));

        // Act
        TransactionWallet transaction = transactionWalletService.deposit(walletId, amount);

        // Assert
        assertNotNull(transaction);
        assertEquals(wallet, transaction.getWallet());
        assertEquals(TransactionWallet.TransactionType.DEPOSIT, transaction.getType());
        assertEquals(amount, transaction.getAmount());
        assertEquals(wallet.getBalance(), BigDecimal.valueOf(650000.00)); // Check new balance

        // Verify repository interactions
        verify(transactionWalletRepo).save(transaction);
        verify(walletRepo).save(wallet);
    }

    @Test
    void deposit_InvalidAmount() {
        // Arrange
        Long walletId = wallet.getId();
        BigDecimal amount = BigDecimal.valueOf(-1000.00); // Invalid amount

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionWalletService.deposit(walletId, amount);
        });

        assertEquals("Amount must be positive", exception.getMessage());
        verify(transactionWalletRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }

    @Test
    void deposit_WalletNotFound() {
        // Arrange
        Long walletId = 999L; // Non-existing wallet ID
        BigDecimal amount = BigDecimal.valueOf(150000.00);

        when(walletRepo.findById(walletId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionWalletService.deposit(walletId, amount);
        });

        assertEquals("Wallet not found", exception.getMessage());
        verify(transactionWalletRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }

    @Test
    void withdraw_Success() {
        // Arrange
        Long walletId = wallet.getId();
        BigDecimal amount = BigDecimal.valueOf(100000.00);

        when(walletRepo.findById(walletId)).thenReturn(Optional.of(wallet));

        // Act
        TransactionWallet transaction = transactionWalletService.withdraw(walletId, amount);

        // Assert
        assertNotNull(transaction);
        assertEquals(wallet, transaction.getWallet());
        assertEquals(TransactionWallet.TransactionType.WITHDRAWAL, transaction.getType());
        assertEquals(amount, transaction.getAmount());
        assertEquals(wallet.getBalance(), BigDecimal.valueOf(400000.00)); // Check new balance

        // Verify repository interactions
        verify(transactionWalletRepo).save(transaction);
        verify(walletRepo).save(wallet);
    }

    @Test
    void withdraw_InsufficientFunds() {
        // Arrange
        Long walletId = wallet.getId();
        BigDecimal amount = BigDecimal.valueOf(600000.00); // More than the current balance

        when(walletRepo.findById(walletId)).thenReturn(Optional.of(wallet));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionWalletService.withdraw(walletId, amount);
        });

        assertEquals("Insufficient funds for withdrawal", exception.getMessage());
        verify(transactionWalletRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }

    @Test
    void withdraw_WalletNotFound() {
        // Arrange
        Long walletId = 999L; // Non-existing wallet ID
        BigDecimal amount = BigDecimal.valueOf(100000.00);

        when(walletRepo.findById(walletId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionWalletService.withdraw(walletId, amount);
        });

        assertEquals("Wallet not found", exception.getMessage());
        verify(transactionWalletRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }

    @Test
    void getWalletDetails_Success() {
        // Arrange
        Long walletId = wallet.getId();
        when(walletRepo.findById(walletId)).thenReturn(Optional.of(wallet));

        // Act
        Wallet foundWallet = transactionWalletService.getWalletDetails(walletId);

        // Assert
        assertNotNull(foundWallet);
        assertEquals(walletId, foundWallet.getId());

        // Verify repository interaction
        verify(walletRepo).findById(walletId);
    }

    @Test
    void getWalletDetails_WalletNotFound() {
        // Arrange
        Long walletId = 999L; // Non-existing wallet ID
        when(walletRepo.findById(walletId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionWalletService.getWalletDetails(walletId);
        });

        assertEquals("Wallet not found", exception.getMessage());
        verify(walletRepo).findById(walletId);
    }
}
