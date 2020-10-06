package com.innova.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;


@Entity
@Table(name = "verification_token" , schema = "public")
public class EmailVerificationToken {
    private static final int EXPIRATION = 60 * 24;

    @Id
    @NotBlank
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "token")
    @NotBlank
    private String token;

    @NotBlank
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "id", nullable = false)
    private User user;

    @NotBlank
    @Column(name = "created_date")
    private Date createdDate;

    @NotBlank
    @Column(name = "expiry_date")
    private Date expiryDate;

    public EmailVerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public EmailVerificationToken() {

    }

    public static int getEXPIRATION() {
        return EXPIRATION;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }


    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        // calendar.add(Calendar.MINUTE, expiryTimeInMinutes);
        // calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(calendar.getTime().getTime());
    }
}