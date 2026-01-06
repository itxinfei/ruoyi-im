package com.ruoyi.im.utils;

import org.springframework.util.StringUtils;

/**
 * 文件工具类
 *
 * @author ruoyi
 */
public class FileUtils {

    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
    private static final String[] VIDEO_EXTENSIONS = {"mp4", "avi", "mov", "wmv", "flv", "mkv"};
    private static final String[] DOCUMENT_EXTENSIONS = {"doc", "docx", "pdf", "txt", "xls", "xlsx", "ppt", "pptx"};
    private static final String[] AUDIO_EXTENSIONS = {"mp3", "wav", "aac", "flac", "ogg"};

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    public static String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 获取文件类型
     *
     * @param extension 文件扩展名
     * @return 文件类型
     */
    public static String getFileType(String extension) {
        if (!StringUtils.hasText(extension)) {
            return "other";
        }

        String ext = extension.toLowerCase();

        for (String imageExt : IMAGE_EXTENSIONS) {
            if (ext.equals(imageExt)) {
                return "image";
            }
        }

        for (String videoExt : VIDEO_EXTENSIONS) {
            if (ext.equals(videoExt)) {
                return "video";
            }
        }

        for (String audioExt : AUDIO_EXTENSIONS) {
            if (ext.equals(audioExt)) {
                return "audio";
            }
        }

        for (String docExt : DOCUMENT_EXTENSIONS) {
            if (ext.equals(docExt)) {
                return "document";
            }
        }

        return "other";
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 检查是否为图片文件
     *
     * @param filename 文件名
     * @return 是否为图片
     */
    public static boolean isImage(String filename) {
        String extension = getFileExtension(filename);
        return "image".equals(getFileType(extension));
    }

    /**
     * 检查是否为视频文件
     *
     * @param filename 文件名
     * @return 是否为视频
     */
    public static boolean isVideo(String filename) {
        String extension = getFileExtension(filename);
        return "video".equals(getFileType(extension));
    }

    /**
     * 检查是否为文档文件
     *
     * @param filename 文件名
     * @return 是否为文档
     */
    public static boolean isDocument(String filename) {
        String extension = getFileExtension(filename);
        return "document".equals(getFileType(extension));
    }
}
