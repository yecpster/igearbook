package com.igearbook.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.jforum.entities.User;

import com.igearbook.constant.UserAPISource;

@Entity
public class UserApi implements Serializable {
    private static final long serialVersionUID = 1331358382208728287L;
    private int id;
    private String openId;
    private User user;
    private UserAPISource source;
    private String accessToken;
    private long tokenExpireIn;
    private Date lastUpdateDate;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(final String openId) {
        this.openId = openId;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public UserAPISource getSource() {
        return source;
    }

    public void setSource(final UserAPISource source) {
        this.source = source;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * In millisecond.
     * 
     * @return
     */
    public long getTokenExpireIn() {
        return tokenExpireIn;
    }

    public void setTokenExpireIn(final long tokenExpireIn) {
        this.tokenExpireIn = tokenExpireIn;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(final Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

}
