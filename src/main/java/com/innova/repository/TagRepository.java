package com.innova.repository;

import java.util.List;

import com.innova.model.Tag;

import org.springframework.data.jpa.repository.JpaRepository;


public interface TagRepository extends JpaRepository<Tag, Integer> {
    public Tag findByTagName(String tagName);

    public List<Tag> findFirst100OrderByArticleCount(String title);

    public List<Tag> findAllByOrderByTagNameDesc();
}
