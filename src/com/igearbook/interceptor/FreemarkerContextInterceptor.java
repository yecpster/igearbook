package com.igearbook.interceptor;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jforum.ControllerUtils;
import net.jforum.JForumExecutionContext;
import net.jforum.SessionFacade;
import net.jforum.context.JForumContext;
import net.jforum.context.RequestContext;
import net.jforum.context.ResponseContext;
import net.jforum.context.web.WebRequestContext;
import net.jforum.context.web.WebResponseContext;
import net.jforum.repository.SecurityRepository;
import net.jforum.util.I18n;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import freemarker.template.SimpleHash;
import freemarker.template.TemplateModelException;

public class FreemarkerContextInterceptor extends AbstractInterceptor {
    private static final long serialVersionUID = -628729668697573810L;

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {

        if (invocation.isExecuted()) {
            //JForumExecutionContext.finish();
        } else {
            mockService(ServletActionContext.getRequest(), ServletActionContext.getResponse());
        }
        return invocation.invoke();
    }

    private void mockService(HttpServletRequest req, HttpServletResponse res) throws TemplateModelException, IOException {
        JForumContext forumContext = null;
        RequestContext request = null;
        ResponseContext response = null;

        // Initializes the execution context
        JForumExecutionContext ex = JForumExecutionContext.get();

        request = new WebRequestContext(req);
        response = new WebResponseContext(res);

        forumContext = new JForumContext(request.getContextPath(), SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION), request, response);
        ex.setForumContext(forumContext);

        JForumExecutionContext.set(ex);

        // Setup stuff
        SimpleHash context = JForumExecutionContext.getTemplateContext();

        Object startupTime = ServletActionContext.getServletContext().getAttribute("startupTime");
        context.put("startupTime", startupTime);

        ControllerUtils utils = new ControllerUtils();
        utils.refreshSession();

        context.put("logged", SessionFacade.isLogged());

        // Process security data
        SecurityRepository.load(SessionFacade.getUserSession().getUserId());

        utils.prepareTemplateContext(context, forumContext);
        context.put("language", I18n.getUserLanguage());
        context.put("session", SessionFacade.getUserSession());

        ActionContext actionContext = ServletActionContext.getContext();
        @SuppressWarnings("unchecked")
        Map<String, Object> contextMap = context.toMap();
        for (Entry<String, Object> entry : contextMap.entrySet()) {
            actionContext.put(entry.getKey(), entry.getValue());
        }

    }

}
