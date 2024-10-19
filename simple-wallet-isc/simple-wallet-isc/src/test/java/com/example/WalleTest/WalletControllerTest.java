package com.example.WalleTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import com.example.service.UserService;
import com.example.controller.WalletController;
import com.example.model.User;
import com.example.model.Wallet;
import com.example.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WalletControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(walletController).build();
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateWallet_Success() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setId(1L);

        when(walletService.createWallet(any(User.class), anyString(), anyString())).thenReturn(wallet);

        mockMvc.perform(post("/api/wallets/1")
                        .param("accountNumber", "1234567890")
                        .param("shabaNumber", "IR123456789012345678901"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(wallet.getId()));

        verify(walletService, times(1)).createWallet(any(User.class), eq("1234567890"), eq("IR123456789012345678901"));
    }
    @Test
    public void testfoundUser_UserNotFound() throws Exception {
        when(userService.findUserById(1L)).thenReturn(null);

        mockMvc.perform(post("/api/wallets/{userId}/foundUser", 1L)
                        .param("accountNumber", "1234567890")
                        .param("shabaNumber", "IR123456789012345678901"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found for userId: 1"));
    }

    @Test
    public void testfoundUser_UserFound() throws Exception {
        User user = new User();
        user.setId(1L);
        when(userService.findUserById(1L)).thenReturn(user);

        mockMvc.perform(post("/api/wallets/{userId}/foundUser", 1L)
                        .param("accountNumber", "1234567890")
                        .param("shabaNumber", "IR123456789012345678901"))
                .andExpect(status().isOk())
                .andExpect(content().string("User found with id: 1"));
    }

    @Test
    public void testLogin_Success() throws Exception {
        when(walletService.login(anyLong(), anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/wallets/login")
                        .param("walletId", "1")
                        .param("accountNumber", "123456789")
                        .param("shabaNumber", "IR123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful!"));

        verify(walletService, times(1)).login(1L, "123456789", "IR123456789");
    }

    @Test
    public void testLogin_Failure() throws Exception {
        when(walletService.login(anyLong(), anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/wallets/login")
                        .param("walletId", "1")
                        .param("accountNumber", "123456789")
                        .param("shabaNumber", "IR123456789"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Login failed!"));

        verify(walletService, times(1)).login(1L, "123456789", "IR123456789");
    }

    @Test
    public void testGetBalance_Success() throws Exception {
        when(walletService.getBalance(anyLong())).thenReturn(BigDecimal.valueOf(100));

        mockMvc.perform(get("/api/wallets/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(100));

        verify(walletService, times(1)).getBalance(1L);
    }

    @Test
    public void testGetBalance_WalletNotFound() throws Exception {
        when(walletService.getBalance(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/wallets/999/balance"))
                .andExpect(status().isNotFound());

        verify(walletService, times(1)).getBalance(999L);
    }

    @Test
    public void testAddFunds_Success() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setBalance(BigDecimal.valueOf(200));

        when(walletService.addFunds(anyLong(), any(BigDecimal.class))).thenReturn(wallet);

        mockMvc.perform(post("/api/wallets/1/addFunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(BigDecimal.valueOf(100))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200));

        verify(walletService, times(1)).addFunds(1L, BigDecimal.valueOf(100));
    }

    @Test
    public void testAddFunds_InvalidAmount() throws Exception {
        mockMvc.perform(post("/api/wallets/1/addFunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(BigDecimal.valueOf(-100))))
                .andExpect(status().isBadRequest());

        verify(walletService, times(0)).addFunds(anyLong(), any(BigDecimal.class));
    }

    @Test
    public void testWithdrawFunds_Success() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setBalance(BigDecimal.valueOf(50));

        when(walletService.withdrawFunds(anyLong(), any(BigDecimal.class))).thenReturn(wallet);

        mockMvc.perform(post("/api/wallets/1/withdrawFunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(BigDecimal.valueOf(50))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50));

        verify(walletService, times(1)).withdrawFunds(1L, BigDecimal.valueOf(50));
    }

    @Test
    public void testWithdrawFunds_InvalidAmount() throws Exception {
        mockMvc.perform(post("/api/wallets/1/withdrawFunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(BigDecimal.valueOf(-50))))
                .andExpect(status().isBadRequest());

        verify(walletService, times(0)).withdrawFunds(anyLong(), any(BigDecimal.class));
    }

    @Test
    public void testWithdrawFunds_InsufficientFunds() throws Exception {
        when(walletService.withdrawFunds(anyLong(), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Insufficient funds."));

        mockMvc.perform(post("/api/wallets/1/withdrawFunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(BigDecimal.valueOf(200))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient funds."));

        verify(walletService, times(1)).withdrawFunds(1L, BigDecimal.valueOf(200));
    }
}
