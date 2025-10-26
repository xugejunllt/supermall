package com.lanf.logistics.service.manager;

import com.lanf.rocketmq.model.bo.ExpressPushBO;

/**
 * 物流信息推送成功回调
 */
public interface ExpressPushSuccessCallback {

    void callback(ExpressPushBO expressPushBO);

}
