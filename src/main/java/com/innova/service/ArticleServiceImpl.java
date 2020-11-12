package com.innova.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
            article.setReleaseDate(LocalDateTime.now());
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
    public Optional<Article> getById(Integer id) {
        Optional<Article> article = articleRepository.findById(id);
        article.get().setViewCount(article.get().getViewCount() + 1);
        articleRepository.save(article.get());
        return article;
    }

    @Override
    public Article updateArticle(Integer id, String content, String contentType, boolean published, int readTime,
            String heading) {
        Article article = articleRepository.getOne(id);
        article.setContent(content);
        article.setContentType(contentType);
        article.setPublished(published);
        article.setReadTime(readTime);
        article.setHeading(heading);
        if (published) {
            article.setReleaseDate(LocalDateTime.now());
        }
        return articleRepository.save(article);
    }
}
