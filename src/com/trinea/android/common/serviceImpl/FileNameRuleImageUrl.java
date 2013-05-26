package com.trinea.android.common.serviceImpl;

import com.trinea.android.common.service.FileNameRule;
import com.trinea.android.common.utils.FileUtils;
import com.trinea.android.common.utils.StringUtils;

/**
 * File name rule, used when save image to sdcard in {@link ImageSDCardCache}
 * <ul>
 * <li>use image url as file name, replace char with _ if not letter or number</li>
 * <li>use file suffix in url as target file suffix</li>
 * </ul>
 * 
 * @author Trinea 2012-11-21
 */
public class FileNameRuleImageUrl implements FileNameRule {

    private static final long  serialVersionUID     = 1L;

    /** default file name if image url is empty **/
    public static final String DEFAULT_FILE_NAME    = "ImageSDCardCacheFile.jpg";
    /** max length of file name, not include suffix **/
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
