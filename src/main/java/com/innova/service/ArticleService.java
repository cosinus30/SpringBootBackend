package com.innova.service;

import java.util.List;
import java.util.Optional;

import com.innova.model.Article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleService {
    public Article saveArticle(Article article, String [] tags);

    public List<Article> getAllArticlesByUserId(Integer userId);

    public Optional<Article> getById(Integer id);

    public Article updateArticle(Integer id, String content, String contentType, boolean published, int readTime,
            String heading);

    public Page<Article> getArticles(boolean published, String contentType, Pageable pageable);

    public Page<Article> getArticles(boolean published, String contentType, Pageable pageable, String span);

}
