package com.innova.service;

import java.util.Set;

import com.innova.model.Article;
import com.innova.model.Like;
import com.innova.model.User;

public interface LikeService {

    public Like saveLike(User user, Article article);

    public void removeLike(User user, Article article);

    public boolean isUserLiked(User user, Article article);

    public Set<Like> getLikesOfUser(User user);
}