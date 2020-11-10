package com.innova.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.hibernate.annotations.Fetch;

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

    @Column(name = "heading")
    @Max(50)
    private String heading;

    @Column(name = "read_time")
    private int readTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author", nullable = false)
    @JsonIgnoreProperties(value = { "activeSessions", "articles", "roles" })
    private User author;

    public Article() {

    }

    public Article(String content, boolean published, String contentType, int readTime, User author, String heading) {
        this.content = content;
        this.published = published;
        this.contentType = contentType;
        this.readTime = readTime;
        this.author = author;
        this.heading = heading;
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

    public String getHeading() {
        return this.heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
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
