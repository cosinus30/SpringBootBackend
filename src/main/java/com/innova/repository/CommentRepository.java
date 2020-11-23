package com.innova.repository;

import java.util.Set;

import com.innova.model.Comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Integer> {
    
    public Set<Comment> findByArticleIdOrderByCommentDateDesc(Integer articleId);

    public Set<Comment> findByUserIdOrderByCommentDateDesc(Integer userId);
}
