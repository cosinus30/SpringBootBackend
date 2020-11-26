package com.innova.service;

import java.util.Optional;

import com.innova.model.Article;
import com.innova.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleService {
    public Article saveArticle(Article article, String [] tags);

    public void deleteArticle(Integer articleId, User user);

    public Page<Article> getAllArticlesByUserId(User user, String contentType, Pageable pageable, String releaseSit);

    public Optional<Article> getById(Integer id);

    public Article updateArticle(Integer id, String content, String contentType, boolean published, int readTime,
            String heading, String [] tags);

    public Page<Article> getArticles(boolean published, String contentType, Pageable pageable);

    public Page<Article> getArticles(boolean published, String contentType, Pageable pageable, String span);

}
