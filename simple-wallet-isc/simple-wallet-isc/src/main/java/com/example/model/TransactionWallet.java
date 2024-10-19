package com.example.model;

import com.example.model.domain.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "transactionWallet")
public class TransactionWallet {

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100000.00", message = "Amount must be at least 100,000")
    @DecimalMax(value = "10000000.00", message = "Amount must not exceed 10,000,000")
    private BigDecimal amount;

    private LocalDateTime transactionDate;

    @NotNull(message = "Wallet must not be null")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_id",nullable = false)
    private Wallet wallet;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    public TransactionWallet() {
        this.transactionDate = LocalDateTime.now();
    }
    public TransactionWallet(Wallet wallet, TransactionType type, BigDecimal amount) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.transactionDate = LocalDateTime.now();
    }

    /*public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public Wallet getWallet() {return wallet;}

    public void setWallet(Wallet wallet) {this.wallet = wallet;}

    public TransactionType getType() {return type;}

    public void setType(TransactionType type) {this.type = type;}

    public BigDecimal getAmount() {return amount;}

    public void setAmount(BigDecimal amount) {this.amount = amount;}

    public LocalDateTime getTransactionDate() {return transactionDate;}*/

}
