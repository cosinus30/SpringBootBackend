package com.innova.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "views", schema = "public")
public class View {
    
    @EmbeddedId
    ViewKey id;

    @ManyToOne
    @MapsId("articleId")
    @JoinColumn(name = "article_id")
    @JsonIgnoreProperties(value = { "content", "published" })
    Article article;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties(value = { "activeSessions", "articles", "roles", "enabled", "phoneNumber" })
    User user;

    @Column(name = "like_date")
    private LocalDateTime likeDate;

    public View() {
    }

    public View(Article article, User user) {
        this.id = new ViewKey(user.getId(), article.getId());
        this.article = article;
        this.user = user;
        this.likeDate = LocalDateTime.now();
    }

    public ViewKey getId() {
        return id;
    }

    public void setId(ViewKey id) {
        this.id = id;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public LocalDateTime getLikeDate() {
        return likeDate;
    }

    public void setLikeDate(LocalDateTime likeDate) {
        this.likeDate = likeDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
