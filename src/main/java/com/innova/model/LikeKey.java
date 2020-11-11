package com.innova.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LikeKey implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1817478072408039320L;

    @Column(name = "user_id")
    Integer userId;

    @Column(name = "article_id")
    Integer articleId;

    public LikeKey() {
    }

    public LikeKey(Integer userId, Integer articleId) {
        this.userId = userId;
        this.articleId = articleId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        LikeKey that = (LikeKey) o;
        return Objects.equals(userId, that.userId) && Objects.equals(articleId, that.articleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, articleId);
    }
}
