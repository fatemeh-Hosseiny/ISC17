package com.example.model;

import com.example.model.domain.UserGender;
import com.example.model.domain.UserStatusForMan;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;


import java.time.LocalDate;
import java.time.Period;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //@Column(unique = true)
    private String fullName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    //@Column(unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserGender gender = UserGender.Female;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatusForMan userStatusForMan = UserStatusForMan.yes;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JoinColumn(name = "wallet_User",nullable = false)
    private Wallet wallet;

    public User() {
        super();
    }

    public User(String fullName, LocalDate dateOfBirth, String phoneNumber, String email, String password, UserStatusForMan userStatusForMan, UserGender gender ) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.userStatusForMan = userStatusForMan;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.password = password;
    }

    public User(String testuser, String password) {
        this.fullName = testuser;
        this.password = password;
    }

 /*   public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getfullName() {
        return fullName;
    }

    public void setfullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }*/

    public boolean isEligible() {
        return Period.between(this.dateOfBirth, LocalDate.now()).getYears() >= 18;
    }

    public void validateUser() {
        if (!isEligible()) {
            throw new IllegalArgumentException("User must be at least 18 years old.");
        }
    }

    public void setfullName(String fullName) {
         this.fullName = fullName;
    }

}
