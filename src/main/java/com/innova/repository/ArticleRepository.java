package com.innova.repository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import com.innova.model.Article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

@Repository
public interface ArticleRepository extends PagingAndSortingRepository<Article, Integer> {

    @Query(value = "SELECT article.* FROM articles article WHERE article.content_type = :contentType and article.published = true", nativeQuery = true)
    List<Article> findArticlesByType(@Param("contentType") String contentType);

    Page<Article> findByPublishedAndContentType(boolean published,String contentType, Pageable pageable);

    Page<Article> findByPublishedAndContentTypeAndReleaseDateBetween(boolean published,String contentType, Pageable pageable, LocalDateTime releaseDateStart, LocalDateTime releaseDateEnd);

    @Query(value = "SELECT article.* FROM articles article WHERE article.author = :userId", nativeQuery = true)
    List<Article> findByUserId(@Param("userId") Integer userId);

}
