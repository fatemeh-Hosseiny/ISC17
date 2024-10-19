package com.example.service;

import com.example.exception.WalletNotFoundException;
import com.example.model.User;
import com.example.model.Wallet;
import com.example.repository.WalletRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class WalletService {
    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);


    @Autowired
    private WalletRepo walletRepo;

    @Transactional
    public Wallet createWallet(User user, String accountNumber, String shabaNumber) {
        validateAccountDetails(accountNumber, shabaNumber);
        Wallet wallet = new Wallet(user, accountNumber, shabaNumber);
        logger.info("Creating wallet for user: {}", user.getId());
        return walletRepo.save(wallet);
    }

    public boolean login(Long walletId, String accountNumber, String shabaNumber) {
        Wallet wallet = walletRepo.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        boolean success = wallet.login(accountNumber, shabaNumber);
        logger.info("Login attempt for wallet ID: {} was {}", walletId, success ? "successful" : "unsuccessful");
        return success;
        //return wallet.login(accountNumber, shabaNumber);
    }

    public BigDecimal getBalance(Long walletId) {
        return walletRepo.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
    }

    @Transactional
    public Wallet addFunds(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepo.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        wallet.addFunds(amount);
        logger.info("Added funds to wallet ID: {}", walletId);
        return walletRepo.save(wallet);
    }

    private void validateAccountDetails(String accountNumber, String shabaNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }
        if (shabaNumber == null || shabaNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("SHABA number cannot be null or empty");
        }
    }
    public Optional<Wallet> getWalletByUserId(Long userId) {
        return Optional.ofNullable(walletRepo.findByUserId(userId));
    }
    @Transactional
    public Wallet withdrawFunds(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepo.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));


        wallet.withdrawFunds(amount);
        logger.info("Withdrew funds from wallet ID: {}", walletId);
        return walletRepo.save(wallet);
    }
}
