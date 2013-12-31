package net.jforum.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.jforum.repository.ForumRepository;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "jforum_forums")
public class Forum implements Serializable {
    private static final long serialVersionUID = -5566145240758297008L;
    public static final int TYPE_PUBLIC = 0;
    public static final int TYPE_TEAM = 1;

    private int id;
    private int categoryId;
    private int type = 0; // 0 - public forum, 1 - team forum
    private String name;
    private String uri;
    private String logo;
    private String description;
    private int order;
    private int totalTopics;
    private int totalPosts;
    private int lastPostId;
    private boolean moderated;
    private boolean unread;
    private LastPostInfo lastPostInfo;

    public Forum() {
    }

    public Forum(final int forumId) {
        this.id = forumId;
    }

    public Forum(final Forum f) {
        this.description = f.getDescription();
        this.id = f.getId();
        this.categoryId = f.getCategoryId();
        this.type = f.getType();
        this.lastPostId = f.getLastPostId();
        this.moderated = f.isModerated();
        this.name = f.getName();
        this.logo = f.getLogo();
        this.order = f.getOrder();
        this.totalPosts = f.getTotalPosts();
        this.totalTopics = f.getTotalTopics();
        this.unread = f.isUnread();
        this.lastPostInfo = f.getLastPostInfo();
    }

    @Transient
    public List<ModeratorInfo> getModeratorList() {
        return ForumRepository.getModeratorList(this.id);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "forum_id")
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    @Column(name = "categories_id")
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(final int categoryId) {
        this.categoryId = categoryId;
    }

    @Column(name = "forum_type")
    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    @Column(name = "forum_name")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Column(name = "forum_uri")
    public String getUri() {
        return uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    @Column(name = "forum_logo")
    public String getLogo() {
        return logo;
    }

    public void setLogo(final String logo) {
        this.logo = logo;
    }

    @Column(name = "forum_desc")
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Column(name = "forum_order")
    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    @Column(name = "forum_topics")
    public int getTotalTopics() {
        return totalTopics;
    }

    public void setTotalTopics(final int totalTopics) {
        this.totalTopics = totalTopics;
    }

    @Transient
    public int getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(final int totalPosts) {
        this.totalPosts = totalPosts;
    }

    @Column(name = "forum_last_post_id")
    public int getLastPostId() {
        return lastPostId;
    }

    public void setLastPostId(final int lastPostId) {
        this.lastPostId = lastPostId;
    }

    @Column(name = "moderated", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public boolean isModerated() {
        return moderated;
    }

    public void setModerated(final boolean moderated) {
        this.moderated = moderated;
    }

    @Transient
    public boolean isUnread() {
        return unread;
    }

    public void setUnread(final boolean unread) {
        this.unread = unread;
    }

    @Transient
    public LastPostInfo getLastPostInfo() {
        return lastPostInfo;
    }

    public void setLastPostInfo(final LastPostInfo lastPostInfo) {
        this.lastPostInfo = lastPostInfo;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @Transient
    public boolean equals(final Object o) {
        return ((o instanceof Forum) && (((Forum) o).getId() == this.id));
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
        return "[" + this.name + ", id=" + this.id + ", order=" + this.order + "]";
    }

}
