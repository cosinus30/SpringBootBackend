package com.innova.service;

import java.util.Set;

import com.innova.model.Article;
import com.innova.model.User;
import com.innova.model.View;

public interface ViewService {
    
    public View saveView(User user, Article article);

    public Set<View> getViewsOfUser(User user);

}
