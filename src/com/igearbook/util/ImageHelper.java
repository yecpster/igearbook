package com.igearbook.util;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author liuwei
 * 
 */
public class ImageHelper {

    private static final String BMP = "bmp";

    private static final String PNG = "png";

    private static final String GIF = "gif";

    private static final String JPEG = "jpeg";

    private static final String JPG = "jpg";

    public static void zoomPicture(String source, final int width, final int height, final String saveName, final boolean adjustSize) {
        if (source == null || source.equals("") || width < 1 || height < 1) {
            return;
        }
        source = source.toLowerCase();
        if (source.endsWith(BMP)) {
            BMPThumbnailHandler(source, width, height, saveName, adjustSize);
        } else if (source.endsWith(PNG) || source.endsWith(GIF) || source.endsWith(JPEG) || source.endsWith(JPG)) {
            thumbnailHandler(source, width, height, saveName, adjustSize);
        }
    }

    private static void thumbnailHandler(final String source, final int width, final int height, final String saveName, final boolean adjustSize) {
        try {
            final File sourceFile = new File(source);
            if (sourceFile.exists()) {
                final Image image = ImageIO.read(sourceFile);
                final int theImgWidth = image.getWidth(null);
                final int theImgHeight = image.getHeight(null);
                int[] size = { theImgWidth, theImgHeight };
                if (adjustSize) {
                    size = adjustImageSize(theImgWidth, theImgHeight, width, height);
                }
                final StringBuffer thumbnailFile = new StringBuffer();
                thumbnailFile.append(sourceFile.getParent());
                thumbnailFile.append(File.separatorChar);
                thumbnailFile.append(saveName);
                writeFile(image, size[0], size[1], thumbnailFile.toString());
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static void BMPThumbnailHandler(final String source, final int width, final int height, final String saveName, final boolean adjustSize) {
        try {
            final File sourceFile = new File(source);
            if (sourceFile.exists()) {
                final Image image = getBMPImage(source);
                final int theImgWidth = image.getWidth(null);
                final int theImgHeight = image.getHeight(null);
                int[] size = { theImgWidth, theImgHeight };
                if (adjustSize) {
                    size = adjustImageSize(theImgWidth, theImgHeight, width, height);
                }
                final StringBuffer thumbnailFile = new StringBuffer();
                thumbnailFile.append(sourceFile.getParent());
                thumbnailFile.append(File.separatorChar);
                thumbnailFile.append(saveName);
                writeFile(image, size[0], size[1], thumbnailFile.toString());
            }
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Image getBMPImage(final String source) throws Exception {

        FileInputStream fs = null;
        Image image = null;
        try {
            fs = new FileInputStream(source);
            final int bfLen = 14;
            final byte bf[] = new byte[bfLen];
            fs.read(bf, 0, bfLen); // 读取14字节BMP文件头
            final int biLen = 40;
            final byte bi[] = new byte[biLen];
            fs.read(bi, 0, biLen); // 读取40字节BMP信息头

            // 源图宽度
            final int nWidth = ((bi[7] & 0xff) << 24) | ((bi[6] & 0xff) << 16) | ((bi[5] & 0xff) << 8) | bi[4] & 0xff;

            // 源图高度
            final int nHeight = ((bi[11] & 0xff) << 24) | ((bi[10] & 0xff) << 16) | ((bi[9] & 0xff) << 8) | bi[8] & 0xff;

            // 位数
            final int nBitCount = ((bi[15] & 0xff) << 8) | bi[14] & 0xff;

            // 源图大小
            final int nSizeImage = ((bi[23] & 0xff) << 24) | ((bi[22] & 0xff) << 16) | ((bi[21] & 0xff) << 8) | bi[20] & 0xff;

            // 对24位BMP进行解析
            if (nBitCount == 24) {
                final int nPad = (nSizeImage / nHeight) - nWidth * 3;
                final int nData[] = new int[nHeight * nWidth];
                final byte bRGB[] = new byte[(nWidth + nPad) * 3 * nHeight];
                fs.read(bRGB, 0, (nWidth + nPad) * 3 * nHeight);
                int nIndex = 0;
                for (int j = 0; j < nHeight; j++) {
                    for (int i = 0; i < nWidth; i++) {
                        nData[nWidth * (nHeight - j - 1) + i] = (255 & 0xff) << 24 | ((bRGB[nIndex + 2] & 0xff) << 16)
                                | ((bRGB[nIndex + 1] & 0xff) << 8) | bRGB[nIndex] & 0xff;
                        nIndex += 3;
                    }
                    nIndex += nPad;
                }
                final Toolkit kit = Toolkit.getDefaultToolkit();
                image = kit.createImage(new MemoryImageSource(nWidth, nHeight, nData, 0, nWidth));
            }
        } catch (final Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
        return image;
    }

    private static void writeFile(final Image image, final int width, final int height, final String thumbnailFile) throws Exception {

        if (image == null)
            return;

        final BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        tag.getGraphics().drawImage(image, 0, 0, width, height, null);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(thumbnailFile);
            final JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(tag);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }

    private static int[] adjustImageSize(final int theImgWidth, final int theImgHeight, final int defWidth, final int defHeight) {
        final int[] size = { 0, 0 };

        final float theImgHeightFloat = Float.parseFloat(String.valueOf(theImgHeight));
        final float theImgWidthFloat = Float.parseFloat(String.valueOf(theImgWidth));
        if (theImgWidth < theImgHeight) {
            final float scale = theImgHeightFloat / theImgWidthFloat;
            size[0] = Math.round(defHeight / scale);
            size[1] = defHeight;
        } else {
            final float scale = theImgWidthFloat / theImgHeightFloat;
            size[0] = defWidth;
            size[1] = Math.round(defWidth / scale);
        }
        return size;
    }

    private Dimension getImageDim(final String path) {
        Dimension result = null;
        final String suffix = this.getFileSuffix(path);
        final Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
        if (iter.hasNext()) {
            final ImageReader reader = iter.next();
            try {
                final ImageInputStream stream = new FileImageInputStream(new File(path));
                reader.setInput(stream);
                final int width = reader.getWidth(reader.getMinIndex());
                final int height = reader.getHeight(reader.getMinIndex());
                result = new Dimension(width, height);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            } finally {
                reader.dispose();
            }
        } else {
            throw new RuntimeException("No reader found for given format: " + suffix);
        }
        return result;
    }

    private String getFileSuffix(final String path) {
        String result = null;
        if (path != null) {
            result = "";
            if (path.lastIndexOf('.') != -1) {
                result = path.substring(path.lastIndexOf('.'));
                if (result.startsWith(".")) {
                    result = result.substring(1);
                }
            }
        }
        return result;
    }

}
