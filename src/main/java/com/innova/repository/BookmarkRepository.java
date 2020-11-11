package com.innova.repository;

import com.innova.model.Bookmark;
import com.innova.model.BookmarkKey;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkKey> {

}
