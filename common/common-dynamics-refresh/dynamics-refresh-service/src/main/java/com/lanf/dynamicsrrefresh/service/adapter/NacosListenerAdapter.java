package com.lanf.dynamicsrrefresh.service.adapter;

import com.lanf.dynamicsrrefresh.core.handle.EventHandle;
import com.lanf.dynamicsrrefresh.core.listener.NacosListener;
import org.springframework.stereotype.Component;

/**
 * 注册空的一个nacos监听器 让程序正常启动
 */
@Component
public class NacosListenerAdapter implements NacosListener {
    @Override
    public String getDataId() {
        return "serviceStart";
    }

    @Override
    public String getGroup() {
        return "SERVICE_START";
    }

    @Override
    public EventHandle getEventHandle()
    {
        return new EventHandleAdapter();
    }
}
