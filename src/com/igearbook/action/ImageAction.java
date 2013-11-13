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
                    "image/png,image/gif,image/jpeg,image/pjpeg" }), @InterceptorRef("defaultStackIgearbook") }, results = { @Result(name = SUCCESS, location = "uploadJson.ftl") })
    public String upload() {
        Map<String, Object> data = Maps.newHashMap();
        try {
            Map<String, Object> result = doUpload();
            data.putAll(result);
            data.put("error", 0);
        } catch (AttachmentException ae) {
            data.put("error", 1);
            data.put("message", ae.getMessage());
        } catch (Exception e) {
            data.put("error", 1);
            e.printStackTrace();
            data.put("message", "Unknown exception!");
        }
        ActionContext context = ServletActionContext.getContext();
        context.put("jsonMsg", JSONObject.toJSONString(data));
        return SUCCESS;
    }

    private Map<String, Object> doUpload() {
        if (imgFile == null) {
            throw new AttachmentException("Attachment cannot be null!");
        }
        int userId = SessionFacade.getUserSession().getUserId();
        QuotaLimit ql = this.getQuotaLimit(userId);
        long fileSize = imgFile.length();
        if (ql != null) {
            if (ql.exceedsQuota(fileSize)) {
                throw new AttachmentSizeTooBigException(I18n.getMessage("Attachments.tooBig", new Integer[] { ql.getSizeInBytes() / 1024,
                        (int) fileSize / 1024 }));
            }
        }

        String storyDir = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR);
        String fileDir = makeFileDir();
        String origFileName = makeFileName();
        String origFilePath = fileDir + origFileName;

        saveImage(imgFile, storyDir + origFilePath);
        File origFile = new File(storyDir + origFilePath);
        String filePath = origFilePath;

        if (shouldCreateThumb(origFile, 480, 320)) {
            String fileName480 = makeThumbFileName(origFileName, 1);
            String filePath480 = fileDir + fileName480;
            createImageThumb(origFile, storyDir + filePath480, fileName480, 480, 320);
        }
        if (shouldCreateThumb(origFile, 800, 600)) {
            String fileName800 = makeThumbFileName(origFileName, 2);
            String filePath800 = fileDir + fileName800;
            createImageThumb(origFile, storyDir + filePath800, fileName800, 800, 600);
            filePath = filePath800;
        }

        String imgUrl = ServletActionContext.getRequest().getContextPath() + "/" + SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_UPLOAD_DIR)
                + filePath;
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(new File(storyDir + filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int width = bi.getWidth();
        int height = bi.getHeight();
        Map<String, Object> result = Maps.newHashMap();
        result.put("url", imgUrl);
        result.put("width", String.valueOf(width));
        result.put("height", String.valueOf(height));
        // result.put("title", I18n.getMessage("Image.clickToZoom"));

        return result;
    }

    private String makeFileDir() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        StringBuffer dir = new StringBuffer(256);
        dir.append('/').append(year).append('/').append(month).append('/').append(day).append('/');

        new File(SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR) + dir.toString()).mkdirs();
        return dir.toString();
    }

    private String makeFileName() {
        StringBuffer fileName = new StringBuffer(256);

        int extensionIndex = imgFileFileName.lastIndexOf(".");
        String extension = imgFileFileName.substring(extensionIndex, imgFileFileName.length()).toLowerCase();
        fileName.append(MD5.crypt(imgFileFileName + System.currentTimeMillis()));
        fileName.append('_').append(SessionFacade.getUserSession().getUserId());
        fileName.append('_').append(forumId);
        fileName.append('_').append(0);
        fileName.append(extension);

        return fileName.toString();
    }

    private String makeThumbFileName(String origFileName, int type) {
        return origFileName.replaceAll("_0\\.", String.format("_%s.", type));
    }

    private QuotaLimit getQuotaLimit(int userId) {
        QuotaLimit ql = new QuotaLimit();
        User u = DataAccessDriver.getInstance().newUserDAO().selectById(userId);
        AttachmentDAO am = DataAccessDriver.getInstance().newAttachmentDAO();
        for (Iterator<Group> iter = u.getGroupsList().iterator(); iter.hasNext();) {
            QuotaLimit l = am.selectQuotaLimitByGroup(iter.next().getId());
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

    private void saveImage(File file, String savePath) {
        FileOutputStream outputStream = null;
        FileInputStream fileIn = null;
        try {
            outputStream = new FileOutputStream(savePath);
            fileIn = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fileIn.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                fileIn.close();
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private boolean shouldCreateThumb(File file, int width, int height) {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int oldWidth = bi.getWidth();
        int oldHeight = bi.getHeight();
        if (oldWidth <= width && oldHeight <= height)
            return false;
        else {
            return true;
        }

    }

    private void createImageThumb(File file, String newUrl, String newFileName, int width, int height) {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int oldWidth = bi.getWidth();
        int oldHeight = bi.getHeight();
        if (oldWidth <= width && oldHeight <= height)
            saveImage(file, newUrl);
        else {
            ImageHelper.zoomPicture(file.getAbsolutePath(), width, width, newFileName, true);
        }

    }

    public File getImgFile() {
        return imgFile;
    }

    public void setImgFile(File imgFile) {
        this.imgFile = imgFile;
    }

    public String getImgFileContentType() {
        return imgFileContentType;
    }

    public void setImgFileContentType(String imgFileContentType) {
        this.imgFileContentType = imgFileContentType;
    }

    public String getImgFileFileName() {
        return imgFileFileName;
    }

    public void setImgFileFileName(String imgFileFileName) {
        this.imgFileFileName = imgFileFileName;
    }

    public int getForumId() {
        return forumId;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

}
