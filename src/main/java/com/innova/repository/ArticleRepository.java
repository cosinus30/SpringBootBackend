package com.innova.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import com.innova.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {

    @Query(value = "SELECT article.* FROM articles article WHERE article.content_type = :contentType and article.published = true", nativeQuery = true)
    List<Article> findArticlesByType(@Param("contentType") String contentType);

    @Query(value = "SELECT article.* FROM articles article WHERE article.author = :userId", nativeQuery = true)
    List<Article> findByUserId(@Param("userId") Integer userId);

}
