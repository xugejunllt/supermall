package com.lanf.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.rocketmq.model.message.SyncGoodsInfoToEsMsg;
import com.lanf.search.model.entity.GoodsInfoDO;

/**
 * <p>
 * 商品同步es的数据 服务类
 * </p>
 *
 * @author jarven
 * @since 2025-12-06
 */
public interface IGoodsInfoService extends IService<GoodsInfoDO> {


    void saveGoodsInfo(SyncGoodsInfoToEsMsg msg);



}
