package com.org.booklibrary.service;

import com.org.booklibrary.entity.User;
import com.org.booklibrary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User("john_doe", "john.doe@example.com");
        user1.setId(1L);
        user2 = new User("jane_smith", "jane.smith@example.com");
        user2.setId(2L);
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        List<User> users = userService.getAllUsers();
        assertNotNull(users);
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserByIdFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        User foundUser = userService.getUserById(1L);
        assertNotNull(foundUser);
        assertEquals(user1.getUsername(), foundUser.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testAddUser() {
        User newUser = new User("new_user", "new.user@example.com");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        User addedUser = userService.addUser(newUser);
        assertNotNull(addedUser);
        assertEquals("new_user", addedUser.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
