package com.lanf.order.service.shipping.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.order.model.bo.ShippingInfoBO;
import com.lanf.api.order.model.enums.ShippingStatusEnum;
import com.lanf.api.order.model.vo.ShippingTrackContentVO;
import com.lanf.api.order.model.vo.ShippingTrackVO;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.utils.IdUtils;
import com.lanf.order.mapper.ShippingTrackMapper;
import com.lanf.order.model.bo.AddShippingTrackBO;
import com.lanf.order.model.bo.BathAddShippingTrackBO;
import com.lanf.order.model.entity.ShippingInfoDO;
import com.lanf.order.model.entity.ShippingTrackDO;
import com.lanf.order.model.vo.ShippingTrackDetailVO;
import com.lanf.order.service.shipping.IShippingInfoService;
import com.lanf.order.service.shipping.IShippingTrackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private RedissonCacheService redissonCacheService;

    private static final String SHIPPING_TRACK_CACHE_KEY_PREFIX = "shippingTrack:";
    private static final String SHIPPING_TRACK_DETAIL_CACHE_KEY_PREFIX = "shippingTrackDetail:";


    @Override
    public void bathAddShippingTrack(BathAddShippingTrackBO bo) {

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
        baseMapper.insertIgnoreBatch(trackDOList);
        //构建缓存
        loadShippingTrackDetailFromDB(bo.getOrderId());
    }

    @Override
    public ShippingTrackDetailVO shippingTrackDetailQuery(Long orderId) {
        String cacheKey = SHIPPING_TRACK_DETAIL_CACHE_KEY_PREFIX + orderId;
        // 1. 从缓存中读取
        String cacheValue = redissonCacheService.get(cacheKey);
        if (cacheValue != null && !RedissonCacheService.isErrorValue(cacheValue)) {
            return JsonUtils.toObject(cacheValue, ShippingTrackDetailVO.class);
        }
        return loadShippingTrackDetailFromDB( orderId);
    }
    private ShippingTrackDetailVO loadShippingTrackDetailFromDB(Long orderId){

        String cacheKey = SHIPPING_TRACK_DETAIL_CACHE_KEY_PREFIX + orderId;


        ShippingTrackDetailVO shippingTrackDetailVO = new ShippingTrackDetailVO();

        // 查询物流信息
        ShippingInfoDO shippingInfoDO = shippingInfoService.lambdaQuery()
                .eq(ShippingInfoDO::getOrderId, orderId)
                .one();
        if (shippingInfoDO != null) {
            ShippingInfoBO shippingInfoBO = new ShippingInfoBO();
            shippingInfoBO.setLogisticsCompany(shippingInfoDO.getLogisticsCompany());
            shippingInfoBO.setTrackingNumber(shippingInfoDO.getTrackingNumber());
            shippingTrackDetailVO.setShippingInfoBO(shippingInfoBO);
        }

        List<ShippingTrackVO> shippingTrack = findShippingTrackFromDB(orderId);
        shippingTrackDetailVO.setTrackVOList(shippingTrack);

        // 2. 写入缓存，过期时间7天
        redissonCacheService.set(cacheKey, JsonUtils.toJsonString(shippingTrackDetailVO), 7, TimeUnit.DAYS);

        return shippingTrackDetailVO;
    }

    private static ShippingTrackDO getShippingTrackDO(AddShippingTrackBO shippingTrackBO, BathAddShippingTrackBO bo) {
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

    @Override
    public List<ShippingTrackVO> findShippingTrack(Long orderId) {



        return findShippingTrackFromDB(orderId);
    }

    private List<ShippingTrackVO> findShippingTrackFromDB(Long orderId) {


        List<ShippingTrackDO> trackDOList = this.lambdaQuery()
                .eq(ShippingTrackDO::getOrderId, orderId)
                .list();

        // 3. 按status分组，并构建VO
        Map<ShippingStatusEnum, List<ShippingTrackDO>> statusGroup = trackDOList.stream()
                .collect(Collectors.groupingBy(ShippingTrackDO::getStatus));

        List<ShippingTrackVO> trackVOList = new ArrayList<>();
        for (Map.Entry<ShippingStatusEnum, List<ShippingTrackDO>> entry : statusGroup.entrySet()) {
            ShippingTrackVO trackVO = new ShippingTrackVO();
            trackVO.setStatus(entry.getKey());

            // 相同status的内容按finishTime降序
            List<ShippingTrackContentVO> contentVOList = entry.getValue().stream()
                    .sorted((a, b) -> b.getFinishTime().compareTo(a.getFinishTime()))
                    .map(doItem -> {
                        ShippingTrackContentVO contentVO = new ShippingTrackContentVO();
                        contentVO.setFinishTime(doItem.getFinishTime());
                        contentVO.setFinishContent(doItem.getFinishContent());
                        return contentVO;
                    })
                    .collect(Collectors.toList());

            trackVO.setTrackContentVOList(contentVOList);
            trackVOList.add(trackVO);
        }

        // 4. 按ShippingStatusEnum code值降序排序
        trackVOList.sort((a, b) -> b.getStatus().getCode().compareTo(a.getStatus().getCode()));

        return trackVOList;
    }


}
