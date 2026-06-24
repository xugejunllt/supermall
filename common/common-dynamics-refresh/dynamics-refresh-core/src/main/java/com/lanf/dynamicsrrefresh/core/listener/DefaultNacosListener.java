package com.lanf.dynamicsrrefresh.core.listener;

import com.alibaba.nacos.api.config.listener.Listener;
import com.lanf.dynamicsrrefresh.core.handle.EventHandle;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

@Slf4j
public class DefaultNacosListener implements Listener {


    private EventHandle eventHandle;

    public DefaultNacosListener(EventHandle eventHandle) {
        this.eventHandle = eventHandle;
    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public void receiveConfigInfo(String configInfo) {

        eventHandle.refresh();
    }
}
