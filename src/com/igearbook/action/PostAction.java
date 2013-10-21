package com.igearbook.action;

import org.apache.struts2.convention.annotation.Namespace;

@Namespace("/post")
public class PostAction extends BaseAction {

    private static final long serialVersionUID = 8123243588174068662L;

    public String setSticky() {

        return SUCCESS;
    }
}
