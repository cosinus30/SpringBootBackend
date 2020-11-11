package com.innova.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import com.innova.model.Article;
import com.innova.model.Like;
import com.innova.model.LikeKey;
import com.innova.model.User;
import com.innova.repository.LikeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    LikeRepository likeRepository;

    @Override
    public Like saveLike(User user, Article article) {
        Like like = new Like(article, user);
        return likeRepository.save(like);
    }

    public void removeLike(User user, Article article) throws NoSuchElementException {
        Optional<Like> like = likeRepository.findById(new LikeKey(user.getId(), article.getId()));
        likeRepository.delete(like.get());
    }

    @Override
    public Set<Like> getLikesOfUser(User user) {
        return user.getLikes();
    }

}
