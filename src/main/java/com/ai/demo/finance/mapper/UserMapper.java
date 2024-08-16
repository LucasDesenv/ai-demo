package com.ai.demo.finance.mapper;

import com.ai.demo.finance.dto.UserDTO;
import com.ai.demo.finance.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    UserDTO toUserDTO(User user);

    User toUser(UserDTO userDTO);

    @Mapping(target = "id", ignore = true)
    User toUserToCreate(UserDTO userDTO);
}