package com.lanf.dynamicsrrefresh.core.runner;

import com.alibaba.nacos.api.config.listener.Listener;
import com.lanf.dynamicsrrefresh.core.handle.EventHandle;
import com.lanf.dynamicsrrefresh.core.listener.DefaultNacosListener;
import com.lanf.dynamicsrrefresh.core.listener.NacosListener;
import com.lanf.dynamicsrrefresh.core.store.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
public class InitNacosListenner implements CommandLineRunner {

    @Autowired
    private StoreService storeService;
    @Autowired
    private Map<String, NacosListener> listenerMap;

    @Override
    public void run(String... args) {
        log.info("容器启动，添加nacos配置监听器");

        Collection<NacosListener> values = listenerMap.values();

        for (NacosListener listener : values) {

            //注册配置变更监听器
            EventHandle eventHandle = listener.getEventHandle();
            Listener listener1 = new DefaultNacosListener(eventHandle);

            storeService.addListener(listener.getDataId(), listener.getGroup(), listener1);
            //加载配置
            eventHandle.load();

        }


    }
}
