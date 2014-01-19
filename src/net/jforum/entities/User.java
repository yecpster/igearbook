/*
 * Copyright (c) JForum Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, 
 * with or without modification, are permitted provided 
 * that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above 
 * copyright notice, this list of conditions and the 
 * following  disclaimer.
 * 2)  Redistributions in binary form must reproduce the 
 * above copyright notice, this list of conditions and 
 * the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * 3) Neither the name of "Rafael Steil" nor 
 * the names of its contributors may be used to endorse 
 * or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 * 
 * This file creating date: Feb 17, 2003 / 10:25:04 PM
 * The JForum Project
 * http://www.jforum.net 
 * 
 * $Id: User.java,v 1.22 2008/03/14 02:56:08 andowson Exp $
 */
package net.jforum.entities;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.jforum.SessionFacade;
import net.jforum.repository.SecurityRepository;
import net.jforum.security.SecurityConstants;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

import org.hibernate.annotations.Type;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Represents a single user in the system. An user is every person which uses the forum. Well, every registered user. Anonymous users does not have a
 * specific ID, for example. This class contains all information about some user configuration options and preferences.
 * 
 * @author Rafael Steil
 */
@Entity
@Table(name = "jforum_users")
public class User implements Serializable {
    private static final long serialVersionUID = 7698867048764582802L;

    private int id;
    private int themeId;
    private int level;
    private int totalPosts;
    private boolean attachSignatureEnabled = true;
    private int rankId = 1;
    private boolean htmlEnabled = true;
    private boolean bbCodeEnabled = true;
    private boolean smiliesEnabled = true;
    private boolean avatarEnabled = true;
    private boolean privateMessagesEnabled = true;
    private boolean viewOnlineEnabled = true;
    private boolean notifyPrivateMessagesEnabled = true;
    private boolean notifyOnMessagesEnabled = true;
    private boolean notifyAlways;
    private boolean notifyText;
    private String username;
    private String password;
    private Date lastVisit;
    private Date registrationDate;
    private String avatar;
    private boolean isExternalAvatar;
    private String email;
    private String icq;
    private String webSite;
    private String from;
    private String signature;
    private String aim;
    private String yim;
    private String msnm;
    private String occupation;
    private String interests;
    private String biography;
    private String gender;
    private String timeZone;
    private String lang;
    private String dateFormat;
    private boolean viewEmailEnabled = true;
    private List<Group> groupsList;
    private int privateMessagesCount;
    private KarmaStatus karma;
    private double karmaColumn;
    private boolean active;
    private boolean apiUser;
    private boolean apiUserActive;
    private String activationKey;
    private boolean deleted;
    private String firstName;
    private String lastName;
    private String registerIp;
    private final Map<String, Object> extra = Maps.newHashMap();

    public User(final int userId) {
        this.id = userId;
    }

    /**
     * Default Constructor
     */
    public User() {
        this.groupsList = Lists.newArrayList();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    public int getId() {
        return this.id;
    }

    public void addExtra(final String name, final Object value) {
        this.extra.put(name, value);
    }

    @Transient
    public Object getExtra(final String name) {
        return this.extra.get(name);
    }

    public void setFirstName(final String name) {
        this.firstName = name;
    }

    @Transient
    public String getFirstName() {
        return this.firstName;
    }

    public void setLastName(final String name) {
        this.lastName = name;
    }

    @Transient
    public String getLastName() {
        return this.lastName;
    }

    @Transient
    public String getName() {
        return this.firstName + " " + this.lastName;
    }

    @Column(name = "deleted", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    @Transient
    public String getAim() {
        return this.aim;
    }

    @Column(name = "user_avatar")
    public String getAvatar() {
        return this.avatar;
    }

    @Transient
    public boolean isAvatarEnabled() {
        return this.avatarEnabled;
    }

    @Transient
    public boolean isBbCodeEnabled() {
        return this.bbCodeEnabled;
    }

    @Transient
    public String getDateFormat() {
        return this.dateFormat;
    }

    @Column(name = "user_email")
    public String getEmail() {
        return this.email;
    }

    @Column(name = "user_from")
    public String getFrom() {
        return this.from;
    }

    @Column(name = "reg_ip")
    public String getRegisterIp() {
        return registerIp;
    }

    public void setRegisterIp(final String registerIp) {
        this.registerIp = registerIp;
    }

    @Transient
    public String getGender() {
        return this.gender;
    }

    @Transient
    public boolean isHtmlEnabled() {
        return this.htmlEnabled;
    }

    @Transient
    public String getIcq() {
        return this.icq;
    }

    @Transient
    public String getInterests() {
        return this.interests;
    }

    @Transient
    public String getLang() {
        return this.lang;
    }

    @Transient
    public Date getLastVisit() {
        return this.lastVisit;
    }

    @Transient
    public int getLevel() {
        return this.level;
    }

    @Transient
    public boolean isNotifyPrivateMessagesEnabled() {
        return this.notifyPrivateMessagesEnabled;
    }

    @Transient
    public String getOccupation() {
        return this.occupation;
    }

    @Column(name = "user_password")
    public String getPassword() {
        return this.password;
    }

    @Transient
    public boolean isPrivateMessagesEnabled() {
        return this.privateMessagesEnabled;
    }

    @Transient
    public int getRankId() {
        return this.rankId;
    }

    @Column(name = "user_regdate")
    public Date getRegistrationDate() {
        return registrationDate;
    }

    @Column(name = "apiUser", columnDefinition = "bit default false")
    public boolean isApiUser() {
        return apiUser;
    }

    public void setApiUser(final boolean apiUser) {
        this.apiUser = apiUser;
    }

    @Column(name = "apiUserActive", columnDefinition = "bit default false")
    public boolean isApiUserActive() {
        return apiUserActive;
    }

    public void setApiUserActive(final boolean apiUserActive) {
        this.apiUserActive = apiUserActive;
    }

    @Transient
    public String getRegDateString() {
        final SimpleDateFormat df = new SimpleDateFormat(SystemGlobals.getValue(ConfigKeys.DATE_TIME_FORMAT));
        return df.format(this.registrationDate);
    }

    @Transient
    public String getSignature() {
        return this.signature;
    }

    @Transient
    public boolean isSmiliesEnabled() {
        return this.smiliesEnabled;
    }

    @Transient
    public int getThemeId() {
        return this.themeId;
    }

    @Transient
    public String getTimeZone() {
        return this.timeZone;
    }

    /**
     * Gets the total number of messages posted by the user
     * 
     * @return int value with the total of messages
     */
    @Column(name = "user_posts")
    public int getTotalPosts() {
        return this.totalPosts;
    }

    @Column(name = "username")
    public String getUsername() {
        return this.username;
    }

    /**
     * Checks if the user permits other people to see he online
     * 
     * @return boolean value
     */
    @Transient
    public boolean isViewOnlineEnabled() {
        return this.viewOnlineEnabled;
    }

    @Column(name = "user_website")
    public String getWebSite() {
        return this.webSite;
    }

    @Transient
    public String getYim() {
        return this.yim;
    }

    /**
     * Is the user's email authenticated?
     * 
     * @return integer 1 if true
     */
    @Column(name = "user_active", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public boolean isActive() {
        return active;
    }

    /**
     * Gets the Yahoo messenger ID
     * 
     * @return String with the activation key that is created during user registration
     */
    @Transient
    public String getActivationKey() {
        return this.activationKey;
    }

    /**
     * Sets the aim.
     * 
     * @param aim
     *            The aim ID to set
     */
    public void setAim(final String aim) {
        this.aim = aim;
    }

    /**
     * Sets the avatar.
     * 
     * @param avatar
     *            The avatar to set
     */
    public void setAvatar(final String avatar) {
        this.avatar = avatar;

        if (avatar != null && avatar.toLowerCase().startsWith("http://")) {
            this.isExternalAvatar = true;
        }
    }

    /**
     * Indicates if the avatar points to an external URL
     * 
     * @return <code>true</code> if the avatar is some external image
     */
    @Transient
    public boolean isExternalAvatar() {
        return this.isExternalAvatar;
    }

    /**
     * Sets avatar status
     * 
     * @param avatarEnabled
     *            <code>true</code> or <code>false</code>
     */
    public void setAvatarEnabled(final boolean avatarEnabled) {
        this.avatarEnabled = avatarEnabled;
    }

    /**
     * Sets the status for BB codes
     * 
     * @param bbCodeEnabled
     *            <code>true</code> or <code>false</code>
     */
    public void setBbCodeEnabled(final boolean bbCodeEnabled) {
        this.bbCodeEnabled = bbCodeEnabled;
    }

    /**
     * Sets the date format.
     * 
     * @param dateFormat
     *            The date format to set
     */
    public void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Sets the email.
     * 
     * @param email
     *            The email to set
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Sets the user location ( where he lives )
     * 
     * @param from
     *            The location
     */
    public void setFrom(final String from) {
        this.from = from;
    }

    /**
     * Sets the gender.
     * 
     * @param gender
     *            The gender to set. Possible values must be <code>M</code> or <code>F</code>
     */
    public void setGender(final String gender) {
        this.gender = gender;
    }

    /**
     * Enable or not HTML code into the messages
     * 
     * @param htmlEnabled
     *            <code>true</code> or <code>false</code>
     */
    public void setHtmlEnabled(final boolean htmlEnabled) {
        this.htmlEnabled = htmlEnabled;
    }

    /**
     * Sets the icq UIN
     * 
     * @param icq
     *            The icq to set
     */
    public void setIcq(final String icq) {
        this.icq = icq;
    }

    /**
     * Sets the user id.
     * 
     * @param id
     *            The user id to set
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Sets the interests.
     * 
     * @param interests
     *            The interests to set
     */
    public void setInterests(final String interests) {
        this.interests = interests;
    }

    /**
     * Sets the language.
     * 
     * @param lang
     *            The lang to set
     */
    public void setLang(final String lang) {
        this.lang = lang;
    }

    /**
     * Sets the last visit time
     * 
     * @param lastVisit
     *            Last visit time, represented as a long value
     */
    public void setLastVisit(final Date lastVisit) {
        this.lastVisit = lastVisit;
    }

    /**
     * Sets the level.
     * 
     * @param level
     *            The level to set
     */
    public void setLevel(final int level) {
        this.level = level;
    }

    /**
     * Sets the status for notification of new private messages
     * 
     * @param notifyPrivateMessagesEnabled
     *            <code>true</code> or <code>false</code>
     */
    public void setNotifyPrivateMessagesEnabled(final boolean notifyPrivateMessagesEnabled) {
        this.notifyPrivateMessagesEnabled = notifyPrivateMessagesEnabled;
    }

    /**
     * Sets the occ.
     * 
     * @param occ
     *            The occ to set
     */
    public void setOccupation(final String occupation) {
        this.occupation = occupation;
    }

    /**
     * Sets the password.
     * 
     * @param password
     *            The password to set
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Enable or not private messages to the user
     * 
     * @param privateMessagesEnabled
     *            <code>true</code> or <code>false</code>
     */
    public void setPrivateMessagesEnabled(final boolean privateMessagesEnabled) {
        this.privateMessagesEnabled = privateMessagesEnabled;
    }

    /**
     * Sets the ranking id
     * 
     * @param rankId
     *            The id of the ranking
     */
    public void setRankId(final int rankId) {
        this.rankId = rankId;
    }

    /**
     * Sets the registration date.
     * 
     * @param registrationDate
     *            The registration date to set
     */
    public void setRegistrationDate(final Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * Sets the signature.
     * 
     * @param signature
     *            The signature to set
     */
    public void setSignature(final String signature) {
        this.signature = signature;
    }

    /**
     * Enable or not smilies in messages
     * 
     * @param smilesEnabled
     *            <code>true</code> or <code>false</code>
     */
    public void setSmiliesEnabled(final boolean smilesEnabled) {
        this.smiliesEnabled = smilesEnabled;
    }

    /**
     * Sets the theme id
     * 
     * @param themeId
     *            The theme Id to set
     */
    public void setThemeId(final int themeId) {
        this.themeId = themeId;
    }

    /**
     * Sets the Timezone.
     * 
     * @param timeZone
     *            The Timezone to set
     */
    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Sets the total number of posts by the user
     * 
     * @param totalPosts
     *            int value with the total of messages posted by the user
     */
    public void setTotalPosts(final int totalPosts) {
        this.totalPosts = totalPosts;
    }

    /**
     * Sets the username.
     * 
     * @param username
     *            The username to set
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Sets the viewOnlineEnabled.
     * 
     * @param viewOnlineEnabled
     *            The viewOnlineEnabled to set
     */
    public void setViewOnlineEnabled(final boolean viewOnlineEnabled) {
        this.viewOnlineEnabled = viewOnlineEnabled;
    }

    /**
     * Sets the webSite.
     * 
     * @param webSite
     *            The webSite to set
     */
    public void setWebSite(final String webSite) {
        this.webSite = webSite;
    }

    /**
     * Sets the Yahoo messenger ID
     * 
     * @param yim
     *            The yim to set
     */
    public void setYim(final String yim) {
        this.yim = yim;
    }

    /**
     * @return
     */
    @Transient
    public String getMsnm() {
        return this.msnm;
    }

    /**
     * @param string
     */
    public void setMsnm(final String string) {
        this.msnm = string;
    }

    /**
     * @return
     */
    @Transient
    public boolean isNotifyOnMessagesEnabled() {
        return this.notifyOnMessagesEnabled;
    }

    /**
     * @param b
     */
    public void setNotifyOnMessagesEnabled(final boolean b) {
        this.notifyOnMessagesEnabled = b;
    }

    /**
     * @return
     */
    @Column(name = "user_viewemail", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public boolean isViewEmailEnabled() {
        return this.viewEmailEnabled;
    }

    /**
     * @param b
     */
    public void setViewEmailEnabled(final boolean b) {
        this.viewEmailEnabled = b;
    }

    /**
     * @return
     */
    @Transient
    public boolean getAttachSignatureEnabled() {
        return this.attachSignatureEnabled;
    }

    /**
     * @param b
     */
    public void setAttachSignatureEnabled(final boolean b) {
        this.attachSignatureEnabled = b;
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinTable(name = "jforum_user_groups", joinColumns = { @JoinColumn(name = "user_id", nullable = false, updatable = false) },
            inverseJoinColumns = { @JoinColumn(name = "group_id", nullable = false, updatable = false) })
    public List<Group> getGroupsList() {
        return this.groupsList;
    }

    public void setGroupsList(final List<Group> groupsList) {
        this.groupsList = groupsList;
    }

    /**
     * @return Returns the privateMessagesCount.
     */
    @Transient
    public int getPrivateMessagesCount() {
        return this.privateMessagesCount;
    }

    /**
     * @param privateMessagesCount
     *            The privateMessagesCount to set.
     */
    public void setPrivateMessagesCount(final int privateMessagesCount) {
        this.privateMessagesCount = privateMessagesCount;
    }

    /**
     * @return Returns the hasPrivateMessages.
     */
    public boolean hasPrivateMessages() {
        return this.privateMessagesCount > 0;
    }

    /**
     * Set when user authenticates his email after user registration
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setActivationKey(final String activationKey) {
        this.activationKey = activationKey;
    }

    public void setKarma(final KarmaStatus karma) {
        this.karma = karma;
    }

    @Transient
    public KarmaStatus getKarma() {
        return this.karma;
    }

    @Column(name = "user_karma")
    public double getKarmaColumn() {
        return karmaColumn;
    }

    public void setKarmaColumn(final double karmaColumn) {
        final KarmaStatus karmaStatus = new KarmaStatus();
        karmaStatus.setKarmaPoints(karmaColumn);
        setKarma(karma);
        this.karmaColumn = karmaColumn;
    }

    /**
     * Is the user online?
     * 
     * @return true if user is in Session
     */
    @Transient
    public boolean isOnline() {
        return (SessionFacade.isUserInSession(this.id) != null);
    }

    /**
     * Gets the user's biography
     * 
     * @return the user biography
     */
    @Transient
    public String getBiography() {
        return biography;
    }

    /**
     * Sets the user's biography
     * 
     * @param biography
     *            the user's biography
     */
    public void setBiography(final String biography) {
        this.biography = biography;
    }

    /**
     * @return the notifyAlways
     */
    @Transient
    public boolean notifyAlways() {
        return this.notifyAlways;
    }

    /**
     * @return the notifyText
     */
    @Transient
    public boolean notifyText() {
        return this.notifyText;
    }

    /**
     * @param notifyAlways
     *            the notifyAlways to set
     */
    public void setNotifyAlways(final boolean notifyAlways) {
        this.notifyAlways = notifyAlways;
    }

    /**
     * @param notifyText
     *            the notifyText to set
     */
    public void setNotifyText(final boolean notifyText) {
        this.notifyText = notifyText;
    }

    /**
     * Checks if the user is an administrator
     * 
     * @return <code>true</code> if the user is an administrator
     */
    @Transient
    public boolean isAdmin() {
        return SecurityRepository.canAccess(this.id, SecurityConstants.PERM_ADMINISTRATION);
    }

    /**
     * Checks if the user is a moderator
     * 
     * @return <code>true</code> if the user has moderations rights
     */
    @Transient
    public boolean isSuperModerator() {
        return SecurityRepository.canAccess(this.id, SecurityConstants.PERM_SUPER_MODERATION);
    }
}
