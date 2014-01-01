package net.jforum.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.jforum.view.forum.common.ViewCommon;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "jforum_posts")
@SecondaryTable(name = "jforum_posts_text")
public class Post implements Serializable {
    private static final long serialVersionUID = -6067049478602005132L;
    private int id;
    private int topicId;
    private int forumId;
    private String formatedTime;
    private int userId;
    private Date time;
    private String text;
    private String subject;
    private String postUsername;
    private boolean bbCodeEnabled = true;
    private boolean htmlEnabled = true;
    private boolean smiliesEnabled = true;
    private boolean signatureEnabled = true;
    private Date editTime;
    private int editCount;
    private String userIp;
    private boolean canEdit;
    private KarmaStatus karma;
    private boolean hasAttachments;
    private boolean moderate;

    public Post() {
    }

    public Post(final int postId) {
        this.id = postId;
    }

    /**
     * Copy constructor
     * 
     * @param p
     *            The Post to make a copy from
     */
    public Post(final Post p) {
        this.setBbCodeEnabled(p.isBbCodeEnabled());
        this.setCanEdit(p.getCanEdit());
        this.setEditCount(p.getEditCount());
        this.setEditTime(p.getEditTime());
        this.setFormatedTime(p.getFormatedTime());
        this.setForumId(p.getForumId());
        this.setHtmlEnabled(p.isHtmlEnabled());
        this.setId(p.getId());
        this.setPostUsername(p.getPostUsername());
        this.setSignatureEnabled(p.isSignatureEnabled());
        this.setSmiliesEnabled(p.isSmiliesEnabled());
        this.setSubject(p.getSubject());
        this.setText(p.getText());
        this.setTime(p.getTime());
        this.setTopicId(p.getTopicId());
        this.setUserId(p.getUserId());
        this.setUserIp(p.getUserIp());
        this.setKarma(new KarmaStatus(p.getKarma()));
        this.setModerate(p.isModerate());
        this.hasAttachments(p.hasAttachments());
    }

    public void setModerate(final boolean status) {
        this.moderate = status;
    }

    @Column(name = "need_moderate", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public boolean isModerate() {
        return this.moderate;
    }

    @Transient
    public KarmaStatus getKarma() {
        return this.karma;
    }

    public void setKarma(final KarmaStatus karma) {
        this.karma = karma;
    }

    /**
     * Checks if the BB code is enabled
     * 
     * @return boolean value representing the result
     */
    @Column(name = "enable_bbcode", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public boolean isBbCodeEnabled() {
        return this.bbCodeEnabled;
    }

    /**
     * Gets the total number of times the post was edited
     * 
     * @return int value with the total number of times the post was edited
     */
    @Column(name = "post_edit_count")
    public int getEditCount() {
        return this.editCount;
    }

    /**
     * Gets the edit time of the post
     * 
     * @return long value representing the time
     */
    @Column(name = "post_edit_time")
    public Date getEditTime() {
        return this.editTime;
    }

    /**
     * Gets the forum's id the post is associated
     * 
     * @return int value with the id of the forum
     */
    @Column(name = "forum_id")
    public int getForumId() {
        return this.forumId;
    }

    /**
     * Checks if HTML is enabled in the topic
     * 
     * @return boolean value representing the result
     */
    @Column(name = "enable_html", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public boolean isHtmlEnabled() {
        return this.htmlEnabled;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    public int getId() {
        return this.id;
    }

    /**
     * Gets the username of the user ( an anonymous user ) that have posted the message
     * 
     * @return String with the username
     */
    @Transient
    public String getPostUsername() {
        return this.postUsername;
    }

    /**
     * Checks if signature is allowd in the message
     * 
     * @return boolean representing the result
     */
    @Column(name = "enable_sig", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public boolean isSignatureEnabled() {
        return this.signatureEnabled;
    }

    /**
     * Checks if smart Smilies are enabled :)
     * 
     * @return boolean representing the result
     */
    @Column(name = "enable_smilies", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public boolean isSmiliesEnabled() {
        return this.smiliesEnabled;
    }

    /**
     * Gets the time, represented as long, of the message post
     * 
     * @return long representing the post time
     */
    @Column(name = "post_time")
    public Date getTime() {
        return this.time;
    }

    /**
     * Gets the id of the topic this message is associated
     * 
     * @return int value with the topic id
     */
    @Column(name = "topic_id")
    public int getTopicId() {
        return this.topicId;
    }

    /**
     * Gets the ID of the user that have posted the message
     * 
     * @return int value with the user id
     */
    @Column(name = "user_id")
    public int getUserId() {
        return this.userId;
    }

    /**
     * Gets the IP of the user who have posted the message
     * 
     * @return String value with the user IP
     */
    @Column(name = "poster_ip")
    public String getUserIp() {
        return this.userIp;
    }

    /**
     * Sets the status for BB code in the message
     * 
     * @param bbCodeEnabled
     *            <code>true</code> or <code>false</code>, depending the intention
     */
    public void setBbCodeEnabled(final boolean bbCodeEnabled) {
        this.bbCodeEnabled = bbCodeEnabled;
    }

    /**
     * Sets the count times the message was edited
     * 
     * @param editCount
     *            The count time
     */
    public void setEditCount(final int editCount) {
        this.editCount = editCount;
    }

    /**
     * Sets the edit time the message was last edited
     * 
     * @param editTime
     *            long value representing the time
     */
    public void setEditTime(final Date editTime) {
        this.editTime = editTime;
    }

    /**
     * Sets the id of the forum this message belongs to
     * 
     * @param forumId
     *            The forum's id
     */
    public void setForumId(final int forumId) {
        this.forumId = forumId;
    }

    /**
     * Sets the status for HTML code in the message
     * 
     * @param htmlEnabled
     *            <code>true</code> or <code>false</code>, depending the intention
     */
    public void setHtmlEnabled(final boolean htmlEnabled) {
        this.htmlEnabled = htmlEnabled;
    }

    /**
     * Sets the id for the message
     * 
     * @param id
     *            The id
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Sets the username of the anonymous user that have sent the message
     * 
     * @param postUsername
     *            String with the username
     */
    public void setPostUsername(final String postUsername) {
        this.postUsername = postUsername;
    }

    /**
     * Sets the status for signatures in the message
     * 
     * @param signatureEnabled
     *            <code>true</code> or <code>false</code>, depending the intention
     */
    public void setSignatureEnabled(final boolean signatureEnabled) {
        this.signatureEnabled = signatureEnabled;
    }

    /**
     * Sets the status for smilies in the message
     * 
     * @param smiliesEnabled
     *            <code>true</code> or <code>false</code>, depending the intention
     */
    public void setSmiliesEnabled(final boolean smiliesEnabled) {
        this.smiliesEnabled = smiliesEnabled;
    }

    /**
     * Sets the time the message was sent
     * 
     * @param time
     *            The time
     */
    public void setTime(final Date time) {
        this.time = time;
    }

    public void setFormatedTime(final String t) {
        this.formatedTime = t;
    }

    @Transient
    public String getFormatedTime() {
        if (this.formatedTime == null && this.time != null) {
            this.formatedTime = ViewCommon.formatDate(this.time);
        }

        return this.formatedTime;
    }

    /**
     * Sets the id of the topic that the message belongs to
     * 
     * @param topicId
     *            The id of the topic
     */
    public void setTopicId(final int topicId) {
        this.topicId = topicId;
    }

    /**
     * Sets the id of the user that sent the message
     * 
     * @param userId
     *            The user Id
     */
    public void setUserId(final int userId) {
        this.userId = userId;
    }

    /**
     * Gets the message of the post
     * 
     * @return String containing the text
     */
    @Column(name = "post_text", table = "jforum_posts_text")
    @Lob
    public String getText() {
        return this.text;
    }

    /**
     * Sets the text of the post
     * 
     * @param text
     *            The text to set
     */
    public void setText(final String text) {
        this.text = text;
    }

    /**
     * Gets the subject of the post
     * 
     * @return String with the subject
     */
    @Column(name = "post_subject", table = "jforum_posts_text")
    public String getSubject() {
        return this.subject;
    }

    /**
     * Sets the subject for the message
     * 
     * @param subject
     *            The subject to set
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * Sets the IP of the user
     * 
     * @param userIP
     *            The IP address of the user
     */
    public void setUserIp(final String userIp) {
        this.userIp = userIp;
    }

    @Transient
    public boolean getCanEdit() {
        return this.canEdit;
    }

    public void setCanEdit(final boolean canEdit) {
        this.canEdit = canEdit;
    }

    /**
     * @return Returns the hasAttachments.
     */
    @Column(name = "attach", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public boolean hasAttachments() {
        return this.hasAttachments;
    }

    /**
     * @param hasAttachments
     *            The hasAttachments to set.
     */
    public void hasAttachments(final boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @Transient
    public boolean equals(final Object o) {
        if (!(o instanceof Post)) {
            return false;
        }

        return ((Post) o).getId() == this.id;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    @Transient
    public int hashCode() {
        return this.id;
    }
}
