package com.igearbook.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.igearbook.constant.UrlType;

/**
 * 
 * @author Chesley
 * 
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "type", "moduleId" }) })
public class CustomUrl implements Serializable {
    private static final long serialVersionUID = -52490539424260507L;

    private int id;
    private UrlType type;
    private String url;
    private int moduleId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public UrlType getType() {
        return type;
    }

    public void setType(final UrlType type) {
        this.type = type;
    }

    @Column(unique = true)
    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(final int moduleId) {
        this.moduleId = moduleId;
    }

}
