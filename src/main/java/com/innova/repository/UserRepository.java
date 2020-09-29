package com.innova.repository;

import com.innova.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Boolean existByUsername(String username);

    Boolean existByEmail(String email);
}
