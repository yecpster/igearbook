package com.igearbook.action;

import org.apache.struts2.ServletActionContext;

import com.igearbook.entities.PaginationParams;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public abstract class BaseAction extends ActionSupport {
    private static final long serialVersionUID = -7109541755686605891L;

    public static final String PERMISSION = "permission";

    protected ActionContext context = ServletActionContext.getContext();

    private int id;
    private int start;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getStart() {
        return start;
    }

    public void setStart(final int start) {
        this.start = start;
    }

    protected PaginationParams getPaginationParams() {
        final PaginationParams params = new PaginationParams();
        params.setStart(this.getStart());
        return params;
    }

}
