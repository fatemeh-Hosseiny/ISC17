package com.example.userTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.model.User;
import com.example.model.domain.UserGender;
import com.example.model.domain.UserStatusForMan;
import com.example.repository.UserRepo;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)

public class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserService userService;

    private BCryptPasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.initMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();

        // Create a user for testing
        user = new User("John Doe", LocalDate.of(1990, 1, 1), "1234567890",
                "john.doe@example.com", "password123", UserStatusForMan.yes, UserGender.Male);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    @Test
    public void testCreateUser_Success() {
        // Given
        when(userRepo.save(any(User.class))).thenReturn(user);

        // When
        User createdUser = userService.createUser("John Doe", LocalDate.of(1990, 1, 1), "1234567890",
                "john.doe@example.com", "password123", UserStatusForMan.yes, UserGender.Male);

        // Then
        assertNotNull(createdUser);
        assertEquals("John Doe", createdUser.getFullName());
        assertEquals("john.doe@example.com", createdUser.getEmail());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    public void testCreateUser_InvalidStatus() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser("John Doe", LocalDate.of(1990, 1, 1), "1234567890",
                    "john.doe@example.com", "password123", UserStatusForMan.no, UserGender.Male);
        });
        assertEquals("User status must be 'yes'.", exception.getMessage());
    }

    @Test
    public void testCreateUser_Underage() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser("John Doe", LocalDate.now().minusYears(17), "1234567890",
                    "john.doe@example.com", "password123", UserStatusForMan.yes, UserGender.Male);
        });
        assertEquals("User must be at least 18 years old.", exception.getMessage());
    }

    @Test
    public void testLogin_Success() {
        // Given
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));

        // When
        Optional<User> loggedInUser = userService.login("john.doe@example.com", "password123", "1234567890");

        // Then
        assertTrue(loggedInUser.isPresent());
        assertEquals("john.doe@example.com", loggedInUser.get().getEmail());
        verify(userRepo, times(1)).findByEmail(anyString());
    }

    @Test
    public void testLogin_Failure_WrongPassword() {
        // Given
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));

        // When
        Optional<User> loggedInUser = userService.login("john.doe@example.com", "wrongpassword", "1234567890");

        // Then
        assertFalse(loggedInUser.isPresent());
        verify(userRepo, times(1)).findByEmail(anyString());
    }

    @Test
    public void testLogin_Failure_UserNotFound() {
        // Given
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        // When
        Optional<User> loggedInUser = userService.login("nonexistent@example.com", "password123", "1234567890");

        // Then
        assertFalse(loggedInUser.isPresent());
        verify(userRepo, times(1)).findByEmail(anyString());
    }

    @Test
    public void testUpdateUser_Success() {
        // Given
        when(userRepo.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepo.save(any(User.class))).thenReturn(user);

        User updatedData = new User("Jane Doe", LocalDate.of(1992, 2, 2), "0987654321",
                "jane.doe@example.com", "password123", UserStatusForMan.yes, UserGender.Female);

        // When
        User updatedUser = userService.updateUser(1L, updatedData);

        // Then
        assertNotNull(updatedUser);
        assertEquals("Jane Doe", updatedUser.getFullName());
        assertEquals("jane.doe@example.com", updatedUser.getEmail());
        verify(userRepo, times(1)).findById(anyLong());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateUser_Failure_UserNotFound() {
        // Given
        when(userRepo.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(1L, user);
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testDeleteUser_Success() {
        // Given
        when(userRepo.existsById(anyLong())).thenReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepo, times(1)).existsById(anyLong());
        verify(userRepo, times(1)).deleteById(anyLong());
    }

    @Test
    public void testDeleteUser_Failure_UserNotFound() {
        // Given
        when(userRepo.existsById(anyLong())).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(1L);
        });
        assertEquals("User not found", exception.getMessage());
    }

    /*@Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("John Doe", LocalDate.of(2000, 1, 1), "1234567890", "john.doe@example.com", "password123", UserStatusForMan.yes, UserGender.Male);
    }

    @Test
    public void testCreateUser_Success() {
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        User createdUser = userService.createUser("John Doe", LocalDate.of(2000, 1, 1), "1234567890", "john.doe@example.com", "password123", UserStatusForMan.yes, UserGender.Male);

        assertNotNull(createdUser);
        assertEquals("john.doe@example.com", createdUser.getEmail());
        assertNotNull(createdUser.getWallet());
        assertEquals(BigDecimal.valueOf(10000), createdUser.getWallet().getBalance());
    }

    @Test
    public void testCreateUser_AgeNotEligible() {
        User underageUser = new User("John Doe", LocalDate.of(2010, 1, 1), "1234567890", "john.doe@example.com", "password123", UserStatusForMan.yes, UserGender.Male);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(underageUser.getFullName(), underageUser.getDateOfBirth(), underageUser.getPhoneNumber(), underageUser.getEmail(), underageUser.getPassword(), underageUser.getUserStatusForMan(), underageUser.getGender());
        });

        assertEquals("User must be at least 18 years old.", exception.getMessage());
    }

    @Test
    public void testCreateUser_InvalidUserStatus() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser("John Doe", LocalDate.of(2000, 1, 1), "1234567890", "john.doe@example.com", "password123", UserStatusForMan.no, UserGender.Male);
        });

        assertEquals("User status must be 'yes'.", exception.getMessage());
    }

    @Test
    public void testLogin_Success() {
        when(userRepo.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        Optional<User> loggedInUser = userService.login("john.doe@example.com", "password123", "1234567890");

        assertTrue(loggedInUser.isPresent());
        assertEquals("john.doe@example.com", loggedInUser.get().getEmail());
    }

    @Test
    public void testLogin_Failure_IncorrectPassword() {
        when(userRepo.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        Optional<User> loggedInUser = userService.login("john.doe@example.com", "wrongpassword", "1234567890");

        assertFalse(loggedInUser.isPresent());
    }

    @Test
    public void testFindUserByFullName_Success() {
        when(userRepo.findByfullName("John Doe")).thenReturn(Optional.of(user));

        User foundUser = userService.findUserByFullName("John Doe");

        assertNotNull(foundUser);
        assertEquals("john.doe@example.com", foundUser.getEmail());
    }

    @Test
    public void testFindUserByFullName_NotFound() {
        when(userRepo.findByfullName("Nonexistent User")).thenReturn(Optional.empty());

        User foundUser = userService.findUserByFullName("Nonexistent User");

        assertNull(foundUser);
    }

    @Test
    public void testFindUserById_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.findUserById(1L);

        assertNotNull(foundUser);
        assertEquals("john.doe@example.com", foundUser.getEmail());
    }

    @Test
    public void testFindUserById_NotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        User foundUser = userService.findUserById(1L);

        assertNull(foundUser);
    }

    @Test
    public void testUpdateUser_Success() {
        when(userRepo.existsById(1L)).thenReturn(true);
        when(userRepo.save(any(User.class))).thenReturn(user);
        User updatedUser = new User("Jane Doe", LocalDate.of(1995, 5, 5), "0987654321", "jane.doe@example.com", "newpassword", UserStatusForMan.yes, UserGender.Female);

        User result = userService.updateUser(1L, updatedUser);

        assertEquals("Jane Doe", result.getFullName());
        assertEquals("jane.doe@example.com", result.getEmail());
    }

    @Test
    public void testUpdateUser_NotFound() {
        when(userRepo.existsById(1L)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(1L, user);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testDeleteUser_Success() {
        when(userRepo.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepo, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteUser_NotFound() {
        when(userRepo.existsById(1L)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(1L);
        });

        assertEquals("User not found", exception.getMessage());
    }*/
}
