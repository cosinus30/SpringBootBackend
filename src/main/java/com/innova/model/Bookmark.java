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
@Table(name = "bookmarks", schema = "public")
public class Bookmark {

    @EmbeddedId
    BookmarkKey id;

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

    @Column(name = "bookmark_date")
    private LocalDateTime bookmarkDate;

    public Bookmark() {
    }

    public Bookmark(Article article, User user) {
        this.id = new BookmarkKey(user.getId(), article.getId());
        this.article = article;
        this.user = user;
        this.bookmarkDate = LocalDateTime.now();
    }

    public BookmarkKey getId() {
        return id;
    }

    public void setId(BookmarkKey id) {
        this.id = id;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public LocalDateTime getBookmarkDate() {
        return bookmarkDate;
    }

    public void setBookmarkDate(LocalDateTime bookmarkDate) {
        this.bookmarkDate = bookmarkDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
