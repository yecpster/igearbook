package com.igearbook.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.igearbook.dao.CustomUrlDao;
import com.igearbook.entities.CustomUrl;
import com.igearbook.entities.PaginationData;

@Namespace("/url")
public class CustomUrlAction extends BaseAction {
    private static final long serialVersionUID = 7587622153127430L;
    @Autowired
    private CustomUrlDao customUrlDao;
    private PaginationData<CustomUrl> data;
    private CustomUrl url;
    private int type;

    public void setCustomUrlDao(final CustomUrlDao customUrlDao) {
        this.customUrlDao = customUrlDao;
    }

    @Action(value = "list", results = { @Result(name = SUCCESS, location = "url_list.ftl") })
    public String list() {
        data = customUrlDao.doPagination(getPaginationParams());
        return SUCCESS;
    }

    @Action(value = "add", results = { @Result(name = SUCCESS, location = "url_form.ftl") })
    public String add() {
        return SUCCESS;
    }

    @Action(value = "save", results = { @Result(name = SUCCESS, location = "list.action", type = "redirect") })
    public String save() {
        customUrlDao.add(url);
        return SUCCESS;
    }

    public PaginationData<CustomUrl> getData() {
        return data;
    }

    public void setData(final PaginationData<CustomUrl> data) {
        this.data = data;
    }

    public CustomUrl getUrl() {
        return url;
    }

    public void setUrl(final CustomUrl url) {
        this.url = url;
    }

    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

}
