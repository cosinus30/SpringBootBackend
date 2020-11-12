package com.innova.service;

import java.util.Optional;
import java.util.Set;

import com.innova.model.Article;
import com.innova.model.User;
import com.innova.model.View;
import com.innova.repository.ArticleRepository;
import com.innova.repository.ViewRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ViewServiceImpl implements ViewService{

    @Autowired
    ViewRepository viewRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Override
    public Set<View> getViewsOfUser(User user) {
        return user.getViews();
    }

    @Override
    public View saveView(User user, Article article) {
        View view = new View(article, user);
        return viewRepository.save(view);
    }
}