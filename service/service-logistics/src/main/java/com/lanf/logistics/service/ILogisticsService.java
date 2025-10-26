package com.lanf.logistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.logistics.model.dto.LogisticsAddDTO;
import com.lanf.logistics.model.entity.LogisticsDO;
import com.lanf.logistics.model.vo.LogisticsVO;
import com.lanf.logistics.service.manager.ExpressPushSuccessCallback;
import com.lanf.rocketmq.model.message.PaySuccessEventMessage;

/**
 * <p>
 * 物流信息 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-25
 */
public interface ILogisticsService extends IService<LogisticsDO>   {


    void logisticsAdd(LogisticsAddDTO addDTO);
    LogisticsVO logisticsDetail(Long orderId);

    void  paySuccessHandle(PaySuccessEventMessage paySuccessEventMessage);

}
