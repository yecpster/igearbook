package com.igearbook.interceptor;

import net.jforum.JForumExecutionContext;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.ExceptionHolder;
import com.opensymphony.xwork2.interceptor.ExceptionMappingInterceptor;

public class CustomExceptionInterceptor extends ExceptionMappingInterceptor {
    private static final long serialVersionUID = -628729668697573810L;

    private static Logger LOG = Logger.getRootLogger();

    @Override
    protected void publishException(final ActionInvocation invocation, final ExceptionHolder exceptionHolder) {
        if (JForumExecutionContext.exists()) {
            JForumExecutionContext.enableRollback();
        }
        final String exceptionId = "Ex" + System.currentTimeMillis();
        final String msg = "ExceptionId:" + exceptionId + "!";
        final Exception e = exceptionHolder.getException();
        LOG.error(msg, e);
        final Object debug = ServletActionContext.getServletContext().getAttribute("debug");
        if (Boolean.TRUE.equals(debug)) {
            final Exception debugException = new Exception(msg, e);
            debugException.printStackTrace();
        }
        invocation.getInvocationContext().put("exceptionId", exceptionId);
    }
}
