package com.innova.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "articles", schema = "public")
public class Article {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "content")
    private String content;

    @Column(name = "published")
    private boolean published;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "read_time")
    private int readTime;

    @ManyToOne
    @JoinColumn(name = "author", nullable = false)
    @JsonBackReference
    private User author;

    public Article() {

    }

    public Article(String content, boolean published, String contentType, int readTime, User author) {
        this.content = content;
        this.published = published;
        this.contentType = contentType;
        this.readTime = readTime;
        this.author = author;
    }

    public Integer getArticleId() {
        return this.id;
    }

    public void setArticleId(Integer articleId) {
        this.id = articleId;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getPublished() {
        return this.published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public LocalDateTime getReleaseDate() {
        return this.releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getReadTime() {
        return this.readTime;
    }

    public void setReadTime(int readTime) {
        this.readTime = readTime;
    }

    public User getAuthor() {
        return this.author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

}
