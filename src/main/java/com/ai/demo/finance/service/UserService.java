package com.ai.demo.finance.service;

import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.finance.mapper.UserMapper;
import com.ai.demo.finance.model.User;
import com.ai.demo.finance.model.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private static final UserMapper MAPPER = Mappers.getMapper(UserMapper.class);
    private final UserRepository userRepository;

    public UserDTO createUser(UserDTO userDTO) {
        User entity = MAPPER.toUserToCreate(userDTO);
        return MAPPER.toUserDTO(userRepository.save(entity));
    }

    public UserDTO findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(MAPPER::toUserDTO)
                .orElseThrow(() -> new NotFoundResourceException("User not found"));
    }

    public UserDTO findById(Long id) {
        return userRepository.findById(id)
                .map(MAPPER::toUserDTO)
                .orElseThrow(() -> new NotFoundResourceException("User not found"));
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        if (userRepository.existsById(id)) {
            User user = MAPPER.toUser(userDTO);
            User saved = userRepository.save(user);
            return MAPPER.toUserDTO(saved);
        }

        throw new NotFoundResourceException("User not found with id " + id);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundResourceException("User not found with id " + id);
        }

        userRepository.deleteById(id);
    }
}
