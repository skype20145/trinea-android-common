package com.trinea.android.common.serviceImpl;

import com.trinea.android.common.entity.CacheObject;
import com.trinea.android.common.service.CacheFullRemoveType;
import com.trinea.android.common.utils.ObjectUtils;

/**
 * Remove type when cache is full.<br/>
 * when cache is full, compare data of object in cache, if data is smaller remove it first.<br/>
 * 
 * @author Trinea 2011-12-26
 */
public class RemoveTypeDataSmall<T> implements CacheFullRemoveType<T> {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(CacheObject<T> obj1, CacheObject<T> obj2) {
        return ObjectUtils.compare(obj1.getData(), obj2.getData());
    }
}
