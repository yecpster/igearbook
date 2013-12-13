package com.igearbook.action;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import net.jforum.SessionFacade;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.ForumDAO;
import net.jforum.dao.PostDAO;
import net.jforum.dao.TopicDAO;
import net.jforum.entities.Forum;
import net.jforum.entities.Post;
import net.jforum.entities.Topic;
import net.jforum.entities.UserSession;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.redfin.sitemapgenerator.ChangeFreq;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import com.redfin.sitemapgenerator.WebSitemapUrl;

@Namespace("/sitemap")
public class SitemapAction extends BaseAction {
    private static final long serialVersionUID = 7587622153127430L;

    @Action(value = "generate", results = { @Result(name = SUCCESS, location = "/templates/default/common/success.ftl") })
    public String generate() {
        final UserSession userSession = SessionFacade.getUserSession();
        final boolean canEdit = userSession.isWebAdmin() || userSession.isAdmin();
        if (!canEdit) {
            return PERMISSION;
        }
        final ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
        final TopicDAO topicDao = DataAccessDriver.getInstance().newTopicDAO();
        final PostDAO postDao = DataAccessDriver.getInstance().newPostDAO();
        final List<Forum> forums = forumDao.selectAll();

        final File dir = new File(SystemGlobals.getValue(ConfigKeys.APPLICATION_PATH));
        WebSitemapGenerator wsg;
        try {
            wsg = new WebSitemapGenerator("http://www.igearbook.com", dir);
            final String urlStrIndex = "http://www.igearbook.com/portal/index.action";
            final WebSitemapUrl urlIndex = new WebSitemapUrl.Options(urlStrIndex).changeFreq(ChangeFreq.DAILY).build();
            wsg.addUrl(urlIndex);
            int urlCount = 1;
            for (final Forum forum : forums) {
                if (forum.getType() == Forum.TYPE_TEAM) {
                    final String urlStr = "http://www.igearbook.com/team/show.action?teamId=" + forum.getId();
                    final WebSitemapUrl url = new WebSitemapUrl.Options(urlStr).build();
                    wsg.addUrl(url);
                    urlCount++;
                }
            }
            for (final Forum forum : forums) {
                final List<Topic> topics = topicDao.selectAllByForum(forum.getId());
                for (final Topic topic : topics) {
                    final String urlStr = "http://www.igearbook.com/posts/list/" + topic.getId() + ".page";
                    final Post post = postDao.selectById(topic.getFirstPostId());
                    final WebSitemapUrl url = new WebSitemapUrl.Options(urlStr).lastMod(post.getTime()).build();
                    wsg.addUrl(url);
                    urlCount++;
                }
            }
            wsg.write();
            if (urlCount > WebSitemapGenerator.MAX_URLS_PER_SITEMAP) {
                wsg.writeSitemapsWithIndex();
            }

        } catch (final MalformedURLException e) {
            e.printStackTrace();
        }

        return SUCCESS;
    }

}
