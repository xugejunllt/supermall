package com.lanf.dynamicsrrefresh.gatewayfilter.handle;

import com.lanf.dynamicsrrefresh.core.handle.EventHandle;
import com.lanf.dynamicsrrefresh.core.model.enums.BizCodeEnum;
import com.lanf.dynamicsrrefresh.core.store.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class CloseServiceHandle implements EventHandle {


    //关闭的服务列表
    private static final List<String> serviceNameList = new CopyOnWriteArrayList<>();

    @Autowired
    private StoreService storeService;


    @Override
    public void load() {
        log.info("重写加载nacos配置");

        List<String> newList = storeService.contentList(BizCodeEnum.CLOSE_SERVICE.getDateId(), BizCodeEnum.CLOSE_SERVICE.getGroup());
        serviceNameList.clear();
        serviceNameList.addAll(newList);

    }

    /**
     * 重新加载配置
     */
    @Override
    public void refresh() {
        load();
    }

    public static boolean close(String path){
        //服务请求前缀
        String serviceRequestPre = path.split("/")[1];
        return  serviceNameList.contains(serviceRequestPre);
    }
}
