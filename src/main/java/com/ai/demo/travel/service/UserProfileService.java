package com.ai.demo.travel.service;

import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.mapper.UserProfileMapper;
import com.ai.demo.travel.model.UserProfile;
import com.ai.demo.travel.model.repository.UserProfileRepository;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private static final UserProfileMapper USER_MAPPER = Mappers.getMapper(UserProfileMapper.class);
    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfileDTO createUserProfile(UserProfileDTO dto) {
        UserProfile user = USER_MAPPER.toUserProfileToCreate(dto);
        user.getInterests().forEach(interest -> interest.setUser(user));

        return USER_MAPPER.toUserProfileDTO(userProfileRepository.save(user));
    }

    public UserProfileDTO findById(Long id) {
        return USER_MAPPER.toUserProfileDTO(findEntityById(id));
    }

    private UserProfile findEntityById(Long id) {
        return userProfileRepository.findById(id).orElseThrow(() -> new NotFoundResourceException("UserProfile not found"));
    }

    @Transactional
    public UserProfileDTO updateUserProfile(Long id, UserProfileDTO dto) {
        UserProfile existing = findEntityById(id);

        UserProfile user = USER_MAPPER.toUserProfile(dto, existing.getId());
        user.getInterests().forEach(interest -> interest.setUser(user));
        return USER_MAPPER.toUserProfileDTO(userProfileRepository.save(user));
    }
}
