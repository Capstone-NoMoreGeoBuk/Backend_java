package com.back.domain.user.repository;

import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByOauthId(String oauthId);
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    boolean existsByNicknameAndIdNot(String nickname, UUID id);
}

