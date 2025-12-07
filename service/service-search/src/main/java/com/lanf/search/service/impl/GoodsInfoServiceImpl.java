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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        List<GoodsDocument.Attribute> attributes = BeanCopyUtils.copyBeanList(msg.getAttributes(),
                GoodsDocument.Attribute.class);
        goodsDocument.setAttributes(filterAttribute(attributes));

        GoodsInfoDO goodsInfoDO = new GoodsInfoDO();
        goodsInfoDO.setGoodsId(System.currentTimeMillis());
        //goodsInfoDO.setGoodsId(msg.getGoodsId());
        goodsInfoDO.setVersion(goodsDocument.getVersion());
        goodsInfoDO.setGoodsInfo(JsonUtils.toJsonString(goodsDocument));

        this.save(goodsInfoDO);

    }

    private List<GoodsDocument.Attribute> filterAttribute(List<GoodsDocument.Attribute> attributes) {
        //传过来的sku属性可能重复，进行去重 去重key：attr.getAttrName() + "_" + attr.getAttrValue()

        return attributes.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                attr -> attr.getAttrName() + "_" + attr.getAttrValue(), // 唯一key
                                attr -> attr,
                                (existing, replacement) -> existing // 重复时保留第一个
                        ),
                        map -> new ArrayList<>(map.values())
                ));

    }
}
