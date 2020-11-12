package com.innova.service;

import java.util.List;
import java.util.Optional;

import com.innova.model.Article;

public interface ArticleService {
    public Article saveArticle(Article article);

    public List<Article> getAllTutorials();

    public List<Article> getAllInsights();

    public List<Article> getAllEngineerings();

    public List<Article> getAllArticlesByUserId(Integer userId);

    public Optional<Article> getById(Integer id);

    public Article updateArticle(Integer id, String content, String contentType, boolean published, int readTime,
            String heading);

}
