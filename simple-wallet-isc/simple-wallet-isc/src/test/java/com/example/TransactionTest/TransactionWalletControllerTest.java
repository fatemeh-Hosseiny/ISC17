package com.example.TransactionTest;

import com.example.controller.TransactionWalletController;
import com.example.model.TransactionWallet;
import com.example.model.Wallet;
import com.example.service.TransactionWalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TransactionWalletControllerTest {
    @InjectMocks
    private TransactionWalletController transactionWalletController;

    @Mock
    private TransactionWalletService transactionWalletService;


    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionWalletController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void deposit_Success() throws Exception {
        Long walletId = 1L;
        BigDecimal amount = BigDecimal.valueOf(150000.00);
        TransactionWallet transaction = new TransactionWallet();
        transaction.setAmount(amount);
        transaction.setType(TransactionWallet.TransactionType.DEPOSIT);

        when(transactionWalletService.deposit(walletId, amount)).thenReturn(transaction);

        mockMvc.perform(post("/api/transactions/{walletId}/deposit", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(amount))
                .andExpect(jsonPath("$.type").value("DEPOSIT"));

        verify(transactionWalletService).deposit(walletId, amount);
    }

    @Test
    void deposit_InvalidAmount() throws Exception {
        Long walletId = 1L;
        BigDecimal amount = BigDecimal.valueOf(-1000.00); // Invalid amount

        // Perform the deposit request
        mockMvc.perform(post("/api/transactions/{walletId}/deposit", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amount)))
                .andExpect(status().isBadRequest()); // Expecting a 400 Bad Request

        // Ensure the service method was not called because of the invalid amount
        verify(transactionWalletService, never()).deposit(any(), any());
    }

    @Test
    void withdraw_Success() throws Exception {
        Long walletId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100000.00);
        TransactionWallet transaction = new TransactionWallet();
        transaction.setAmount(amount);
        transaction.setType(TransactionWallet.TransactionType.WITHDRAWAL);

        when(transactionWalletService.withdraw(walletId, amount)).thenReturn(transaction);

        mockMvc.perform(post("/api/transactions/{walletId}/withdraw", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(amount))
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"));

        verify(transactionWalletService).withdraw(walletId, amount);
    }

    @Test
    void withdraw_InsufficientFunds() throws Exception {
        Long walletId = 1L;
        BigDecimal amount = BigDecimal.valueOf(600000.00); // More than the current balance

        when(transactionWalletService.withdraw(walletId, amount)).thenThrow(new IllegalArgumentException("Insufficient funds for withdrawal"));

        mockMvc.perform(post("/api/transactions/{walletId}/withdraw", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amount)))
                .andExpect(status().isBadRequest());

        verify(transactionWalletService).withdraw(walletId, amount);
    }

    @Test
    void getWalletDetails_Success() throws Exception {
        Long walletId = 1L;
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(500000.00));

        when(transactionWalletService.getWalletDetails(walletId)).thenReturn(wallet);

        mockMvc.perform(get("/api/transactions/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId))
                .andExpect(jsonPath("$.balance").value(500000.00));

        verify(transactionWalletService).getWalletDetails(walletId);
    }

    @Test
    void getWalletDetails_NotFound() throws Exception {
        Long walletId = 999L;

        when(transactionWalletService.getWalletDetails(walletId)).thenThrow(new IllegalArgumentException("Wallet not found"));

        mockMvc.perform(get("/api/transactions/{walletId}", walletId))
                .andExpect(status().isNotFound());

        verify(transactionWalletService).getWalletDetails(walletId);
    }
}
