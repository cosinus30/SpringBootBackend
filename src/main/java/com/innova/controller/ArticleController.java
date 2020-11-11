package com.innova.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import javax.validation.Valid;

import com.innova.constants.ErrorCodes;
import com.innova.dto.request.CreateArticleForm;
import com.innova.dto.response.ArticleDetailResponse;
import com.innova.dto.response.SuccessResponse;
import com.innova.exception.BadRequestException;
import com.innova.exception.UnauthorizedException;
import com.innova.model.Article;
import com.innova.model.User;
import com.innova.service.ArticleService;
import com.innova.service.UserService;
import com.innova.service.LikeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // TODO Add pagination
    @GetMapping("/tutorials")
    public ResponseEntity<?> getAllTutorials() {
        List<Article> tutorials = articleService.getAllTutorials();
        return ResponseEntity.ok().body(tutorials);
    }

    // TODO look at multiple url mappings
    @GetMapping("/tutorials/{articleId}")
    public ResponseEntity<?> getTutorial(@PathVariable String articleId) {
        User user = userService.getUserWithAuthentication(SecurityContextHolder.getContext().getAuthentication());
        Article tutorialDetail = articleService.getById(Integer.parseInt(articleId))
                .orElseThrow(() -> new BadRequestException("No such article", ErrorCodes.NO_SUCH_USER));

        if (user != null) {

            boolean isUserLiked = likeService.isUserLiked(user, tutorialDetail);
            boolean isBookmarked = false; // TODO add bookmark service
            return ResponseEntity.ok().body(new ArticleDetailResponse(tutorialDetail, isUserLiked, isBookmarked));
        }
        return ResponseEntity.ok().body(new ArticleDetailResponse(tutorialDetail, false, false));
    }

    @GetMapping("/insights")
    public ResponseEntity<?> getAllInsights() {
        List<Article> insights = articleService.getAllInsights();
        return ResponseEntity.ok().body(insights);
    }

    @GetMapping("/insights/{articleId}")
    public ResponseEntity<?> getInsight(@PathVariable String articleId) {
        Article insightDetail = articleService.getById(Integer.parseInt(articleId))
                .orElseThrow(() -> new BadRequestException("No such article", ErrorCodes.NO_SUCH_USER));
        return ResponseEntity.ok().body(insightDetail);
    }

    @GetMapping("/engineerings")
    public ResponseEntity<?> getAllEngineering() {
        List<Article> engineerings = articleService.getAllEngineerings();
        return ResponseEntity.ok().body(engineerings);
    }

    @GetMapping("/engineerings/{articleId}")
    public ResponseEntity<?> getEngineering(@PathVariable String articleId) {
        Article engineeringDetail = articleService.getById(Integer.parseInt(articleId))
                .orElseThrow(() -> new BadRequestException("No such article", ErrorCodes.NO_SUCH_USER));
        return ResponseEntity.ok().body(engineeringDetail);
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
        // TODO create bookmark table, model, repository, service, serviceImpl
        return null;
    }

}
