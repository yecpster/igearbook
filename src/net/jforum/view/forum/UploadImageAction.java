package net.jforum.view.forum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;

import net.jforum.Command;
import net.jforum.JForumExecutionContext;
import net.jforum.SessionFacade;
import net.jforum.dao.AttachmentDAO;
import net.jforum.dao.DataAccessDriver;
import net.jforum.entities.Attachment;
import net.jforum.entities.AttachmentExtension;
import net.jforum.entities.AttachmentInfo;
import net.jforum.entities.Group;
import net.jforum.entities.QuotaLimit;
import net.jforum.entities.User;
import net.jforum.exceptions.AttachmentException;
import net.jforum.exceptions.AttachmentSizeTooBigException;
import net.jforum.exceptions.BadExtensionException;
import net.jforum.exceptions.ForumException;
import net.jforum.util.I18n;
import net.jforum.util.MD5;
import net.jforum.util.legacy.commons.fileupload.FileItem;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;
import net.jforum.util.preferences.TemplateKeys;
import net.jforum.view.forum.common.UploadUtils;
import net.jforum.view.forum.common.ViewCommon;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.google.common.collect.Maps;

/**
 * 
 * @author Chesley
 * 
 */
public class UploadImageAction extends Command {
    private static Logger LOGGER = Logger.getLogger(UploadImageAction.class);
    private AttachmentDAO am;

    public UploadImageAction() {
        this.am = DataAccessDriver.getInstance().newAttachmentDAO();
    }

    public void upload() {

        try {
            int fileId = handleUpload();
            handleUploadResultMsg(String.valueOf(fileId), 0);
        } catch (AttachmentException ae) {
            handleUploadResultMsg(ae.getMessage(), 1);
        } catch (Exception e) {
            handleUploadResultMsg("Unknown exception!", 1);
        }

        setTemplateName(TemplateKeys.UPLOADIMG_UPLOAD);
    }

    private int handleUpload() {
        FileItem item = (FileItem) JForumExecutionContext.getRequest().getObjectParameter("imgFile");
        int userId = SessionFacade.getUserSession().getUserId();

        if (item == null) {
            throw new AttachmentException("Attachment cannot be null!");
        }
        // Bad attachment
        if (item.getName().indexOf('\000') > -1) {
            LOGGER.warn("Possible bad attachment (null char): " + item.getName() + " - user_id: " + userId);
            throw new AttachmentException("Bad attachment (null char)");
        }
        UploadUtils uploadUtils = new UploadUtils(item);
        // Gets file extension
        String extension = uploadUtils.getExtension().toLowerCase();
        if (!extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("gif") && !extension.equals("png")) {
            throw new BadExtensionException(I18n.getMessage("Attachments.badExtension", new String[] { uploadUtils.getExtension() }));
        }
        Attachment a = new Attachment();
        a.setUserId(userId);
        AttachmentInfo info = new AttachmentInfo();
        info.setFilesize(item.getSize());
        info.setComment("Upload from editor!");
        info.setMimetype(item.getContentType());

        // Get only the filename, without the path (IE does that)
        String realName = this.stripPath(item.getName());
        info.setRealFilename(realName);
        info.setUploadTimeInMillis(System.currentTimeMillis());

        AttachmentExtension ext = this.am.selectExtension(uploadUtils.getExtension().toLowerCase());
        if (ext.isUnknown()) {
            ext.setExtension(uploadUtils.getExtension());
        }

        info.setExtension(ext);
        String savePath = this.makeStoreFilename(info);
        info.setPhysicalFilename(savePath);

        a.setInfo(info);

        // Check upload limits
        QuotaLimit ql = this.getQuotaLimit(userId);
        long fileSize = item.getSize();
        if (ql != null) {
            if (ql.exceedsQuota(fileSize)) {
                throw new AttachmentSizeTooBigException(I18n.getMessage("Attachments.tooBig", new Integer[] {
                        new Integer(ql.getSizeInBytes() / 1024), new Integer((int) fileSize / 1024) }));
            }
        }

        String path = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR) + "/" + savePath;
        this.am.addAttachment(a);
        uploadUtils.saveUploadedFile(path);
        return a.getId();
    }

    /**
     * @see net.jforum.Command#list()
     */
    public void list() {
        this.ignoreAction();
    }

    public void view() {
        int id = this.request.getIntParameter("attach_id");

        if (!SessionFacade.isLogged() && !SystemGlobals.getBoolValue(ConfigKeys.ATTACHMENTS_ANONYMOUS)) {
            String referer = this.request.getHeader("Referer");

            if (referer != null) {
                this.setTemplateName(ViewCommon.contextToLogin(referer));
            } else {
                this.setTemplateName(ViewCommon.contextToLogin());
            }

            return;
        }

        AttachmentDAO am = DataAccessDriver.getInstance().newAttachmentDAO();
        Attachment a = am.selectAttachmentById(id);

        String filename = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR) + "/" + a.getInfo().getPhysicalFilename();

        if (!new File(filename).exists()) {
            this.setTemplateName(TemplateKeys.POSTS_ATTACH_NOTFOUND);
            this.context.put("message", I18n.getMessage("Attachments.notFound"));
            return;
        }

        FileInputStream fis = null;
        OutputStream os = null;

        try {
            fis = new FileInputStream(filename);
            os = response.getOutputStream();

            if (am.isPhysicalDownloadMode(a.getInfo().getExtension().getExtensionGroupId())) {
                this.response.setContentType("application/octet-stream");
            } else {
                this.response.setContentType(a.getInfo().getMimetype());
            }

            if (this.request.getHeader("User-Agent").indexOf("Firefox") != -1) {
                this.response.setHeader(
                        "Content-Disposition",
                        "attachment; filename=\""
                                + new String(a.getInfo().getRealFilename().getBytes(SystemGlobals.getValue(ConfigKeys.ENCODING)), SystemGlobals
                                        .getValue(ConfigKeys.DEFAULT_CONTAINER_ENCODING)) + "\";");
            } else {
                this.response.setHeader("Content-Disposition", "attachment; filename=\"" + ViewCommon.toUtf8String(a.getInfo().getRealFilename())
                        + "\";");
            }

            this.response.setContentLength((int) a.getInfo().getFilesize());

            int c;
            byte[] b = new byte[4096];
            while ((c = fis.read(b)) != -1) {
                os.write(b, 0, c);
            }

            JForumExecutionContext.enableCustomContent(true);
        } catch (IOException e) {
            throw new ForumException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                }
            }

            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * @param realName
     *            String
     * @return String
     */
    public String stripPath(String realName) {
        String separator = "/";
        int index = realName.lastIndexOf(separator);

        if (index == -1) {
            separator = "\\";
            index = realName.lastIndexOf(separator);
        }

        if (index > -1) {
            realName = realName.substring(index + 1);
        }

        return realName;
    }

    private String makeStoreFilename(AttachmentInfo a) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());
        c.get(Calendar.YEAR);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        StringBuffer dir = new StringBuffer(256);
        dir.append(year).append('/').append(month).append('/').append(day).append('/');

        new File(SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR) + "/" + dir).mkdirs();

        return dir.append(MD5.crypt(a.getRealFilename() + System.currentTimeMillis())).append('_').append(SessionFacade.getUserSession().getUserId())
                .append('.').append(a.getExtension().getExtension()).toString();
    }

    private QuotaLimit getQuotaLimit(int userId) {
        QuotaLimit ql = new QuotaLimit();
        User u = DataAccessDriver.getInstance().newUserDAO().selectById(userId);

        for (Iterator<Group> iter = u.getGroupsList().iterator(); iter.hasNext();) {
            QuotaLimit l = this.am.selectQuotaLimitByGroup(iter.next().getId());
            if (l == null) {
                continue;
            }

            if (l.getSizeInBytes() > ql.getSizeInBytes()) {
                ql = l;
            }
        }

        if (ql.getSize() == 0) {
            return null;
        }

        return ql;
    }

    private void handleUploadResultMsg(String msg, int status) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("error", status);
        if (status == 0) {
            String home = SystemGlobals.getValue(ConfigKeys.HOMEPAGE_LINK);
            String extension = SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION);
            map.put("url", String.format("%s/uploadImg/view/%s%s", home, msg, extension));
        } else {
            map.put("message", msg);
        }
        this.context.put("jsonMsg", JSONObject.toJSONString(map));
    }
}
