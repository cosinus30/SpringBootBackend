package com.innova.security.services;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.innova.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserDetailImpl implements UserDetails, OAuth2User {
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String username;

    private String email;

    private String name;

    private String lastname;

    private String phoneNumber;

    private String age;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    private Map<String, Object> attributes;


    public UserDetailImpl(Integer id,
                          String username, String email, String name, String lastname, String phoneNumber, String age, String password,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.lastname = lastname;
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getRole().name())
        ).collect(Collectors.toList());

        return new UserDetailImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getLastname(),
                user.getPhoneNumber(),
                user.getAge(),
                user.getPassword(),
                authorities
        );
    }

    public static UserDetailImpl build(User user, Map<String, Object> attributes) {
        UserDetailImpl userPrincipal = UserDetailImpl.build(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAge() {
        return age;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDetailImpl user = (UserDetailImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}
