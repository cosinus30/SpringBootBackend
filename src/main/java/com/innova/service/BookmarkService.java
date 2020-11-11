package com.innova.service;

import java.util.Set;

import com.innova.model.Article;
import com.innova.model.Bookmark;
import com.innova.model.User;

public interface BookmarkService {
    public Bookmark saveBookmark(User user, Article article);

    public void removeBookmark(User user, Article article);

    public boolean isUserBookmarked(User user, Article article);

    public Set<Bookmark> getBookmarksOfUser(User user);
}
