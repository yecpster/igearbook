package com.igearbook.action;

import com.igearbook.entities.PaginationParams;
import com.opensymphony.xwork2.ActionSupport;

public abstract class BaseAction extends ActionSupport {
    private static final long serialVersionUID = -7109541755686605891L;

    public static final String PERMITION = "permition";

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    protected PaginationParams getPaginationParams() {
        return null;
    }

}
