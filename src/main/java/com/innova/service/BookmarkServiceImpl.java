package com.innova.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import com.innova.model.Article;
import com.innova.model.Bookmark;
import com.innova.model.BookmarkKey;
import com.innova.model.User;
import com.innova.repository.ArticleRepository;
import com.innova.repository.BookmarkRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    @Autowired
    BookmarkRepository bookmarkRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Override
    public Bookmark saveBookmark(User user, Article article) {
        Bookmark bookmark = new Bookmark(article, user);
        article.setBookmarkCount(article.getBookmarkCount() + 1);
        articleRepository.save(article);
        return bookmarkRepository.save(bookmark);
    }

    public void removeBookmark(User user, Article article) throws NoSuchElementException {
        Optional<Bookmark> bookmark = bookmarkRepository.findById(new BookmarkKey(user.getId(), article.getId()));
        article.setBookmarkCount(article.getBookmarkCount() - 1);
        articleRepository.save(article);
        bookmarkRepository.delete(bookmark.get());
    }

    @Override
    public Set<Bookmark> getBookmarksOfUser(User user) {
        return user.getBookmarks();
    }

    @Override
    public boolean isUserBookmarked(User user, Article article) {
        Optional<Bookmark> bookmark = bookmarkRepository.findById(new BookmarkKey(user.getId(), article.getId()));
        return bookmark.isPresent();
    }

}
