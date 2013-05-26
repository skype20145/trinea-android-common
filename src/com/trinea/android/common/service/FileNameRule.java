package com.trinea.android.common.service;

import java.io.Serializable;

import com.trinea.android.common.serviceImpl.ImageSDCardCache;

/**
 * File name rule, used when save image to sdcard in {@link ImageSDCardCache}
 * 
 * @author Trinea 2012-7-6
 */
public interface FileNameRule extends Serializable {

    /**
     * get file name, include suffix, it's optional to include folder.
     * 
     * @param imageUrl the url of image
     * @return
     */
    public String getFileName(String imageUrl);
}
