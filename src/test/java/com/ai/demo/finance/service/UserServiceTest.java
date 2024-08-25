package com.ai.demo.finance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.finance.model.User;
import com.ai.demo.finance.model.enums.Country;
import com.ai.demo.finance.model.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        userDTO = new UserDTO(1L, "testuser", Country.BR);
    }

    @Test
    void testCreateUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO createdUser = userService.createUser(userDTO);

        assertNotNull(createdUser);
        assertEquals(user.getId(), createdUser.id());
        assertEquals(user.getUsername(), createdUser.username());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testFindByUsername_UserExists() {
        when(userRepository.findByUsername("lucas")).thenReturn(Optional.of(user));

        UserDTO foundUser = userService.findByUsername("lucas");

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.id());
        assertEquals(user.getUsername(), foundUser.username());
        verify(userRepository, times(1)).findByUsername("lucas");
    }

    @Test
    void testFindByUsername_UserDoesNotExist() {
        when(userRepository.findByUsername("lucas")).thenReturn(Optional.empty());

        assertThrows(NotFoundResourceException.class, () -> userService.findByUsername("lucas"));
        verify(userRepository, times(1)).findByUsername("lucas");
    }

    @Test
    void testUpdateUser_UserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO updatedUser = userService.updateUser(1L, userDTO);

        assertNotNull(updatedUser);
        assertEquals(user.getId(), updatedUser.id());
        assertEquals(user.getUsername(), updatedUser.username());
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_UserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundResourceException.class, () -> userService.updateUser(1L, userDTO));
        verify(userRepository, times(1)).existsById(1L);
    }

    @Test
    void testDeleteUser_UserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_UserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundResourceException.class, () -> userService.deleteUser(1L));
        verify(userRepository, times(1)).existsById(1L);
    }
}