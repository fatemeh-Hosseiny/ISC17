package com.example.userTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.controller.UserController;
import com.example.model.User;
import com.example.model.domain.UserGender;
import com.example.model.domain.UserStatusForMan;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        objectMapper = new ObjectMapper();
        // Register the JavaTimeModule to handle LocalDate serialization
        //objectMapper.registerModule(new JavaTimeModule());

        // Sample user for tests
        user = new User("John Doe", LocalDate.of(1990, 1, 1), "1234567890",
                "john.doe@example.com", "password", UserStatusForMan.yes, UserGender.Male);
        user.setId(1L);
    }

    @Test
    void registerUser_Success() throws Exception {
        when(userService.createUser(any(), any(), any(), any(), any(), any(), any())).thenReturn(user);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fullName", user.getFullName())
                        .param("dateOfBirth", user.getDateOfBirth().toString())
                        .param("phoneNumber", user.getPhoneNumber())
                        .param("email", user.getEmail())
                        .param("password", user.getPassword())
                        .param("userStatusForMan", user.getUserStatusForMan().toString())
                        .param("gender", user.getGender().toString()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fullName").value(user.getFullName()));
    }

    @Test
    void registerUser_BadRequest() throws Exception {
        when(userService.createUser(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Registration failed"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fullName", "")
                        .param("dateOfBirth", "")
                        .param("phoneNumber", "")
                        .param("email", "")
                        .param("password", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        when(userService.login(any(), any(), any())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", user.getEmail())
                        .param("password", user.getPassword())
                        .param("phoneNumber", user.getPhoneNumber()))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful!"));
    }

    @Test
    void login_Failed() throws Exception {
        when(userService.login(any(), any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", user.getEmail())
                        .param("password", "wrongpassword")
                        .param("phoneNumber", user.getPhoneNumber()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Login failed!"));
    }

    @Test
    void getUser_Success() throws Exception {
        when(userService.findUserById(anyLong())).thenReturn(user);

        mockMvc.perform(get("/api/users/getUser/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fullName").value(user.getFullName()));
    }

    @Test
    void getUser_NotFound() throws Exception {
        when(userService.findUserById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/users/getUser/{id}", 999L))
                .andExpect(status().isNotFound());
    }
    @Test
    void updateUser_Success() throws Exception {
        // Mocking the service layer to return the updated user
        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/updateUser/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value(user.getFullName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void updateUser_ThrowsUnexpectedError() throws Exception {
        // Mocking the service layer to throw a general exception (e.g., database issue)
        when(userService.updateUser(anyLong(), any(User.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/api/users/updateUser/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isInternalServerError());  // Expecting 500 Internal Server Error
    }

    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(anyLong());

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_NotFound() throws Exception {
        doThrow(new IllegalArgumentException("User not found")).when(userService).deleteUser(anyLong());

        mockMvc.perform(delete("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByFullName_Success() throws Exception {
        when(userService.findUserByFullName(any())).thenReturn(user);

        mockMvc.perform(get("/api/users/search")
                        .param("fullName", user.getFullName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value(user.getFullName()));
    }

    @Test
    void getUserByFullName_NotFound() throws Exception {
        when(userService.findUserByFullName(any())).thenReturn(null);

        mockMvc.perform(get("/api/users/search")
                        .param("fullName", "NotFoundUser"))
                .andExpect(status().isNotFound());
    }
}
