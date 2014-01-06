package com.igearbook.action;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import net.jforum.SessionFacade;
import net.jforum.dao.AttachmentDAO;
import net.jforum.dao.DataAccessDriver;
import net.jforum.entities.Group;
import net.jforum.entities.QuotaLimit;
import net.jforum.entities.User;
import net.jforum.exceptions.AttachmentException;
import net.jforum.exceptions.AttachmentSizeTooBigException;
import net.jforum.util.I18n;
import net.jforum.util.MD5;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.json.simple.JSONObject;

import com.google.common.collect.Maps;
import com.igearbook.util.ImageHelper;
import com.opensymphony.xwork2.ActionContext;

@Namespace("/image")
public class ImageAction extends BaseAction {
    private static final long serialVersionUID = 8123243588174068662L;

    private File imgFile;

    private String imgFileContentType;

    private String imgFileFileName;

    private int forumId;

    @Action(value = "upload", interceptorRefs = {
            @InterceptorRef(value = "fileUpload", params = { "allowedExtensions ", ".gif,.jpg,.png", "allowedTypes",
                    "image/png,image/gif,image/jpeg,image/pjpeg", "maximumSize", "5242880" }), @InterceptorRef("defaultStackIgearbook") },
            results = { @Result(name = SUCCESS, location = "uploadJson.ftl"), @Result(name = INPUT, location = "upload_input.ftl") })
    public String upload() {
        final Map<String, Object> data = Maps.newHashMap();
        try {
            final Map<String, Object> result = doUpload();
            data.putAll(result);
            data.put("error", 0);
        } catch (final AttachmentException ae) {
            data.put("error", 1);
            data.put("message", ae.getMessage());
        } catch (final Exception e) {
            data.put("error", 1);
            e.printStackTrace();
            data.put("message", "Unknown exception!");
        }
        final ActionContext context = ServletActionContext.getContext();
        context.put("jsonMsg", JSONObject.toJSONString(data));
        return SUCCESS;
    }

    private Map<String, Object> doUpload() {
        if (imgFile == null) {
            throw new AttachmentException("Attachment cannot be null!");
        }
        final int userId = SessionFacade.getUserSession().getUserId();
        final QuotaLimit ql = this.getQuotaLimit(userId);
        final long fileSize = imgFile.length();
        if (ql != null) {
            if (ql.exceedsQuota(fileSize)) {
                throw new AttachmentSizeTooBigException(I18n.getMessage("Attachments.tooBig", new Integer[] { ql.getSizeInBytes() / 1024,
                        (int) fileSize / 1024 }));
            }
        }

        final String storyDir = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR);
        final String fileDir = makeFileDir();
        final String origFileName = makeFileName();
        final String origFilePath = fileDir + origFileName;

        saveImage(imgFile, storyDir + origFilePath);
        final File origFile = new File(storyDir + origFilePath);
        String filePath = origFilePath;

        if (shouldCreateThumb(origFile, 480, 320)) {
            final String fileName480 = makeThumbFileName(origFileName, 1);
            final String filePath480 = fileDir + fileName480;
            createImageThumb(origFile, storyDir + filePath480, fileName480, 480, 320);
        }
        if (shouldCreateThumb(origFile, 800, 600)) {
            final String fileName800 = makeThumbFileName(origFileName, 2);
            final String filePath800 = fileDir + fileName800;
            createImageThumb(origFile, storyDir + filePath800, fileName800, 800, 600);
            filePath = filePath800;
        }

        final String imgUrl = ServletActionContext.getRequest().getContextPath() + "/" + SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_UPLOAD_DIR)
                + filePath;
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(new File(storyDir + filePath));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final int width = bi.getWidth();
        final int height = bi.getHeight();
        final Map<String, Object> result = Maps.newHashMap();
        result.put("url", imgUrl);
        result.put("width", String.valueOf(width));
        result.put("height", String.valueOf(height));
        result.put("alt", String.format("_IGBImageAlt%s_", System.currentTimeMillis()));

        return result;
    }

    private String makeFileDir() {
        final Calendar calendar = Calendar.getInstance();

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH) + 1;
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        final StringBuffer dir = new StringBuffer(256);
        dir.append('/').append(year).append('/').append(month).append('/').append(day).append('/');

        new File(SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR) + dir.toString()).mkdirs();
        return dir.toString();
    }

    private String makeFileName() {
        final StringBuffer fileName = new StringBuffer(256);

        final int extensionIndex = imgFileFileName.lastIndexOf(".");
        final String extension = imgFileFileName.substring(extensionIndex, imgFileFileName.length()).toLowerCase();
        fileName.append(MD5.crypt(imgFileFileName + System.currentTimeMillis()));
        fileName.append('_').append(SessionFacade.getUserSession().getUserId());
        fileName.append('_').append(forumId);
        fileName.append('_').append(0);
        fileName.append(extension);

        return fileName.toString();
    }

    private String makeThumbFileName(final String origFileName, final int type) {
        return origFileName.replaceAll("_0\\.", String.format("_%s.", type));
    }

    private QuotaLimit getQuotaLimit(final int userId) {
        QuotaLimit ql = new QuotaLimit();
        final User u = DataAccessDriver.getInstance().newUserDAO().selectById(userId);
        final AttachmentDAO am = DataAccessDriver.getInstance().newAttachmentDAO();
        for (final Iterator<Group> iter = u.getGroupsList().iterator(); iter.hasNext();) {
            final QuotaLimit l = am.selectQuotaLimitByGroup(iter.next().getId());
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

    private void saveImage(final File file, final String savePath) {
        FileOutputStream outputStream = null;
        FileInputStream fileIn = null;
        try {
            outputStream = new FileOutputStream(savePath);
            fileIn = new FileInputStream(file);
            final byte[] buffer = new byte[1024];
            int len;
            while ((len = fileIn.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                fileIn.close();
                outputStream.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private boolean shouldCreateThumb(final File file, final int width, final int height) {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final int oldWidth = bi.getWidth();
        final int oldHeight = bi.getHeight();
        if (oldWidth <= width && oldHeight <= height)
            return false;
        else {
            return true;
        }

    }

    private void createImageThumb(final File file, final String newUrl, final String newFileName, final int width, final int height) {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final int oldWidth = bi.getWidth(null);
        final int oldHeight = bi.getHeight(null);
        if (oldWidth <= width && oldHeight <= height)
            saveImage(file, newUrl);
        else {
            ImageHelper.zoomPicture(file.getAbsolutePath(), width, width, newFileName, true);
        }

    }

    public File getImgFile() {
        return imgFile;
    }

    public void setImgFile(final File imgFile) {
        this.imgFile = imgFile;
    }

    public String getImgFileContentType() {
        return imgFileContentType;
    }

    public void setImgFileContentType(final String imgFileContentType) {
        this.imgFileContentType = imgFileContentType;
    }

    public String getImgFileFileName() {
        return imgFileFileName;
    }

    public void setImgFileFileName(final String imgFileFileName) {
        this.imgFileFileName = imgFileFileName;
    }

    public int getForumId() {
        return forumId;
    }

    public void setForumId(final int forumId) {
        this.forumId = forumId;
    }

}
