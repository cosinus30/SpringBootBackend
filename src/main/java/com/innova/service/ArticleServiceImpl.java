package com.innova.service;

import java.time.LocalDateTime;
import java.util.List;

import com.innova.model.Article;
import com.innova.model.User;
import com.innova.repository.ArticleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    ArticleRepository articleRepository;

    @Override
    public List<Article> getAllEngineerings() {
        List<Article> engineering = articleRepository.findArticlesByType("Engineering");
        return engineering;
    }

    @Override
    public List<Article> getAllInsights() {
        List<Article> insight = articleRepository.findArticlesByType("Insight");
        return insight;
    }

    @Override
    public List<Article> getAllTutorials() {
        List<Article> tutorials = articleRepository.findArticlesByType("Tutorial");
        return tutorials;
    }

    @Override
    public Article saveArticle(Article article) {
        boolean isPublished = article.getPublished();
        if (isPublished) {
            article.setReleaseDate(LocalDateTime.now());
        } else {
            article.setReleaseDate(null);
        }
        User user = article.getAuthor();
        user.addArticle(article);
        return articleRepository.save(article);
    }

    @Override
    public List<Article> getAllArticlesByUserId(Integer userId) {
        List<Article> articlesOfUser = articleRepository.findByUserId(userId);
        return articlesOfUser;
    }

    @Override
    public Article getById(Integer id) {
        Article article = articleRepository.getOne(id);
        return article;
    }

    @Override
    public Article updateArticle(Integer id, String content, String contentType, boolean published, int readTime) {
        Article article = articleRepository.getOne(id);
        article.setContent(content);
        article.setContentType(contentType);
        article.setPublished(published);
        article.setReadTime(readTime);
        if (published) {
            article.setReleaseDate(LocalDateTime.now());
        }
        return articleRepository.save(article);
    }
}
