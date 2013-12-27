/*
 * Copyright (c) JForum Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, 
 * with or without modification, are permitted provided 
 * that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above 
 * copyright notice, this list of conditions and the 
 * following  disclaimer.
 * 2)  Redistributions in binary form must reproduce the 
 * above copyright notice, this list of conditions and 
 * the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * 3) Neither the name of "Rafael Steil" nor 
 * the names of its contributors may be used to endorse 
 * or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 * 
 * Created on 09/08/2007 09:31:17
 * 
 * The JForum Project
 * http://www.jforum.net
 */
package net.jforum.view.forum;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.jforum.Command;
import net.jforum.SessionFacade;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.PostDAO;
import net.jforum.entities.ModerationLog;
import net.jforum.entities.Post;
import net.jforum.entities.User;
import net.jforum.repository.PostRepository;
import net.jforum.repository.SecurityRepository;
import net.jforum.search.LuceneManager;
import net.jforum.search.SearchFacade;
import net.jforum.security.SecurityConstants;
import net.jforum.util.SafeHtml;
import net.jforum.util.mail.Spammer;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;
import net.jforum.util.preferences.TemplateKeys;
import net.jforum.view.forum.common.PostCommon;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import freemarker.template.SimpleHash;

/**
 * @author Rafael Steil
 * @version $Id: AjaxAction.java,v 1.7 2007/09/21 15:54:31 rafaelsteil Exp $
 */
public class AjaxAction extends Command {
    private static Logger logger = Logger.getLogger(AjaxAction.class);

    /**
     * Sends a test message
     * 
     * @param sender
     *            The sender's email address
     * @param host
     *            the smtp host
     * @param auth
     *            if need authorization or not
     * @param username
     *            the smtp server username, if auth is needed
     * @param password
     *            the smtp server password, if auth is needed
     * @param to
     *            the recipient
     * @return The status message
     */
    public void sendTestMail() {
        final String sender = this.request.getParameter("sender");
        final String host = this.request.getParameter("host");
        final String port = this.request.getParameter("port");
        final String auth = this.request.getParameter("auth");
        final String ssl = this.request.getParameter("ssl");
        final String username = this.request.getParameter("username");
        final String password = this.request.getParameter("password");
        final String to = this.request.getParameter("to");

        // Save the current values
        final String originalHost = SystemGlobals.getValue(ConfigKeys.MAIL_SMTP_HOST);
        final String originalAuth = SystemGlobals.getValue(ConfigKeys.MAIL_SMTP_AUTH);
        final String originalUsername = SystemGlobals.getValue(ConfigKeys.MAIL_SMTP_USERNAME);
        final String originalPassword = SystemGlobals.getValue(ConfigKeys.MAIL_SMTP_PASSWORD);
        final String originalSender = SystemGlobals.getValue(ConfigKeys.MAIL_SENDER);
        final String originalSSL = SystemGlobals.getValue(ConfigKeys.MAIL_SMTP_SSL);
        final String originalPort = SystemGlobals.getValue(ConfigKeys.MAIL_SMTP_PORT);

        // Now put the new ones
        SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_HOST, host);
        SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_AUTH, auth);
        SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_USERNAME, username);
        SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_PASSWORD, password);
        SystemGlobals.setValue(ConfigKeys.MAIL_SENDER, sender);
        SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_SSL, ssl);
        SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_PORT, port);

        String status = "OK";

        // Send the test mail
        class TestSpammer extends Spammer {
            public TestSpammer(final String to) {
                final List l = new ArrayList();

                final User user = new User();
                user.setEmail(to);

                l.add(user);

                this.setUsers(l);

                this.setTemplateParams(new SimpleHash());
                this.prepareMessage("JForum Test Mail", null);
            }

            @Override
            protected String processTemplate() throws Exception {
                return ("Test mail from JForum Admin Panel. Sent at " + new Date());
            }

            @Override
            protected void createTemplate(final String messageFile) throws Exception {
            }
        }

        final Spammer s = new TestSpammer(to);

        try {
            s.dispatchMessages();
        } catch (final Exception e) {
            status = StringEscapeUtils.escapeEcmaScript(e.toString());
            logger.error(e.toString(), e);
        } finally {
            // Restore the original values
            SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_HOST, originalHost);
            SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_AUTH, originalAuth);
            SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_USERNAME, originalUsername);
            SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_PASSWORD, originalPassword);
            SystemGlobals.setValue(ConfigKeys.MAIL_SENDER, originalSender);
            SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_SSL, originalSSL);
            SystemGlobals.setValue(ConfigKeys.MAIL_SMTP_PORT, originalPort);
        }

        this.setTemplateName(TemplateKeys.AJAX_TEST_MAIL);
        this.context.put("status", status);
    }

    public void isPostIndexed() {
        final int postId = this.request.getIntParameter("post_id");

        this.setTemplateName(TemplateKeys.AJAX_IS_POST_INDEXED);

        final LuceneManager manager = (LuceneManager) SearchFacade.manager();
        final Document doc = manager.luceneSearch().findDocumentByPostId(postId);

        this.context.put("doc", doc);
    }

    public void loadPostContents() {
        final int postId = this.request.getIntParameter("id");
        final PostDAO dao = DataAccessDriver.getInstance().newPostDAO();
        final Post post = dao.selectById(postId);
        this.setTemplateName(TemplateKeys.AJAX_LOAD_POST);
        this.context.put("post", post);
    }

    public void savePost() {
        final PostDAO postDao = DataAccessDriver.getInstance().newPostDAO();
        Post post = postDao.selectById(this.request.getIntParameter("id"));

        final String originalMessage = post.getText();

        if (!PostCommon.canEditPost(post)) {
            post = PostCommon.preparePostForDisplay(post);
        } else {
            post.setText(this.request.getParameter("value"));
            postDao.update(post);
            SearchFacade.update(post);
            post = PostCommon.preparePostForDisplay(post);
        }

        final boolean isModerator = SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_POST_EDIT, String.valueOf(post.getForumId()));

        if (SystemGlobals.getBoolValue(ConfigKeys.MODERATION_LOGGING_ENABLED) && isModerator
                && post.getUserId() != SessionFacade.getUserSession().getUserId()) {
            final ModerationHelper helper = new ModerationHelper();

            this.request.addParameter("log_original_message", originalMessage);
            this.request.addParameter("post_id", String.valueOf(post.getId()));
            this.request.addParameter("topic_id", String.valueOf(post.getTopicId()));

            final ModerationLog log = helper.buildModerationLogFromRequest();
            log.getPosterUser().setId(post.getUserId());

            helper.saveModerationLog(log);
        }

        if (SystemGlobals.getBoolValue(ConfigKeys.POSTS_CACHE_ENABLED)) {
            PostRepository.update(post.getTopicId(), PostCommon.preparePostForDisplay(post));
        }

        this.setTemplateName(TemplateKeys.AJAX_LOAD_POST);
        this.context.put("post", post);
    }

    public void previewPost() {
        Post post = new Post();

        post.setText(this.request.getParameter("text"));
        post.setSubject(this.request.getParameter("subject"));
        post.setHtmlEnabled("true".equals(this.request.getParameter("html")));
        post.setBbCodeEnabled("true".equals(this.request.getParameter("bbcode")));
        post.setSmiliesEnabled("true".equals(this.request.getParameter("smilies")));

        if (post.isHtmlEnabled()) {
            post.setText(new SafeHtml().makeSafe(post.getText()));
        }

        post = PostCommon.preparePostForDisplay(post);
        post.setSubject(StringEscapeUtils.escapeEcmaScript(post.getSubject()));
        post.setText(StringEscapeUtils.escapeEcmaScript(post.getText()));

        this.setTemplateName(TemplateKeys.AJAX_PREVIEW_POST);
        this.context.put("post", post);
    }

    /**
     * @see net.jforum.Command#list()
     */
    @Override
    public void list() {
        this.ignoreAction();
    }
}
