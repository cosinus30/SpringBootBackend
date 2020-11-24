package com.innova.model;

import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="tags", schema="public")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    @JsonIgnore
    private Integer id;

    @Column(name="tag_name")
    private String tagName;

    @Column(name="tag_detail")
    @JsonIgnore
    private String tagDetail;

    @JsonIgnore
    @Column(name="articleCount")
    private int articleCount;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_tags", joinColumns = @JoinColumn(name = "tag_id"), inverseJoinColumns = @JoinColumn(name = "article_id"))
    @JsonIgnore
    private Set<Article> articles;

    public Tag(){}

    public Tag(String tagName, String tagDetail){
        this.tagDetail = tagDetail;
        this.tagName = tagName;
    }

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagDetail() {
        return tagDetail;
    }

    public void setTagDetail(String tagDetail) {
        this.tagDetail = tagDetail;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }

}

