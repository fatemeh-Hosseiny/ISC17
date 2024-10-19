package com.example;

import com.example.model.User;
import com.example.model.Wallet;
import com.example.repository.UserRepo;
import com.example.repository.WalletRepo;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UserService userService; // Ensure you have this service implemented
    @Autowired
    private WalletRepo walletRepo; // Ensure this is implemented as well
    @Autowired
    private UserRepo userRepo;
    @Override
    public void run(String... args) throws Exception {
        // Add initial users and wallets here
        // Example:

        User user = new User("testuser", "password");
        userRepo.save(user);
        Wallet wallet = new Wallet(user, "123456789", "IR123456789012345678901234");
        walletRepo.save(wallet);
    }
}
