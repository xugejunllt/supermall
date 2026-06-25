package com.lanf.dynamicsrrefresh.core.store.impl;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.lanf.dynamicsrrefresh.core.store.StoreService;
import com.lanf.dynamicsrrefresh.core.util.StrUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class NacosStoreServiceImpl implements StoreService {


    private ConfigService configService;

    public NacosStoreServiceImpl(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * 新增配置
     *
     *
     *
     */
    @Override
    public boolean publishConfig(String dataId, String group, String content) {

        log.info("添加配置到nacos:dataId:{},group:{},content:{}", dataId, group, content);
        String config = getConfig(dataId, group);
        //多个内容
        content = content + "\n";
        if (config != null) {
            //旧的+新的进行覆盖更新
            content = config + content;
        }
        return publishCoverConfig( dataId,  group,  content);
    }

    /**
     *
     * 发布覆盖配置
     *
     *
     */
    private boolean publishCoverConfig(String dataId, String group, String content){
        boolean publishConfig = false;
        try {

            publishConfig = configService.publishConfig(dataId, group, content, type);
        } catch (NacosException e) {
            log.error("nacos发布配置异常,dataId:{},group:{}", dataId, group, e);
            throw new RuntimeException("nacos发布异常");
        }
        return publishConfig;
    }
    @Override
    public String getConfig(String dataId, String group) {

        log.info("查询nacos配置:dataId:{},group:{}", dataId, group);
        String config = null;
        try {
            config = configService.getConfig(dataId, group, timeoutMs);
        } catch (NacosException e) {
            log.error("nacos查询配置异常,dataId:{},group:{}", dataId, group, e);
            throw new RuntimeException("nacos查询异常");
        }

        return config;
    }

    @Override
    public List<String> contentList(String dataId, String group) {

        String config = getConfig(dataId, group);
        if (config == null) {

            return new ArrayList<>();
        }

        return StrUtils.toList(config, "\\n");
    }

    @Override
    public boolean deleteContent(String dataId, String group, String content) {

        List<String> contentList = contentList(dataId, group);
        StringBuilder contentBuffer = new StringBuilder();
        for (String c : contentList) {
            if (!c.equals(content)) {
                //没有删除的配置内容重新添加
                contentBuffer.append(c).
                        append("\n");
            }
        }
        return publishCoverConfig( dataId,  group,  contentBuffer.toString());
    }

    @Override
    public void addListener(String dataId, String group, Listener listener) {

        try {
            configService.addListener(dataId,group,listener);
        } catch (NacosException e) {
            log.error("添加监听器失败,dataId:{},group:{}", dataId, group, e);
        }
    }

}
