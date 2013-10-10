package com.igearbook.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.Namespace;

import com.opensymphony.xwork2.ActionSupport;
@InterceptorRefs({
    @InterceptorRef("defaultStackIgearbook")
})
@Namespace("/team")
public class TeamAction extends ActionSupport {
    private static final long serialVersionUID = 7587622153127430L;

    @Action("list")
    public String list() {
        System.out.println("1212121");
        return SUCCESS;
    }
    @Action("list2")
    public String list2() {
        System.out.println("1212121");
        return SUCCESS;
    }
}
