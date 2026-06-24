package com.lanf.dynamicsrrefresh.gatewayfilter.listener;

import com.lanf.dynamicsrrefresh.core.handle.EventHandle;
import com.lanf.dynamicsrrefresh.core.listener.NacosListener;
import com.lanf.dynamicsrrefresh.core.model.enums.BizCodeEnum;
import com.lanf.dynamicsrrefresh.core.util.BeanUtil;
import com.lanf.dynamicsrrefresh.gatewayfilter.handle.IpBlackListHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IpBlackListListener implements NacosListener {


    @Override
    public String getDataId() {
        return BizCodeEnum.IP_BLACK_LIST.getDateId();
    }

    @Override
    public String getGroup() {
        return BizCodeEnum.IP_BLACK_LIST.getGroup();
    }

    @Override
    public EventHandle getEventHandle() {

        return BeanUtil.getBean(IpBlackListHandle.class);
    }
}
