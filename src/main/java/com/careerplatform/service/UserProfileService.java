
package com.careerplatform.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.careerplatform.entity.UserProfile;
import com.careerplatform.repository.UserProfileRepository;

@Service
public class UserProfileService {

    private final UserProfileRepository profileRepository;

    public UserProfileService(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<UserProfile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public UserProfile getProfileById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Profile not found: " + id));
    }

    public UserProfile saveProfile(UserProfile profile) {
        return profileRepository.save(profile);
    }

    public void deleteProfile(Long id) {
        if (!profileRepository.existsById(id)) {
            throw new RuntimeException("Profile not found: " + id);
        }

        profileRepository.deleteById(id);
    }
}