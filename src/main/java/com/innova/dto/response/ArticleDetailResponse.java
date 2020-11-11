package com.innova.dto.response;

import com.innova.model.Article;

public class ArticleDetailResponse {

    private Article article;

    private boolean liked;

    private boolean bookmarked;

    public ArticleDetailResponse() {
    }

    public ArticleDetailResponse(Article article, boolean liked, boolean bookmarked) {
        this.article = article;
        this.liked = liked;
        this.bookmarked = bookmarked;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }
}
