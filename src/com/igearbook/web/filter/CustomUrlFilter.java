package com.igearbook.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.igearbook.constant.UrlType;
import com.igearbook.dao.CustomUrlDao;
import com.igearbook.entities.CustomUrl;

public final class CustomUrlFilter implements Filter {
    @Autowired
    private CustomUrlDao customUrlDao;

    public void setCustomUrlDao(final CustomUrlDao customUrlDao) {
        this.customUrlDao = customUrlDao;
    }

    @Override
    public void init(final FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest hsRequest = (HttpServletRequest) request;
        final HttpServletResponse hsResponse = (HttpServletResponse) response;
        final String contextPath = hsRequest.getContextPath();
        String uri = hsRequest.getRequestURI();
        if (uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length() + 1);
        }
        final CustomUrl customUrl = customUrlDao.getByUrl(uri);
        if (customUrl != null && customUrl.getType() == UrlType.Team) {
            final int moduleId = customUrl.getModuleId();
            hsRequest.getRequestDispatcher("/team/show.action?teamId=" + moduleId).forward(hsRequest, hsResponse);
        }
        chain.doFilter(hsRequest, hsResponse);
    }
}
