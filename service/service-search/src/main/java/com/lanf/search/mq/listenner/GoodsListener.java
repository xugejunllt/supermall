package com.lanf.search.mq.listenner;

import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.SyncGoodsInfoToEsMsg;
import com.lanf.search.model.document.GoodsDocument;
import com.lanf.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.SAVE_GOODS_ES_TOPIC,
        consumerGroup = TopicName.SAVE_GOODS_ES__GROUP, consumeMode = ConsumeMode.ORDERLY)
public class GoodsListener implements RocketMQListener<SyncGoodsInfoToEsMsg> {

    @Autowired
    private GoodsRepository goodsRepository;

    @Override
    public void onMessage(SyncGoodsInfoToEsMsg message) {
        long startTime = System.currentTimeMillis();
        log.info("开始同步商品数据到ES, goodsId: {}, skuId: {}", message.getGoodsId(), message.getSkuId());

        try {
            GoodsDocument goodsDocument = BeanCopyUtils.copyBean(message, GoodsDocument.class);
            

            List<GoodsDocument.Attribute> attributeList = new ArrayList<>();
            message.getAttributeList().forEach(attr -> {
                GoodsDocument.Attribute attribute = new GoodsDocument.Attribute();
                attribute.setAttrName(attr.getAttribute());
                attribute.setAttrValue(attr.getAttributeValue());
                attribute.setSkuId(attr.getSkuId());
                attributeList.add(attribute);
            });
            goodsDocument.setAttributes(attributeList);
            log.info("同步到ES的数据是{}", goodsDocument);
            goodsRepository.save(goodsDocument);

        } catch (Exception e) {
            long totalCostTime = System.currentTimeMillis() - startTime;
            log.error("商品数据同步到ES失败, goodsId: {}, skuId: {}, 耗时: {}ms, 错误信息: {}", 
                    message.getGoodsId(), message.getSkuId(), totalCostTime, e.getMessage(), e);
            throw new MessageRetryConsumeException("商品数据同步到ES失败");
        }
    }


}