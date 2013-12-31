package com.igearbook.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 
 * @author Chesley
 * 
 */
@Entity
public class CustomUrl implements Serializable {
    private static final long serialVersionUID = -52490539424260507L;

    private int id;
    private int type;
    private String url;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    @Column(unique = true)
    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

}
