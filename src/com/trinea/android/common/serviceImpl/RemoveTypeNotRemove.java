/*
 * Copyright 2012 Trinea.com All right reserved. This software is the
 * confidential and proprietary information of Trinea.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Trinea.com.
 */
package com.trinea.android.common.serviceImpl;

import com.trinea.android.common.entity.CacheObject;
import com.trinea.android.common.service.CacheFullRemoveType;

/**
 * 缓存满时删除数据的类型--不删除
 * 
 * @author Trinea 2012-5-10 上午01:15:50
 */
public class RemoveTypeNotRemove<T> implements CacheFullRemoveType<T> {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(CacheObject<T> obj1, CacheObject<T> obj2) {
        return 0;
    }
}
