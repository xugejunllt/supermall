package com.lanf.search.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.rocketmq.model.message.SyncGoodsInfoToEsMsg;
import com.lanf.search.mapper.GoodsInfoMapper;
import com.lanf.search.model.document.GoodsDocument;
import com.lanf.search.model.entity.GoodsInfoDO;
import com.lanf.search.repository.GoodsRepository;
import com.lanf.search.service.IGoodsInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品同步es的数据 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2025-12-06
 */
@Service
public class GoodsInfoServiceImpl extends ServiceImpl<GoodsInfoMapper, GoodsInfoDO> implements IGoodsInfoService {


    @Autowired
    private GoodsRepository goodsRepository;


    @Override
    public void saveGoodsInfo(SyncGoodsInfoToEsMsg msg) {


        GoodsDocument goodsDocument = BeanCopyUtils.copyBean(msg, GoodsDocument.class);
        //随机生成 用户测试
        goodsDocument.setGoodsId(System.currentTimeMillis());
        //list对象无法copy 手动复制


        GoodsInfoDO goodsInfoDO = new GoodsInfoDO();
        //goodsInfoDO.setGoodsId(System.currentTimeMillis());
        goodsInfoDO.setGoodsId(msg.getGoodsId());
        goodsInfoDO.setVersion(goodsDocument.getVersion());
        goodsInfoDO.setGoodsInfo(JsonUtils.toJsonString(goodsDocument));

        this.save(goodsInfoDO);

    }


}
