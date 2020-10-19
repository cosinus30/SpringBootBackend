package com.innova.repository;

import com.innova.model.Template;
import com.innova.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist,String >{
    Boolean existByToken(String token);
}
