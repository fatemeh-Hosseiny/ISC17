package com.example.userTest;

import com.example.model.User;
import com.example.model.domain.UserGender;
import com.example.model.domain.UserStatusForMan;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private Validator validator;
    private User user;

    @BeforeEach
    public void setUp() {
        // Initialize the validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Create a valid user for testing
        user = new User(
                "John Doe",
                LocalDate.of(1990, 1, 1),
                "1234567890",
                "john.doe@example.com",
                "password123",
                UserStatusForMan.yes,
                UserGender.Male
        );
    }

    @Test
    public void testUser_Valid() {
        // Validate the user
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Check if there are no violations
        assertTrue(violations.isEmpty(), "User should be valid");
    }

    @Test
    public void testEmail_NotValid() {
        // Set an invalid email
        user.setEmail("invalid-email");

        // Validate the user
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Expecting a violation for the email field
        assertFalse(violations.isEmpty(), "Email should be invalid");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Email should be valid")));
    }

    @Test
    public void testPassword_TooShort() {
        // Set a short password
        user.setPassword("abc");

        // Validate the user
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Expecting a violation for the password field
        assertFalse(violations.isEmpty(), "Password should be too short");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password must be at least 6 characters")));
    }

    @Test
    public void testDateOfBirth_NotInThePast() {
        // Set a future date of birth
        user.setDateOfBirth(LocalDate.now().plusDays(1));

        // Validate the user
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Expecting a violation for the date of birth field
        assertFalse(violations.isEmpty(), "Date of birth should be in the past");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Date of birth must be in the past")));
    }

    @Test
    public void testPhoneNumber_NotBlank() {
        // Set an empty phone number
        user.setPhoneNumber("");

        // Validate the user
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Expecting a violation for the phone number field
        assertFalse(violations.isEmpty(), "Phone number should not be blank");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Phone number is required")));
    }

    @Test
    public void testUser_IsEligible() {
        // Check if the user is eligible (age >= 18)
        assertTrue(user.isEligible(), "User should be eligible (age >= 18)");
    }

    @Test
    public void testUser_NotEligible_Under18() {
        // Set a date of birth that makes the user under 18
        user.setDateOfBirth(LocalDate.now().minusYears(17));

        // Check if the user is eligible
        assertFalse(user.isEligible(), "User should not be eligible (age < 18)");
    }

    @Test
    public void testSetFullName() {
        // Change the full name
        user.setfullName("Jane Doe");

        // Assert that the full name was updated
        assertEquals("Jane Doe", user.getFullName(), "User's full name should be updated to Jane Doe");
    }

    @Test
    public void testValidateUser_AgeRestriction() {
        // Set a date of birth that makes the user under 18
        user.setDateOfBirth(LocalDate.now().minusYears(17));

        // Assert that an exception is thrown when validateUser is called
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            user.validateUser();
        });
        assertEquals("User must be at least 18 years old.", exception.getMessage());
    }

    /*@Test
    public void testUserCreation_ValidData_ShouldCreateUser() {
        // Arrange
        String fullName = "John Doe";
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1); // 24 years old
        String email = "john.doe@example.com";
        String password = "securePassword";
        String phoneNumber = "1234567890";
        UserStatusForMan userStatusForMan = UserStatusForMan.yes;
        UserGender gender = UserGender.Male;

        // Act
        User user = new User(fullName, dateOfBirth, phoneNumber, email, password, userStatusForMan, gender);

        // Assert
        assertNotNull(user);
        assertEquals(fullName, user.getFullName());
        assertEquals(email, user.getEmail());
        assertTrue(user.isEligible());
    }

    @Test
    public void testUserCreation_AgeUnder18_ShouldThrowException() {
        // Arrange
        String fullName = "Jane Doe";
        LocalDate dateOfBirth = LocalDate.of(2010, 1, 1); // 13 years old
        String email = "jane.doe@example.com";
        String password = "securePassword";
        String phoneNumber = "0987654321";
        UserStatusForMan userStatusForMan = UserStatusForMan.yes;
        UserGender gender = UserGender.Female;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new User(fullName, dateOfBirth, phoneNumber, email, password, userStatusForMan, gender);
        });
        assertEquals("User must be at least 18 years old.", exception.getMessage());
    }

    @Test
    public void testUserCreation_ValidUserStatus_ShouldCreateUser() {
        // Arrange
        String fullName = "Alice Smith";
        LocalDate dateOfBirth = LocalDate.of(1995, 1, 1); // 29 years old
        String email = "alice.smith@example.com";
        String password = "securePassword";
        String phoneNumber = "1122334455";
        UserStatusForMan userStatusForMan = UserStatusForMan.yes;
        UserGender gender = UserGender.Female;

        // Act
        User user = new User(fullName, dateOfBirth, phoneNumber, email, password, userStatusForMan, gender);

        // Assert
        assertNotNull(user);
        assertEquals(userStatusForMan, user.getUserStatusForMan());
    }

    @Test
    public void testUserCreation_InvalidUserStatus_ShouldThrowException() {
        // Arrange
        String fullName = "Bob Johnson";
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1); // 24 years old
        String email = "bob.johnson@example.com";
        String password = "securePassword";
        String phoneNumber = "5566778899";
        UserStatusForMan userStatusForMan = UserStatusForMan.no; // Invalid status
        UserGender gender = UserGender.Male;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new User(fullName, dateOfBirth, phoneNumber, email, password, userStatusForMan, gender);
        });
        assertEquals("User must be at least 18 years old.", exception.getMessage());
    }*/
}
