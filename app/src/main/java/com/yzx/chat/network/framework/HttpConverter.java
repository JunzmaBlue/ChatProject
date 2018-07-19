package com.yzx.chat.network.framework;

import android.support.annotation.Nullable;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by YZX on 2017年10月15日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public interface HttpConverter {

    @Nullable
    byte[] convertRequest(Map<String, Object> requestParams);

    @Nullable
    byte[] convertMultipartRequest(String partName,Object body);

    @Nullable
    Object convertResponseBody(byte[] body, Type genericType);

}
