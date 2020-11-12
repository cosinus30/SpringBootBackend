package com.innova.repository;

import com.innova.model.View;
import com.innova.model.ViewKey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewRepository extends JpaRepository<View, ViewKey>{
    
}
