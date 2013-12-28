package com.igearbook.action;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.struts2.ServletActionContext;

import com.google.common.collect.Maps;
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
        final Map<String, Object> webParams = ActionContext.getContext().getParameters();
        // PropertyUtils.isReadable(bean, name)
        final Map<String, Object> queryParams = Maps.newHashMap();
        for (final Entry<String, Object> entry : webParams.entrySet()) {
            if ("start".equals(entry.getKey())) {
                continue;
            }
            queryParams.put(entry.getKey(), entry.getValue());
        }
        params.setQueryParams(queryParams);
        return params;
    }
}
