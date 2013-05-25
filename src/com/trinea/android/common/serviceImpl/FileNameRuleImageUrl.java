package com.trinea.android.common.serviceImpl;

import com.trinea.android.common.service.FileNameRule;
import com.trinea.android.common.utils.FileUtils;
import com.trinea.android.common.utils.StringUtils;

/**
 * 文件名规则
 * <ul>
 * <li>以图片Url为文件名，将图片Url中所有非字母非数字的字符用_替换，若为空或长度为0，返回{@link #DEFAULT_FILE_NAME}; 若长度超过{@link #MAX_FILE_NAME_LENGTH}
 * 则从后截取长度为 {@link #MAX_FILE_NAME_LENGTH}部分返回</li>
 * <li>以图片url后缀为后缀</li>
 * </ul>
 * 
 * @author Trinea 2012-11-21
 */
public class FileNameRuleImageUrl implements FileNameRule {

    private static final long  serialVersionUID     = 1L;

    /** 默认文件后缀 **/
    public static final String DEFAULT_FILE_NAME    = "ImageSDCardCacheFile.jpg";
    /** 文件名最大长度，不包括后缀 **/
    public static final int    MAX_FILE_NAME_LENGTH = 127;

    @Override
    public String getFileName(String imageUrl) {
        if (StringUtils.isEmpty(imageUrl)) {
            return DEFAULT_FILE_NAME;
        }

        String ext = FileUtils.getFileExtension(imageUrl);
        String fileName = (imageUrl.length() >= MAX_FILE_NAME_LENGTH ? imageUrl.substring(imageUrl.length()
                                                                                                  - MAX_FILE_NAME_LENGTH,
                                                                                          imageUrl.length()) : imageUrl).replaceAll("[\\W]",
                                                                                                                                    "_");
        return StringUtils.isEmpty(ext) ? fileName : (new StringBuilder().append(fileName).append(".").append(ext).toString());
    }
}
