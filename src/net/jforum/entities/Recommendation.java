package net.jforum.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author Chesley
 * 
 */
public class Recommendation implements Serializable {
    private static final long serialVersionUID = -52490539424260507L;

    public static final int TYPE_INDEX_IMG = 0;

    private int id = TYPE_INDEX_IMG;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public User getCreateBy() {
        return createBy;
    }

    public void setCreateBy(User createBy) {
        this.createBy = createBy;
    }

    public User getLastUpdateBy() {
        return lastUpdateBy;
    }

    public void setLastUpdateBy(User lastUpdateBy) {
        this.lastUpdateBy = lastUpdateBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

}
