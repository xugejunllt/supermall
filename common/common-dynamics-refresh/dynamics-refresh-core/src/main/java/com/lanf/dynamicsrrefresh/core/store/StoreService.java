package com.lanf.dynamicsrrefresh.core.store;

import com.alibaba.nacos.api.config.listener.Listener;

import java.util.List;

public interface StoreService {

    final String   type = "text";
    final long timeoutMs = 1000;

    /**
     * 发布配置 如果不存在
     *
     *
     *
     *
     */
    boolean publishConfig(String dataId, String group, String content) ;

    String getConfig(String dataId, String group) ;
    List<String> contentList(String dataId, String group) ;

    boolean deleteContent(String dataId, String group,String content);

    void addListener(String dataId, String group, Listener listener);

}
