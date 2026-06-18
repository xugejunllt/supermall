package com.lanf.order.service.shipping;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.order.model.vo.ShippingTrackVO;
import com.lanf.order.model.bo.BathAddShippingTrackBO;
import com.lanf.order.model.entity.ShippingTrackDO;

import java.util.List;

/**
 * <p>
 * 物流轨迹 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
public interface IShippingTrackService extends IService<ShippingTrackDO> {

    /**
     * 插入物流轨迹
     *
     */
    void bathAddShippingTrack(BathAddShippingTrackBO bo);

    List<ShippingTrackVO> findShippingTrack(Long orderId);
}
