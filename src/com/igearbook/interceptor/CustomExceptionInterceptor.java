package com.igearbook.interceptor;

import net.jforum.JForumExecutionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.ExceptionHolder;
import com.opensymphony.xwork2.interceptor.ExceptionMappingInterceptor;

public class CustomExceptionInterceptor extends ExceptionMappingInterceptor {
    private static final long serialVersionUID = -628729668697573810L;

    @Override
    protected void publishException(ActionInvocation invocation, ExceptionHolder exceptionHolder) {
        if (JForumExecutionContext.exists()) {
            JForumExecutionContext.enableRollback();
        }
        String exceptionId = "Ex" + System.currentTimeMillis();
        Exception e = new Exception("ExceptionId:" + exceptionId + "!", exceptionHolder.getException());
        e.printStackTrace();
        invocation.getInvocationContext().put("exceptionId", exceptionId);
    }
}
