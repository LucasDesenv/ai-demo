package com.ai.demo.travel.mapper;

import com.ai.demo.travel.dto.UserProfileDTO;
import com.ai.demo.travel.model.UserInterest;
import com.ai.demo.travel.model.UserProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserProfileMapper {

    @Mapping(target = "id", source = "userProfileId")
    @Mapping(target = "interests", expression = "java(mapInterests(dto.getInterests()))")
    UserProfile toUserProfile(UserProfileDTO dto, Long userProfileId);

    @Mapping(source = "interests", target = "interests")
    UserProfileDTO toUserProfileDTO(UserProfile userProfile);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "interests", target = "interests")
    UserProfile toUserProfileToCreate(UserProfileDTO dto);

    default List<UserInterest> mapInterests(List<String> interests) {
        if (interests == null)
            return new ArrayList<>();
        return interests.stream().map(interest -> UserInterest.builder().interest(interest).build()).collect(Collectors.toList());
    }

    default List<String> mapInterestsToString(List<UserInterest> interests) {
        if (interests == null)
            return new ArrayList<>();
        return interests.stream()
                .map(UserInterest::getInterest)
                .collect(Collectors.toList());
    }
}
