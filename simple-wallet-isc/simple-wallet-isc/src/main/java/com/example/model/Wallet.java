package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private static final BigDecimal MINIMUM_BALANCE = BigDecimal.valueOf(10000);


    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{10}", message = "Account number must be exactly 10 digits")
    private String accountNumber;

    @NotBlank(message = "SHABA number is required")
    @Pattern(regexp = "[IR]\\d{22}", message = "SHABA number must start with two letters followed by 22 digits")
    private String shabaNumber;

    private BigDecimal balance;

    @NotNull
    private String owner;

    private LocalDateTime creationDate;

    @OneToOne
    private TransactionWallet transactionWallet;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Wallet() {
        this.creationDate = LocalDateTime.now(); // Set creation date to now
        this.balance = MINIMUM_BALANCE; // Initial balance is 10000
    }

    public Wallet(User user, String accountNumber, String shabaNumber) {
        this.user = user;
        this.accountNumber = accountNumber;
        this.shabaNumber = shabaNumber;
        this.creationDate = LocalDateTime.now();
        this.balance = MINIMUM_BALANCE;// Start with the minimum balance
    }
    public boolean login(String accountNumber, String shabaNumber) {
        return this.accountNumber.equals(accountNumber) && this.shabaNumber.equals(shabaNumber);
    }

    public void addFunds(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(amount);
        } else {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public void withdrawFunds(BigDecimal amount) {
        //BigDecimal minimumBalance = new BigDecimal("10000.00");

        if (this.balance.subtract(amount).compareTo(MINIMUM_BALANCE) < 0) {
            throw new IllegalArgumentException("Withdrawal would result in a balance below the minimum of 10,000");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        this.balance = this.balance.subtract(amount);
    }
    /*public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public User getUser() {return user;}

    public void setUser(User user) {this.user = user;}

    public String getAccountNumber() {return accountNumber;}

    public void setAccountNumber(String accountNumber) {this.accountNumber = accountNumber;}

    public String getShabaNumber() {return shabaNumber;}

    public void setShabaNumber(String shabaNumber) {this.shabaNumber = shabaNumber;}

    public BigDecimal getBalance() {return balance;}

    public void setBalance(BigDecimal balance) {this.balance = balance;}

    public LocalDateTime getCreationDate() {return creationDate;}*/


}
