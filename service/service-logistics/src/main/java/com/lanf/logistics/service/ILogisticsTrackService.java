package com.lanf.logistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.logistics.model.entity.LogisticsTrackDO;
import com.lanf.logistics.service.manager.ExpressPushSuccessCallback;
import com.lanf.rocketmq.model.bo.ExpressPushBO;
import com.lanf.rocketmq.model.message.LogisticsTrackBathAddDTO;

import java.util.List;

/**
 * <p>
 * 物流轨迹 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-25
 */
public interface ILogisticsTrackService extends IService<LogisticsTrackDO>, ExpressPushSuccessCallback {

    void  LogisticsTrackAdd(ExpressPushBO expressPushBO);

    void logisticsTrackBathAdd(List<LogisticsTrackBathAddDTO> addDTOList);




}
