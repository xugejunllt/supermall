package com.lanf.order.service.shipping.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.utils.IdUtils;
import com.lanf.order.mapper.ShippingTrackMapper;
import com.lanf.order.model.bo.AddShippingTrackBO;
import com.lanf.order.model.bo.BathAddShippingTrackBO;
import com.lanf.order.model.entity.ShippingTrackDO;
import com.lanf.order.service.shipping.IShippingInfoService;
import com.lanf.order.service.shipping.IShippingTrackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
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


    @Override
    public void bathAddShippingTrack(BathAddShippingTrackBO bo) {

        log.info("批量插入参数:{}",bo);
        List<AddShippingTrackBO> shippingTrackBOList = bo.getShippingTrackBOList();
        List<ShippingTrackDO> trackDOList = new ArrayList<>(shippingTrackBOList.size());

        Date now = new Date();
        for (AddShippingTrackBO shippingTrackBO : shippingTrackBOList) {

            ShippingTrackDO trackDO = getShippingTrackDO(shippingTrackBO, bo);
            trackDO.setId(IdUtils.generateId());
            trackDO.setCreateTime(now);
            trackDO.setUpdateTime(now);
            trackDO.setIsDeleted(0);
            trackDOList.add(trackDO);
        }
        log.info("批量插入的数据:{}", trackDOList);
        baseMapper.insertIgnoreBatch(trackDOList);

    }


    private static ShippingTrackDO getShippingTrackDO(AddShippingTrackBO shippingTrackBO,BathAddShippingTrackBO bo) {
        ShippingTrackDO trackDO = new ShippingTrackDO();
        trackDO.setOrderId(bo.getOrderId());
        trackDO.setStatus(shippingTrackBO.getStatus());
        trackDO.setUserId(bo.getUserId());
        trackDO.setBaseTrackStatus(shippingTrackBO.getBaseTrackStatus());
        trackDO.setAdvancedTrackStatus(null);
        trackDO.setFinishTime(shippingTrackBO.getFinishTime());
        trackDO.setFinishContent(shippingTrackBO.getFinishContent());
        trackDO.setTenantId(bo.getTenantId());
        trackDO.setFlowNo(shippingTrackBO.getFlowNo());
        return trackDO;
    }
}
