package com.igearbook.resulttype;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.views.freemarker.FreemarkerManager;
import org.apache.struts2.views.freemarker.ScopesHashModel;

import com.opensymphony.xwork2.util.ValueStack;

import freemarker.template.ObjectWrapper;

public class CustomFreemarkerManager extends FreemarkerManager{

    @Override
    public ScopesHashModel buildTemplateModel(ValueStack stack, Object action, ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, ObjectWrapper wrapper) {
        ScopesHashModel model = buildScopesHashModel(servletContext, request, response, wrapper, stack);
        populateContext(model, stack, action, request, response);
        if (tagLibraries != null) {
            for (String prefix : tagLibraries.keySet()) {
                model.put(prefix, tagLibraries.get(prefix).getFreemarkerModels(stack, request, response));
            }
        }

        //place the model in the request using the special parameter.  This can be retrieved for freemarker and velocity.
        request.setAttribute(ATTR_TEMPLATE_MODEL, model);

        // To be compatible with jforum, we need to use the use the jforum UserSession instead of using the default one.
        model.remove("session");
        return model;
    }
}
