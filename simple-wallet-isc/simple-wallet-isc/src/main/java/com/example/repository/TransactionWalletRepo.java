package com.example.repository;

import com.example.model.TransactionWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionWalletRepo extends JpaRepository<TransactionWallet, Long> {
}
