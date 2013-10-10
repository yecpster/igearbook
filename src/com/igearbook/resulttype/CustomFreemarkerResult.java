package com.igearbook.resulttype;

import java.io.IOException;

import net.jforum.JForumExecutionContext;

import org.apache.struts2.views.freemarker.FreemarkerResult;

import freemarker.template.Template;
import freemarker.template.TemplateModel;

public class CustomFreemarkerResult extends FreemarkerResult {

    private static final long serialVersionUID = -3296216653545643971L;

    @Override
    protected void postTemplateProcess(Template template, TemplateModel data) throws IOException {
        JForumExecutionContext.finish();
    }

}
