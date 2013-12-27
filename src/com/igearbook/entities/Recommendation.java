package com.igearbook.entities;

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

import net.jforum.entities.User;

/**
 * 
 * @author Chesley
 * 
 */
@Entity
@Table(name = "jforum_recommendation")
public class Recommendation implements Serializable {
    private static final long serialVersionUID = -52490539424260507L;

    public static final int TYPE_INDEX_IMG = 0;
    public static final int TYPE_INDEX_TEAM = 1;

    private int id;
    private int type;
    private String imageUrl;
    private int topicId;
    private String title;
    private String desc;
    private User createBy;
    private User lastUpdateBy;
    private Date createTime;
    private Date lastUpdateTime;

    public Recommendation() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommend_id")
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    @Column(name = "recommend_type")
    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    @Column(name = "image_url")
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Column(name = "topic_id")
    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(final int topicId) {
        this.topicId = topicId;
    }

    @Column(name = "topic_title")
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @Column(name = "topic_desc")
    public String getDesc() {
        return desc;
    }

    public void setDesc(final String desc) {
        this.desc = desc;
    }

    @ManyToOne
    @JoinColumn(name = "create_user_id")
    public User getCreateBy() {
        return createBy;
    }

    public void setCreateBy(final User createBy) {
        this.createBy = createBy;
    }

    @ManyToOne
    @JoinColumn(name = "last_update_user_id")
    public User getLastUpdateBy() {
        return lastUpdateBy;
    }

    public void setLastUpdateBy(final User lastUpdateBy) {
        this.lastUpdateBy = lastUpdateBy;
    }

    @Column(name = "create_time")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(final Date createTime) {
        this.createTime = createTime;
    }

    @Column(name = "last_update_time")
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(final Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

}
