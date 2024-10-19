package com.example.service;

import com.example.exception.InsufficientFundsException;
import com.example.exception.WalletNotFoundException;
import com.example.model.TransactionWallet;
import com.example.model.Wallet;
import com.example.repository.TransactionWalletRepo;
import com.example.repository.WalletRepo;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@Service
public class TransactionWalletService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionWalletService.class);

    @Autowired
    private TransactionWalletRepo transactionWalletRepo;

    @Autowired
    private WalletRepo walletRepo;

    @Transactional
    public TransactionWallet deposit(@PathVariable Long walletId,
                                     @Valid @RequestBody BigDecimal amount) {
        validateAmount(amount);
        Wallet wallet = walletRepo.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        TransactionWallet transaction = new TransactionWallet(wallet, TransactionWallet.TransactionType.DEPOSIT, amount);
        wallet.addFunds(amount);
        transactionWalletRepo.save(transaction);
        walletRepo.save(wallet);
        return transaction;
    }

    @Transactional
    public TransactionWallet withdraw(@PathVariable Long walletId,
                                      @Valid @RequestBody BigDecimal amount) {
        validateAmount(amount);
        Wallet wallet = walletRepo.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal");
        }
        TransactionWallet transaction = new TransactionWallet(wallet, TransactionWallet.TransactionType.WITHDRAWAL, amount);
        wallet.withdrawFunds(amount);
        transactionWalletRepo.save(transaction);
        walletRepo.save(wallet);
        logger.info("Withdrew {} from wallet ID: {}. New balance: {}", amount, walletId, wallet.getBalance());
        return transaction;
    }

    private void validateAmount(@Valid @RequestBody BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    private Wallet getWalletById(Long walletId) {
        return walletRepo.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    private void ensureSufficientFunds(Wallet wallet, @Valid @RequestBody BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }
    }

    private TransactionWallet createTransaction(Wallet wallet, TransactionWallet.TransactionType type, @Valid @RequestBody BigDecimal amount) {
        TransactionWallet transaction = new TransactionWallet(wallet, type, amount);
        transactionWalletRepo.save(transaction); // Save the transaction
        return transaction;
    }

    private void updateWalletBalance(Wallet wallet, @Valid @RequestBody BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            wallet.addFunds(amount);
        } else {
            wallet.withdrawFunds(amount.negate());
        }
        walletRepo.save(wallet); // Save the updated wallet
    }
    public Wallet getWalletDetails(Long walletId) {
        return walletRepo.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
    }
}
