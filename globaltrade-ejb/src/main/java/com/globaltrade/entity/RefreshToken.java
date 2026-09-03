package com.globaltrade.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "refresh_token",
        indexes = {
                @Index(columnList = "token", unique = true),
                @Index(columnList = "username"),
        })
@NamedQueries({
        @NamedQuery(name = "RefreshToken.findByValidToken",
                query = "SELECT rf FROM RefreshToken rf WHERE rf.token=:token AND rf.expiryAt > :now"),
        @NamedQuery(name = "RefreshToken.deleteToken",
                query = "DELETE FROM RefreshToken rf WHERE rf.token=:token"),
        @NamedQuery(name = "RefreshToken.deleteExpiredToken",
                query = "DELETE FROM RefreshToken rf WHERE rf.expiryAt < :now")
})
public class RefreshToken implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token;

    private String username;
    private Instant expiryAt;
    private Instant createdAt = Instant.now();

    public RefreshToken() {
    }

    public RefreshToken(String token, String username, Instant expiryAt) {
        this.token = token;
        this.username = username;
        this.expiryAt = expiryAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getExpiryAt() {
        return expiryAt;
    }

    public void setExpiryAt(Instant expiryAt) {
        this.expiryAt = expiryAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
