package com.example.WalleTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import com.example.exception.WalletNotFoundException;
import com.example.model.User;
import com.example.model.Wallet;
import com.example.repository.WalletRepo;
import com.example.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class WalletServiceTest {

    @InjectMocks
    private WalletService walletService;

    @Mock
    private WalletRepo walletRepo;

    private User user;
    private Wallet wallet;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test data
        user = new User();
        user.setId(1L);
        user.setfullName("John Doe");

        wallet = new Wallet(user, "1234567890", "IR1234567890123456789012");
    }

    @Test
    public void testCreateWallet_Success() {
        // Arrange
        when(walletRepo.save(any(Wallet.class))).thenReturn(wallet);

        // Act
        Wallet createdWallet = walletService.createWallet(user, "1234567890", "IR1234567890123456789012");

        // Assert
        assertNotNull(createdWallet);
        assertEquals("1234567890", createdWallet.getAccountNumber());
        assertEquals("IR1234567890123456789012", createdWallet.getShabaNumber());
        assertEquals(BigDecimal.valueOf(10000), createdWallet.getBalance());

        verify(walletRepo, times(1)).save(any(Wallet.class));
    }

    @Test
    public void testLogin_Success() {
        // Arrange
        when(walletRepo.findById(1L)).thenReturn(Optional.of(wallet));

        // Act
        boolean result = walletService.login(1L, "1234567890", "IR1234567890123456789012");

        // Assert
        assertTrue(result);
        verify(walletRepo, times(1)).findById(1L);
    }

    @Test
    public void testLogin_Failure_InvalidCredentials() {
        // Arrange
        when(walletRepo.findById(1L)).thenReturn(Optional.of(wallet));

        // Act
        boolean result = walletService.login(1L, "wrongAccount", "wrongShaba");

        // Assert
        assertFalse(result);
        verify(walletRepo, times(1)).findById(1L);
    }

    @Test
    public void testGetBalance_Success() {
        // Arrange
        when(walletRepo.findById(1L)).thenReturn(Optional.of(wallet));

        // Act
        BigDecimal balance = walletService.getBalance(1L);

        // Assert
        assertNotNull(balance);
        assertEquals(BigDecimal.valueOf(10000), balance);
        verify(walletRepo, times(1)).findById(1L);
    }

    @Test
    public void testGetBalance_WalletNotFound() {
        // Arrange
        when(walletRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            walletService.getBalance(1L);
        });
        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    public void testAddFunds_Success() {
        // Arrange
        when(walletRepo.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepo.save(any(Wallet.class))).thenReturn(wallet);

        // Act
        Wallet updatedWallet = walletService.addFunds(1L, BigDecimal.valueOf(5000));

        // Assert
        assertNotNull(updatedWallet);
        assertEquals(BigDecimal.valueOf(15000), updatedWallet.getBalance());
        verify(walletRepo, times(1)).findById(1L);
        verify(walletRepo, times(1)).save(any(Wallet.class));
    }

    @Test
    public void testAddFunds_WalletNotFound() {
        // Arrange
        when(walletRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(WalletNotFoundException.class, () -> {
            walletService.addFunds(1L, BigDecimal.valueOf(5000));
        });
        assertEquals("Wallet not found", exception.getMessage());
    }
    @Test
    public void testWithdrawFunds_Success() {
        // Arrange
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setBalance(BigDecimal.valueOf(12000)); // Initial balance of 12,000

        when(walletRepo.findById(1L)).thenReturn(Optional.of(wallet)); // Mock wallet lookup
        when(walletRepo.save(any(Wallet.class))).thenReturn(wallet); // Mock wallet save

        // Act
        Wallet updatedWallet = walletService.withdrawFunds(1L, BigDecimal.valueOf(2000)); // Withdraw 2000

        // Assert
        assertNotNull(updatedWallet);
        assertEquals(BigDecimal.valueOf(10000), updatedWallet.getBalance()); // New balance should be 10,000
        verify(walletRepo, times(1)).findById(1L); // Ensure findById was called once
        verify(walletRepo, times(1)).save(any(Wallet.class)); // Ensure save was called once
    }


    @Test
    public void testWithdrawFunds_ExceedsLimit() {
        // Arrange
        when(walletRepo.findById(1L)).thenReturn(Optional.of(wallet));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            walletService.withdrawFunds(1L, BigDecimal.valueOf(11000));
        });
        assertEquals("Withdrawal would result in a balance below the minimum of 10,000", exception.getMessage());
    }
    @Test
    public void testWithdrawFunds_ExceedsLimit2() {
        // Arrange
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setBalance(BigDecimal.valueOf(10500)); // Setting balance to 10,500

        when(walletRepo.findById(1L)).thenReturn(Optional.of(wallet)); // Mock wallet lookup

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            walletService.withdrawFunds(1L, BigDecimal.valueOf(11000)); // Attempt to withdraw 11,000
        });

        // Assert that the correct exception message is thrown
        assertEquals("Withdrawal would result in a balance below the minimum of 10,000", exception.getMessage());

        // Optionally verify that save is not called, as the withdrawal should fail
        verify(walletRepo, never()).save(any(Wallet.class));
    }

    @Test
    public void testWithdrawFunds_WalletNotFound() {
        // Arrange
        when(walletRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(WalletNotFoundException.class, () -> {
            walletService.withdrawFunds(1L, BigDecimal.valueOf(5000));
        });
        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    public void testGetWalletByUserId_Success() {
        // Arrange
        when(walletRepo.findByUserId(1L)).thenReturn(wallet);

        // Act
        Optional<Wallet> foundWallet = walletService.getWalletByUserId(1L);

        // Assert
        assertTrue(foundWallet.isPresent());
        assertEquals(wallet, foundWallet.get());
        verify(walletRepo, times(1)).findByUserId(1L);
    }

    @Test
    public void testGetWalletByUserId_NotFound() {
        // Arrange
        when(walletRepo.findByUserId(1L)).thenReturn(null);

        // Act
        Optional<Wallet> foundWallet = walletService.getWalletByUserId(1L);

        // Assert
        assertFalse(foundWallet.isPresent());
        verify(walletRepo, times(1)).findByUserId(1L);
    }
}

