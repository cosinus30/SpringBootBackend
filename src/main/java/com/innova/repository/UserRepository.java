package com.innova.repository;

import com.innova.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
//TODO handle these methods
//    Boolean existByUsername(String username);
//
//    Boolean existByEmail(String email);
}
