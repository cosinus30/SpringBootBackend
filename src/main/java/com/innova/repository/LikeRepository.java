package com.innova.repository;

import com.innova.model.Like;
import com.innova.model.LikeKey;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, LikeKey> {

}
