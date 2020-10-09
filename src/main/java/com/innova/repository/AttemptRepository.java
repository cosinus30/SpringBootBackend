package com.innova.repository;

import com.innova.model.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, String> {
    Boolean existsByIp(String username);
}
