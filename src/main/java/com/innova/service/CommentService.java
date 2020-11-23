package com.innova.service;

import java.util.Set;

import com.innova.model.Comment;
import com.innova.model.User;

public interface CommentService {
    
    public Set<Comment> getCommentsByArticle(Integer articleId);

    public Set<Comment> getCommentsByUser(User user);

    public Comment makeComment(User user, Integer articleId, String content);

    public boolean deleteComment(Integer commentId, User user);

    public Comment updateComment(User user, Integer commentId, String content);
}
