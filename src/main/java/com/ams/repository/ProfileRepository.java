package com.ams.repository;

import com.ams.entity.Profile;
import com.ams.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile,Long> {
    Optional<Profile> findByUser(User user);
}
