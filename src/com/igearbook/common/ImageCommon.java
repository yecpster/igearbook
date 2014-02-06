package com.igearbook.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
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

import com.google.common.collect.Maps;
import com.igearbook.constant.ImageSize;
import com.igearbook.entities.ImageVo;
import com.igearbook.util.ImageHelper;

public class ImageCommon {
    private final File imgFile;

    private final String imgFileName;

    private final int moduleId;

    private final List<ImageSize> imageSizes;

    private boolean thumbIgnoreHeight = false;

    public ImageCommon(final File imgFile, final String imgFileName, final int moduleId, final List<ImageSize> imageSizes) {
        this.imgFile = imgFile;
        this.imgFileName = imgFileName;
        this.moduleId = moduleId;
        this.imageSizes = imageSizes;
    }

    public Map<ImageSize, ImageVo> doUpload() {
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
            // TODO handle this exception
        }
        final Map<ImageSize, ImageVo> result = Maps.newHashMap();

        final String storyDir = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR);
        final String fileDir = makeFileDir();
        final String origFileName = makeFileName();
        final String origFilePath = fileDir + origFileName;

        final ImageVo originalImg = saveImage(imgFile, origFilePath);
        result.put(ImageSize.ORIGINAL, originalImg);

        final File origFile = new File(storyDir + origFilePath);

        for (final ImageSize imageSizeEnum : imageSizes) {
            if (shouldCreateThumb(origFile, imageSizeEnum)) {
                final String fileNameThumb = makeThumbFileName(origFileName, imageSizeEnum.getSizeNum());
                final ImageVo thumbImgVo = createImageThumb(origFile, fileDir, fileNameThumb, imageSizeEnum.getWidth());
                result.put(imageSizeEnum, thumbImgVo);
            }
        }

        return result;
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

        final int extensionIndex = imgFileName.lastIndexOf(".");
        final String extension = imgFileName.substring(extensionIndex, imgFileName.length()).toLowerCase();
        fileName.append(MD5.crypt(imgFileName + System.currentTimeMillis()));
        fileName.append('_').append(SessionFacade.getUserSession().getUserId());
        fileName.append('_').append(moduleId);
        fileName.append('_').append(0);
        fileName.append(extension);

        return fileName.toString();
    }

    private String makeThumbFileName(final String origFileName, final int type) {
        return origFileName.replaceAll("_0\\.", String.format("_%s.", type));
    }

    private ImageVo saveImage(final File file, final String filePath) {
        final String storyDir = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR);
        final String storyPath = storyDir + filePath;

        FileOutputStream outputStream = null;
        FileInputStream fileIn = null;
        try {
            outputStream = new FileOutputStream(storyPath);
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

        return loadImageVo(filePath);
    }

    private ImageVo loadImageVo(final String filePath) {
        final String storyDir = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR);
        final String storyPath = storyDir + filePath;
        final String imgUrlPrefix = ServletActionContext.getRequest().getContextPath() + "/"
                + SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_UPLOAD_DIR);

        BufferedImage bi = null;
        try {
            bi = ImageIO.read(new File(storyPath));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final int width = bi.getWidth();
        final int height = bi.getHeight();

        final ImageVo imageVo = new ImageVo();
        imageVo.setUrl(imgUrlPrefix + filePath);
        imageVo.setWidth(width);
        imageVo.setHeight(height);

        return imageVo;
    }

    private boolean shouldCreateThumb(final File file, final ImageSize imageSize) {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final int oldWidth = bi.getWidth();
        final int oldHeight = bi.getHeight();
        final int testNum = oldWidth > oldHeight ? oldWidth : oldHeight;
        if (isThumbIgnoreHeight() && oldWidth <= imageSize.getWidth()) {
            return false;
        } else if (!isThumbIgnoreHeight() && testNum <= imageSize.getWidth()) {
            return false;
        } else {
            return true;
        }
    }

    private ImageVo createImageThumb(final File file, final String fileDir, final String newFileName, final int width) {
        ImageHelper.zoomPicture(file.getAbsolutePath(), width, width, newFileName, true);
        return loadImageVo(fileDir + newFileName);
    }

    public boolean isThumbIgnoreHeight() {
        return thumbIgnoreHeight;
    }

    public void setThumbIgnoreHeight(final boolean thumbIgnoreHeight) {
        this.thumbIgnoreHeight = thumbIgnoreHeight;
    }
}
