package com.innova.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import com.innova.model.Article;
import com.innova.model.Comment;
import com.innova.model.User;
import com.innova.repository.ArticleRepository;
import com.innova.repository.CommentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {
    
    @Autowired
    CommentRepository commentRepository;

    @Autowired
    ArticleRepository articleRepository;
    
    @Override
    public Set<Comment> getCommentsByArticle(Integer articleId) {
        return commentRepository.findByArticleIdOrderByCommentDateDesc(articleId);
    }

    @Override
    public Set<Comment> getCommentsByUser(User user) {
        return commentRepository.findByUserIdOrderByCommentDateDesc(user.getId());
    }

    @Override
    public Comment makeComment(User user, Integer articleId, String content) {
        Optional<Article> article = articleRepository.findById(articleId);
        Comment comment = new Comment(content, LocalDateTime.now(), user, article.get());
        return commentRepository.save(comment);
    }

    public boolean deleteComment(Integer commentId, User user){
        Comment comment = commentRepository.getOne(commentId);
        if(comment.getUser().getId() == user.getId()){
            commentRepository.delete(comment);
            return true;
        }
        else
            return false;
    }

    @Override
    public Comment updateComment(User user, Integer commentId, String content) {
        Comment comment = commentRepository.findById(commentId).get();
        if(comment.getUser().getId() == user.getId()){
            comment.setContent(content);
            return commentRepository.save(comment);
        }
        return null;
    }

}
