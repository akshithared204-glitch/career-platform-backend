
package com.careerplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.careerplatform.entity.UserProfile;

public interface UserProfileRepository
        extends JpaRepository<UserProfile, Long> {

    boolean existsByEmail(String email);
}
