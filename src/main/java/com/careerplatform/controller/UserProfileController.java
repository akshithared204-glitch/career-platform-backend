
package com.careerplatform.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.careerplatform.entity.UserProfile;
import com.careerplatform.service.UserProfileService;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "http://localhost:5174")
public class UserProfileController {

    private final UserProfileService profileService;

    public UserProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public List<UserProfile> getAllProfiles() {
        return profileService.getAllProfiles();
    }

    @GetMapping("/{id}")
    public UserProfile getProfileById(@PathVariable Long id) {
        return profileService.getProfileById(id);
    }

    @PostMapping
    public UserProfile createProfile(@RequestBody UserProfile profile) {
        return profileService.saveProfile(profile);
    }

    @PutMapping("/{id}")
    public UserProfile updateProfile(
            @PathVariable Long id,
            @RequestBody UserProfile profile) {

        UserProfile existing = profileService.getProfileById(id);

        existing.setFullName(profile.getFullName());
        existing.setEmail(profile.getEmail());
        existing.setEducation(profile.getEducation());
        existing.setSkills(profile.getSkills());
        existing.setProjects(profile.getProjects());
        existing.setExperience(profile.getExperience());
        existing.setCareerInterest(profile.getCareerInterest());

        return profileService.saveProfile(existing);
    }

    @DeleteMapping("/{id}")
    public String deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return "Profile deleted successfully";
    }
}
