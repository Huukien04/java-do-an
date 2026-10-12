package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.User;
import com.hcmute.topicmanagement.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByEnabledTrueOrderByFullNameAsc();
    List<User> findByEnabledTrueAndRole_Name(RoleName roleName);
}
