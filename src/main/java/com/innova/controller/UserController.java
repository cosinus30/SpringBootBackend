package com.innova.controller;

import com.innova.constants.ErrorCodes;
import com.innova.dto.request.ChangeForm;
import com.innova.dto.request.ChangePasswordForm;
import com.innova.dto.request.ForgotAndChangePasswordForm;
import com.innova.dto.request.LogoutForm;
import com.innova.dto.response.SuccessResponse;
import com.innova.event.OnRegistrationSuccessEvent;
import com.innova.exception.BadRequestException;
import com.innova.exception.ErrorWhileSendingEmailException;
import com.innova.exception.UnauthorizedException;
import com.innova.model.ActiveSessions;
import com.innova.model.TokenBlacklist;
import com.innova.model.User;
import com.innova.repository.ActiveSessionsRepository;
import com.innova.repository.TokenBlacklistRepository;
import com.innova.repository.UserRepository;
import com.innova.security.jwt.JwtProvider;
import com.innova.security.services.UserDetailImpl;
import com.innova.util.PasswordUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("api/user")
public class UserController {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    ActiveSessionsRepository activeSessionsRepository;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping("/")
    public ResponseEntity<?> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();
        return ResponseEntity.ok().body(userDetails);
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editUser(@Valid @RequestBody ChangeForm changeForm) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User cannot find"));
        if (changeForm.getEmail() != null && !changeForm.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(changeForm.getEmail())) {
                return new ResponseEntity<String>("Email is already in use!", HttpStatus.BAD_REQUEST);
            }
            Set<ActiveSessions> activeSessionsForUserWithCurrentEmail = user.getActiveSessions();
            for (ActiveSessions activeSession : activeSessionsForUserWithCurrentEmail) {
                activeSessionsRepository.deleteById(activeSession.getRefreshToken());
            }
            user.setEmail(changeForm.getEmail());
            user.setEnabled(false);
            try {
                eventPublisher.publishEvent(new OnRegistrationSuccessEvent(user, "/api/auth"));
            } catch (Exception re) {
                throw new ErrorWhileSendingEmailException(re.getMessage());
            }
        }
        user.setName(changeForm.getName());
        user.setLastname(changeForm.getLastname());
        user.setAge(changeForm.getAge());
        user.setPhoneNumber(changeForm.getPhoneNumber());
        userRepository.save(user);
        SuccessResponse response = new SuccessResponse(HttpStatus.OK, "User details successfuly changed.");
        return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody LogoutForm logoutForm) throws IllegalArgumentException {
        if (logoutForm.getAccessToken() == null || logoutForm.getRefreshToken() == null) {
            throw new BadRequestException("Both tokens should be provided", ErrorCodes.REQUIRE_BOTH_TOKENS);
        } else {
            TokenBlacklist oldAccessToken = new TokenBlacklist(logoutForm.getAccessToken(), "access token");
            TokenBlacklist oldRefreshToken = new TokenBlacklist(logoutForm.getRefreshToken(), "refresh token");
            activeSessionsRepository.deleteById(logoutForm.getRefreshToken());
            tokenBlacklistRepository.save(oldAccessToken);
            tokenBlacklistRepository.save(oldRefreshToken);
            SuccessResponse response = new SuccessResponse(HttpStatus.OK, "Successfully logged out");
            return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> createNewPassword(@RequestBody ChangePasswordForm changePasswordForm) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User cannot find"));
        if (!changePasswordForm.checkAllFieldsAreGiven(changePasswordForm)) {
            throw new BadRequestException("All fields should be given", ErrorCodes.REQUIRE_ALL_FIELDS);
        } else {
            if (!passwordEncoder.matches(changePasswordForm.getOldPassword(), user.getPassword())) {
                throw new BadRequestException("Your old password is not correct",
                        ErrorCodes.OLD_PASSWORD_DOES_NOT_MATCH);
            } else if (!changePasswordForm.getNewPassword().equals(changePasswordForm.getNewPasswordConfirmation())) {
                throw new BadRequestException("Password fields does not match", ErrorCodes.NEW_PASSWORD_DOES_NOT_MATCH);
            } else if (changePasswordForm.getNewPassword().equals(changePasswordForm.getNewPasswordConfirmation())) {
                if (!PasswordUtil.isValidPassword(changePasswordForm.getNewPassword())) {
                    throw new BadRequestException("Password is not valid", ErrorCodes.PASSWORD_NOT_VALID);
                }
                user.setPassword(passwordEncoder.encode(changePasswordForm.getNewPassword()));
                userRepository.save(user);
                SuccessResponse response = new SuccessResponse(HttpStatus.OK, "Password successfully changed");
                return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
            } else {
                throw new BadRequestException("Something is wrong", ErrorCodes.SOMETHING_IS_WRONG);
            }
        }

    }

    @PostMapping("/create-new-password")
    public ResponseEntity<?> createNewPassword(@RequestBody ForgotAndChangePasswordForm forgotAndChangePasswordForm,
            HttpServletRequest request) {
        if (!forgotAndChangePasswordForm.checkAllFieldsAreGiven(forgotAndChangePasswordForm)) {
            throw new BadRequestException("All fields should be given", ErrorCodes.REQUIRE_ALL_FIELDS);
        } else {
            String token = forgotAndChangePasswordForm.getToken();
            if (token != null && jwtProvider.validateJwtToken(token, "password", request)) {
                String email = jwtProvider.getSubjectFromJwt(token, "password");
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new BadRequestException("No such user", ErrorCodes.NO_SUCH_USER));
                if (!forgotAndChangePasswordForm.getNewPassword()
                        .equals(forgotAndChangePasswordForm.getNewPasswordConfirmation())) {

                    throw new BadRequestException("Password fields does not match",
                            ErrorCodes.NEW_PASSWORD_DOES_NOT_MATCH);

                } else if (forgotAndChangePasswordForm.getNewPassword()
                        .equals(forgotAndChangePasswordForm.getNewPasswordConfirmation())) {
                    user.setPassword(passwordEncoder.encode(forgotAndChangePasswordForm.getNewPassword()));
                    userRepository.save(user);
                    SuccessResponse response = new SuccessResponse(HttpStatus.OK, "Password successfully changed");
                    return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
                }
            } else {
                throw new UnauthorizedException("Something is wrong with token", ErrorCodes.INVALID_ACCESS_TOKEN);
            }
        }
        throw new BadRequestException("Something is wrong", ErrorCodes.SOMETHING_IS_WRONG);
    }

    @GetMapping("/active-sessions")
    public ResponseEntity<?> getAllActiveSessions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User cannot find"));
        Set<ActiveSessions> activeSessionsForUser = user.getActiveSessions();
        return ResponseEntity.ok().body(activeSessionsForUser);
    }

    @DeleteMapping("/logout-from-session")
    public ResponseEntity<?> logoutFromSession(@RequestParam("token") String refreshToken,
            @RequestParam("accessToken") String accessToken, HttpServletRequest request) {
        if (refreshToken != null) {
            if (jwtProvider.validateJwtToken(refreshToken, "refresh", request)) {
                activeSessionsRepository.deleteById(refreshToken);
                TokenBlacklist oldRefreshToken = new TokenBlacklist(refreshToken, "refresh token");
                TokenBlacklist oldAccessToken = new TokenBlacklist(accessToken, "access token");
                tokenBlacklistRepository.save(oldRefreshToken);
                tokenBlacklistRepository.save(oldAccessToken);
                SuccessResponse response = new SuccessResponse(HttpStatus.OK,
                        "Successfully logged out from " + refreshToken);
                return new ResponseEntity<>(response, new HttpHeaders(), response.getStatus());
            } else {
                throw new UnauthorizedException("All fields should be given", ErrorCodes.INVALID_REFRESH_TOKEN);
            }
        } else {
            throw new BadRequestException("Token must be given", ErrorCodes.REQUIRE_ALL_FIELDS);
        }

    }
}