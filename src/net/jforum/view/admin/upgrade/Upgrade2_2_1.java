package net.jforum.view.admin.upgrade;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.jforum.dao.AttachmentDAO;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.ForumDAO;
import net.jforum.dao.PostDAO;
import net.jforum.dao.RecommendationDAO;
import net.jforum.dao.TopicDAO;
import net.jforum.entities.Attachment;
import net.jforum.entities.Forum;
import net.jforum.entities.Post;
import net.jforum.entities.Recommendation;
import net.jforum.entities.Topic;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

public class Upgrade2_2_1 extends GenericUpgradeService implements UpgradeService {
    private static final Pattern PT = Pattern.compile("uploadImg/view/(\\d+?)\\.page");

    @Override
    public void upgrade() {
        RecommendationDAO rpostDao = DataAccessDriver.getInstance().newRecommendationDAO();

        int foundCount = 0;
        List<Recommendation> recommends = rpostDao.selectByTypeByLimit(Recommendation.TYPE_INDEX_IMG, 0, Integer.MAX_VALUE);
        for (Recommendation recommend : recommends) {
            String text = recommend.getImageUrl();
            System.out.println("Updating post: " + foundCount);
            foundCount++;
            recommend.setImageUrl(text.replaceAll("\\s.*", ""));
            rpostDao.update(recommend);

        }
        System.out.println("Updated post count: " + foundCount);
    }

    public void upgrade2() {
        ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
        TopicDAO topicDao = DataAccessDriver.getInstance().newTopicDAO();
        PostDAO postDao = DataAccessDriver.getInstance().newPostDAO();
        AttachmentDAO am = DataAccessDriver.getInstance().newAttachmentDAO();
        List<Forum> forums = forumDao.selectAll();

        Matcher macher = PT.matcher("");
        int foundCount = 0;
        for (Forum forum : forums) {
            List<Topic> topics = topicDao.selectAllByForum(forum.getId());
            for (Topic topic : topics) {
                List<Post> posts = postDao.selectAllByTopic(topic.getId());
                for (Post post : posts) {
                    String text = post.getText();
                    macher.reset(text);
                    StringBuffer buffer = new StringBuffer(text.length());
                    boolean found = false;
                    while (macher.find()) {
                        found = true;
                        String id = macher.group(1);
                        Attachment a = am.selectAttachmentById(Integer.parseInt(id));
                        String filename = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_UPLOAD_DIR) + "/" + a.getInfo().getPhysicalFilename();
                        String realFilename = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR) + "/" + a.getInfo().getPhysicalFilename();
                        StringBuffer imgSb = new StringBuffer(filename);
                        imgSb.append("\"");
                        try {
                            File imgFile = new File(realFilename);
                            if (imgFile.exists()) {
                                BufferedImage img = ImageIO.read(imgFile);
                                int width = img.getWidth();
                                int height = img.getHeight();
                                imgSb.append(" width=\"");
                                imgSb.append(width);
                                imgSb.append("\" height=\"");
                                imgSb.append(height);
                                imgSb.append("\"");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        macher.appendReplacement(buffer, imgSb.toString());
                    }

                    macher.appendTail(buffer);
                    if (found) {
                        System.out.println("Updating post: " + foundCount);
                        foundCount++;
                        post.setText(buffer.toString());
                        postDao.update(post);
                    }

                }
            }
        }
        System.out.println("Updated post count: " + foundCount);
    }
}
