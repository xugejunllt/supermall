package com.lanf.order.service.shipping.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.order.mapper.ShippingTrackMapper;
import com.lanf.order.model.bo.AddShippingTrackBO;
import com.lanf.order.model.bo.BathAddShippingTrackBO;
import com.lanf.order.model.entity.ShippingInfoDO;
import com.lanf.order.model.entity.ShippingTrackDO;
import com.lanf.order.service.shipping.IShippingInfoService;
import com.lanf.order.service.shipping.IShippingTrackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 物流轨迹 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
@Slf4j
@Service
public class ShippingTrackServiceImpl extends ServiceImpl<ShippingTrackMapper, ShippingTrackDO> implements IShippingTrackService {

    @Autowired
    private IShippingInfoService shippingInfoService;

    @Autowired
    @Override
    public void bathAddShippingTrack(BathAddShippingTrackBO bo) {

        Long orderId = bo.getOrderId();
        ShippingInfoDO infoDO = shippingInfoService.getById(orderId);
        if (infoDO == null) {
            log.error("订单物流信息不存在");
            return;
        }
        List<AddShippingTrackBO> shippingTrackBOList = bo.getShippingTrackBOList();
        List<ShippingTrackDO> trackDOList = new ArrayList<>(shippingTrackBOList.size());

        for (AddShippingTrackBO shippingTrackBO : shippingTrackBOList){

            ShippingTrackDO trackDO = getShippingTrackDO(shippingTrackBO, orderId, infoDO);
            trackDOList.add(trackDO);
        }
        this.saveBatch(trackDOList);


    }


    private static ShippingTrackDO getShippingTrackDO(AddShippingTrackBO shippingTrackBO, Long orderId, ShippingInfoDO infoDO) {
        ShippingTrackDO trackDO = new ShippingTrackDO();
        trackDO.setOrderId(orderId);
        trackDO.setStatus(shippingTrackBO.getStatus());
        trackDO.setUserId(infoDO.getUserId());
        trackDO.setBaseTrackStatus(shippingTrackBO.getBaseTrackStatus());
        trackDO.setAdvancedTrackStatus(null);
        trackDO.setFinishTime(shippingTrackBO.getFinishTime());
        trackDO.setFinishContent(shippingTrackBO.getFinishContent());
        trackDO.setTenantId(infoDO.getTenantId());
        return trackDO;
    }
}
