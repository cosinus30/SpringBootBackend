package com.innova.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import com.innova.constants.ErrorCodes;
import com.innova.dto.request.CommentForm;
import com.innova.dto.request.CreateArticleForm;
import com.innova.dto.response.ArticleDetailResponse;
import com.innova.dto.response.SuccessResponse;
import com.innova.exception.BadRequestException;
import com.innova.exception.UnauthorizedException;
import com.innova.model.Article;
import com.innova.model.Comment;
import com.innova.model.User;
import com.innova.service.ArticleService;
import com.innova.service.BookmarkService;
import com.innova.service.CommentService;
import com.innova.service.UserService;
import com.nimbusds.oauth2.sdk.Response;
import com.innova.service.LikeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/articles")
public class ArticleController {

    @Autowired
    UserService userService;

    @Autowired
    ArticleService articleService;

    @Autowired
    LikeService likeService;

    @Autowired
    BookmarkService bookmarkService;

    @Autowired
    CommentService commentService;


    @GetMapping("/{contentType}")
    public ResponseEntity<?> getTutorialsByType(@RequestParam Optional<String> time, @PathVariable String contentType, Pageable pageable){
        if(time.isPresent()){
            Page<Article> publishedArticlesByTypeAndDate = articleService.getArticles(true, contentType, pageable, time.get());
            System.out.println("Span is present it seems!");
            return ResponseEntity.ok().body(publishedArticlesByTypeAndDate);
        }
        else{
            Page<Article> publishedArticlesByType = articleService.getArticles(true, contentType, pageable);
            System.out.println("Span is present it seems! Naaaaah");
            return ResponseEntity.ok().body(publishedArticlesByType);
        }
    }

    @GetMapping(value = { "/tutorials/{articleId}", "/insights/{articleId}", "/engineerings/{articleId}" })
    public ResponseEntity<?> getArticleDetail(@PathVariable String articleId) {
        User user = userService.getUserWithAuthentication(SecurityContextHolder.getContext().getAuthentication());
        Article articleDetail = articleService.getById(Integer.parseInt(articleId))
                .orElseThrow(() -> new BadRequestException("No such article", ErrorCodes.NO_SUCH_USER));


        if (user != null) {
            boolean isUserLiked = likeService.isUserLiked(user, articleDetail);
            boolean isBookmarked = bookmarkService.isUserBookmarked(user, articleDetail);
            return ResponseEntity.ok().body(new ArticleDetailResponse(articleDetail, isUserLiked, isBookmarked));
        }
        return ResponseEntity.ok().body(new ArticleDetailResponse(articleDetail, false, false));
    }

    @PostMapping("/")
    public ResponseEntity<?> createNewArticle(@Valid @RequestBody CreateArticleForm createArticleForm) {
        User user = userService.getUserWithAuthentication(SecurityContextHolder.getContext().getAuthentication());
        Article article = new Article(createArticleForm.getContent(), createArticleForm.getPublished(),
                createArticleForm.getContentType(), createArticleForm.getReadTime(), user,
                createArticleForm.getHeading());
        articleService.saveArticle(article);
        SuccessResponse response = new SuccessResponse(HttpStatus.CREATED, "Article created successfully");
        return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
    }

    @PutMapping("/")
    public ResponseEntity<?> updateArticle(@Valid @RequestBody CreateArticleForm createArticleForm,
            @Param("articleId") Integer articleId) {
        User user = userService.getUserWithAuthentication(SecurityContextHolder.getContext().getAuthentication());
        Article article = articleService.getById(articleId)
                .orElseThrow(() -> new BadRequestException("No such article", ErrorCodes.NO_SUCH_USER));
        if (user.getId() != article.getAuthor().getId()) {
            throw new UnauthorizedException("Only author can update his/her article", ErrorCodes.INVALID_ACCESS_TOKEN);
        }
        articleService.updateArticle(articleId, createArticleForm.getContent(), createArticleForm.getContentType(),
                createArticleForm.getPublished(), createArticleForm.getReadTime(), createArticleForm.getHeading());
        SuccessResponse response = new SuccessResponse(HttpStatus.OK, "Article updated successfully");
        return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
    }

    @PostMapping("/{articleId}/like")
    public ResponseEntity<?> likeArticle(@PathVariable String articleId) {
        User user = userService.getUserWithAuthentication(SecurityContextHolder.getContext().getAuthentication());
        Article article = articleService.getById(Integer.parseInt(articleId))
                .orElseThrow(() -> new BadRequestException("No such article", ErrorCodes.NO_SUCH_USER));
        likeService.saveLike(user, article);
        // TODO return success response
        return ResponseEntity.ok().body("WOHOOO");
    }

    @PostMapping("/{articleId}/unlike")
    public ResponseEntity<?> unlikeArticle(@PathVariable String articleId) {
        User user = userService.getUserWithAuthentication(SecurityContextHolder.getContext().getAuthentication());
        Article article = articleService.getById(Integer.parseInt(articleId))
                .orElseThrow(() -> new BadRequestException("No such article", ErrorCodes.NO_SUCH_USER));

        try {
            likeService.removeLike(user, article);
        } catch (NoSuchElementException e) {
            throw new BadRequestException("You have never liked that", ErrorCodes.NO_SUCH_USER);
        }
        return null;
    }

    @PostMapping("/{articleId}/bookmark")
    public ResponseEntity<?> bookmarkArticle(@PathVariable String articleId) {
        User user = userService.getUserWithAuthentication(SecurityContextHolder.getContext().getAuthentication());
        Article article = articleService.getById(Integer.parseInt(articleId))
                .orElseThrow(() -> new BadRequestException("No such article", ErrorCodes.NO_SUCH_USER));
        bookmarkService.saveBookmark(user, article);
        return ResponseEntity.ok().body("WOHOOO");
    }

    @PostMapping("/{articleId}/unbookmark")
    public ResponseEntity<?> unbookmarkArticle(@PathVariable String articleId) {
        User user = userService.getUserWithAuthentication(SecurityContextHolder.getContext().getAuthentication());
        Article article = articleService.getById(Integer.parseInt(articleId))
                .orElseThrow(() -> new BadRequestException("No such article", ErrorCodes.NO_SUCH_USER));

        try {
            bookmarkService.removeBookmark(user, article);
        } catch (NoSuchElementException e) {
            throw new BadRequestException("You have never bookmarked that", ErrorCodes.NO_SUCH_USER);
        }
        return null;
    }

    @GetMapping("{articleType}/{articleId}/comment")
    public ResponseEntity<?> getComments(@PathVariable String articleId){
        Set<Comment> comments = commentService.getCommentsByArticle(Integer.parseInt(articleId));
        return ResponseEntity.ok().body(comments);
    }

    @PostMapping("{articleType}/{articleId}/comment")
    public ResponseEntity<?> makeComment(@PathVariable String articleId,@Valid @RequestBody CommentForm commentForm){
        User user = userService.getUserWithAuthentication(SecurityContextHolder.getContext().getAuthentication());
        commentService.makeComment(user, Integer.parseInt(articleId), commentForm.getContent());
        return null;
    }

    @PutMapping("{articleType}/{articleId}/comment/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable String commentId, @Valid @RequestBody CommentForm commentForm){
        User user = userService.getUserWithAuthentication(SecurityContextHolder.getContext().getAuthentication());
        Comment comment = commentService.updateComment(user, Integer.parseInt(commentId), commentForm.getContent());
        if(comment == null)
            return ResponseEntity.badRequest().body("Nnaaah");
        else
            return ResponseEntity.ok().body(comment);
    }

    @DeleteMapping("{articleType}/{articleId}/comment/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable String commentId){
        User user = userService.getUserWithAuthentication(SecurityContextHolder.getContext().getAuthentication());
        boolean success = commentService.deleteComment(Integer.parseInt(commentId), user);
        if(success)
            return ResponseEntity.ok().body("false");
        else
            return ResponseEntity.badRequest().body("True");
    }

}
