package com.innova.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.innova.model.Article;
import com.innova.model.Tag;
import com.innova.model.User;
import com.innova.repository.ArticleRepository;
import com.innova.repository.TagRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    ArticleRepository articleRepository;

    @Autowired 
    TagRepository tagRepository;

    @Override
    public Article saveArticle(Article article, String [] tags) {

        boolean isPublished = article.getPublished();
        if (isPublished) {
            article.setReleaseDate(LocalDateTime.now());
        } else {
            article.setReleaseDate(LocalDateTime.now());
        }

        for(String tag: tags){
            Tag currTag = tagRepository.findByTagName(tag);
            if(currTag == null){
                Tag newTag = new Tag(tag, "");
                newTag.setArticleCount(1);
                tagRepository.save(newTag);
                article.addTag(newTag);
            }
            else{
                currTag.setArticleCount(currTag.getArticleCount() + 1);
                article.addTag(currTag);
            }
        }
        User user = article.getAuthor();
        user.addArticle(article);
        return articleRepository.save(article);
    }

    @Override
    public Page<Article> getAllArticlesByUserId(User user, String contentType, Pageable pageable) {
        switch (contentType) {
            case "tutorials":
                contentType = "Tutorial";
                break;
            case "insights":
                contentType = "Insight";
                break;            
            case "engineerings":
                contentType = "Engineering";
                break;
            default:
                contentType = "";
                break;
        }
        Page<Article> articlesOfUser;
        if(contentType.equals("")){
            articlesOfUser = articleRepository.findByAuthorOrderByReleaseDateDesc(user, pageable);
        }
        else{
            articlesOfUser = articleRepository.findByAuthorAndContentTypeOrderByReleaseDateDesc(user, contentType, pageable);
        }
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
            String heading, String [] tags) {
        Article article = articleRepository.findById(id).get();
        article.setContent(content);
        article.setContentType(contentType);
        article.setPublished(published);
        article.setReadTime(readTime);
        article.setHeading(heading);
        article.setTags(new HashSet<>());
        for(String tag: tags){
            Tag currTag = tagRepository.findByTagName(tag);
            if(currTag == null){
                Tag newTag = new Tag(tag, "");
                newTag.setArticleCount(1);
                tagRepository.save(newTag);
                article.addTag(newTag);
            }
            else{
                currTag.setArticleCount(currTag.getArticleCount() + 1);
                article.addTag(currTag);
            }
        }
        if (published) {
            article.setReleaseDate(LocalDateTime.now());
        }
        return articleRepository.save(article);
    }

    @Override
    public Page<Article> getArticles(boolean published, String contentType, Pageable pageable) {
        switch (contentType) {
            case "tutorials":
                contentType = "Tutorial";
                break;
            case "insights":
                contentType = "Insight";
                break;            
            case "engineerings":
                contentType = "Engineering";
                break;
            default:
                contentType = "";
                break;
        }

        Page<Article> articles = articleRepository.findByPublishedAndContentType(published, contentType, pageable);
        return articles;
    }

    @Override
    public Page<Article> getArticles(boolean published, String contentType, Pageable pageable, String span) {
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();
        switch (contentType) {
            case "tutorials":
                contentType = "Tutorial";
                break;
            case "insights":
                contentType = "Insight";
                break;            
            case "engineerings":
                contentType = "Engineering";
                break;
            default:
                contentType = "";
                break;
        }
        switch (span) {
            case "3days":
                start = LocalDateTime.now().minusDays(3);
                break;
            case "week":
                start = LocalDateTime.now().minusWeeks(1);
                break;
            case "month":
                start = LocalDateTime.now().minusMonths(1);
                break;
            default:
                Page<Article> articles = articleRepository.findByPublishedAndContentType(published, contentType, pageable);
                return articles;
        }

        Page<Article> articles = articleRepository.findByPublishedAndContentTypeAndReleaseDateBetween(published, contentType, pageable, start, end);
        return articles;
    }

    @Override
    public void deleteArticle(Integer articleId, User user) {
        Optional<Article> article = articleRepository.findById(articleId);
        articleRepository.delete(article.get());
    }
}
