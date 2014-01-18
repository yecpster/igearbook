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
 * This file creating date: Feb 23, 2003 / 12:40:13 PM
 * The JForum Project
 * http://www.jforum.net
 */
package net.jforum.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

/**
 * Represents every topic in the forum.
 * 
 * @author Rafael Steil
 * @version $Id: Topic.java,v 1.18 2007/08/01 22:09:03 rafaelsteil Exp $
 */
@Entity
@Table(name = "jforum_topics")
public class Topic implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_STICKY = 1;
    public static final int TYPE_ANNOUNCE = 2;
    public static final int TYPE_GOOD = 3;

    public static final int STATUS_UNLOCKED = 0;
    public static final int STATUS_LOCKED = 1;

    private int id;
    private int forumId;
    private Forum forum;
    private int totalViews;
    private int totalReplies;
    private int status;
    private int type;
    private int firstPostId;
    private Post firstPost;
    private int lastPostId;
    private Post lastPost;
    private int voteId;
    private int movedId;

    private boolean read = true;
    private boolean moderated;
    private boolean isHot;
    private boolean hasAttach;
    private boolean paginate;

    private String firstPostTime;
    private String lastPostTime;
    private String title;

    private Date time;
    private Date lastPostDate;

    private Double totalPages;

    private User postedBy;
    private User lastPostBy;

    public Topic() {
    }

    public Topic(final int topicId) {
        this.id = topicId;
    }

    /**
     * Returns the ID of the firts topic
     * 
     * @return int value with the ID
     */
    @Column(name = "topic_first_post_id")
    public int getFirstPostId() {
        return this.firstPostId;
    }

    @Transient
    public Post getFirstPost() {
        return firstPost;
    }

    public void setFirstPost(final Post firstPost) {
        this.firstPost = firstPost;
    }

    @Transient
    public Post getLastPost() {
        return lastPost;
    }

    public void setLastPost(final Post lastPost) {
        this.lastPost = lastPost;
    }

    /**
     * Returns the ID of the topic
     * 
     * @return int value with the ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    public int getId() {
        return this.id;
    }

    /**
     * Returns the ID of the forum this topic belongs to
     * 
     * @return int value with the ID
     */
    @Column(name = "forum_id")
    public int getForumId() {
        return this.forumId;
    }

    @Transient
    public Forum getForum() {
        return forum;
    }

    public void setForum(final Forum forum) {
        this.forum = forum;
    }

    /**
     * Teturns the ID of the last post in the topic
     * 
     * @return int value with the ID
     */
    @Column(name = "topic_last_post_id")
    public int getLastPostId() {
        return this.lastPostId;
    }

    /**
     * Returns the status
     * 
     * @return int value with the status
     */
    @Column(name = "topic_status")
    public int getStatus() {
        return this.status;
    }

    /**
     * Returns the time the topic was posted
     * 
     * @return int value representing the time
     */
    @Column(name = "topic_time")
    public Date getTime() {
        return this.time;
    }

    public void setFirstPostTime(final String d) {
        this.firstPostTime = d;
    }

    public void setLastPostTime(final String d) {
        this.lastPostTime = d;
    }

    /**
     * Returns the title of the topci
     * 
     * @return String with the topic title
     */
    @Column(name = "topic_title")
    public String getTitle() {
        return (this.title == null ? "" : this.title);
    }

    /**
     * Returns the total number of replies
     * 
     * @return int value with the total
     */
    @Column(name = "topic_replies")
    public int getTotalReplies() {
        return this.totalReplies;
    }

    /**
     * Returns the total number of views
     * 
     * @return int value with the total number of views
     */
    @Column(name = "topic_views")
    public int getTotalViews() {
        return this.totalViews;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User getPostedBy() {
        return this.postedBy;
    }

    @Transient
    public User getLastPostBy() {
        return this.lastPostBy;
    }

    /**
     * Returns the type
     * 
     * @return int value representing the type
     */
    @Column(name = "topic_type")
    public int getType() {
        return this.type;
    }

    /**
     * Is a votation topic?
     * 
     * @return boolean value
     */
    @Transient
    public boolean isVote() {
        return this.voteId != 0;
    }

    /**
     * Is a votation topic? If so, this is the vote Id
     * 
     * @return boolean value
     */
    @Column(name = "topic_vote_id")
    public int getVoteId() {
        return this.voteId;
    }

    /**
     * Sets the id of the firts post in the topic
     * 
     * @param firstPostId
     *            The post id
     */
    public void setFirstPostId(final int firstPostId) {
        this.firstPostId = firstPostId;
    }

    /**
     * Sets the id to the topic
     * 
     * @param id
     *            The id to set
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Sets the id of the forum associeted with this topic
     * 
     * @param idForum
     *            The id of the forum to set
     */
    public void setForumId(final int idForum) {
        this.forumId = idForum;
    }

    /**
     * Sets the id of the last post in the topic
     * 
     * @param lastPostId
     *            The post id
     */
    public void setLastPostId(final int lastPostId) {
        this.lastPostId = lastPostId;
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set
     */
    public void setStatus(final int status) {
        this.status = status;
    }

    /**
     * Sets the time.
     * 
     * @param time
     *            The time to set
     */
    public void setTime(final Date time) {
        this.time = time;
    }

    /**
     * Sets the title.
     * 
     * @param title
     *            The title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Sets the totalReplies.
     * 
     * @param totalReplies
     *            The totalReplies to set
     */
    public void setTotalReplies(final int totalReplies) {
        this.totalReplies = totalReplies;
    }

    /**
     * Sets the totalViews.
     * 
     * @param totalViews
     *            The totalViews to set
     */
    public void setTotalViews(final int totalViews) {
        this.totalViews = totalViews;
    }

    /**
     * Sets the type.
     * 
     * @param type
     *            The type to set
     */
    public void setType(final int type) {
        this.type = type;
    }

    /**
     * Sets the voteId.
     * 
     * @param voteId
     *            The voteId to set
     */
    public void setVoteId(final int voteId) {
        this.voteId = voteId;
    }

    /**
     * @return
     */
    @Column(name = "moderated", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public boolean isModerated() {
        return this.moderated;
    }

    /**
     * @param b
     */
    public void setModerated(final boolean b) {
        this.moderated = b;
    }

    public void setPostedBy(final User u) {
        this.postedBy = u;
    }

    public void setLastPostBy(final User u) {
        this.lastPostBy = u;
    }

    @Transient
    public String getFirstPostTime() {
        return this.firstPostTime;
    }

    @Transient
    public String getLastPostTime() {
        return this.lastPostTime;
    }

    public void setRead(final boolean read) {
        this.read = read;
    }

    @Transient
    public boolean getRead() {
        return this.read;
    }

    public void setLastPostDate(final Date t) {
        this.lastPostDate = t;
    }

    @Transient
    public Date getLastPostDate() {
        return this.lastPostDate;
    }

    public void setPaginate(final boolean paginate) {
        this.paginate = paginate;
    }

    @Transient
    public boolean getPaginate() {
        return this.paginate;
    }

    public void setTotalPages(final Double total) {
        this.totalPages = total;
    }

    @Transient
    public Double getTotalPages() {
        return this.totalPages;
    }

    public void setHot(final boolean hot) {
        this.isHot = hot;
    }

    @Transient
    public boolean isHot() {
        return this.isHot;
    }

    public void setHasAttach(final boolean b) {
        this.hasAttach = b;
    }

    @Transient
    public boolean hasAttach() {
        return this.hasAttach;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @Transient
    public boolean equals(final Object o) {
        if (!(o instanceof Topic)) {
            return false;
        }

        return (((Topic) o).getId() == this.id);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    @Transient
    public int hashCode() {
        return this.id;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    @Transient
    public String toString() {
        return "[" + this.id + ", " + this.title + "]";
    }

    /**
     * @return the movedId
     */
    @Column(name = "topic_moved_id")
    public int getMovedId() {
        return this.movedId;
    }

    /**
     * @param movedId
     *            the movedId to set
     */
    public void setMovedId(final int movedId) {
        this.movedId = movedId;
    }
}
