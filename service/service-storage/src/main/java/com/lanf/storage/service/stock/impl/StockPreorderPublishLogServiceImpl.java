package com.lanf.storage.service.stock.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.constant.model.enums.storage.PublishStatusEnum;
import com.lanf.constant.model.enums.storage.StockPreorderEventTypeEnum;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.mapper.StockPreorderPublishLogMapper;
import com.lanf.api.storage.model.dto.PublishStockDTO;
import com.lanf.storage.model.entity.StockDO;
import com.lanf.storage.model.entity.StockPreorderPublishLogDO;
import com.lanf.api.storage.mq.constant.StorageClientTopicName;
import com.lanf.api.storage.mq.message.PublishStockMessage;
import com.lanf.storage.service.stock.IStockPreorderPublishLogService;
import com.lanf.storage.service.stock.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 库存预售发布记录 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-05
 */
@Slf4j
@Service
public class StockPreorderPublishLogServiceImpl extends ServiceImpl<StockPreorderPublishLogMapper, StockPreorderPublishLogDO> implements IStockPreorderPublishLogService {

    @Autowired
    private IStockService stockService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Transactional
    @Override
    public void publishStock(PublishStockDTO publishStock) {

        String skuCode = publishStock.getSkuCode();
        Integer quantity = publishStock.getChangeQuantity();
        Long warehouseId = publishStock.getWarehouseId();

        StockDO one = stockService.lambdaQuery()
                .eq(StockDO::getSkuCode, skuCode)
                .eq(StockDO::getWarehouseId, warehouseId)
                .one();
        if (one == null) {
            log.error("库存不存在");
            throw new BizException("库存不存在");
        }
        Integer usableStock = one.getUsableStock();
        if (usableStock < quantity) {
            log.warn("库存不足");
            throw new BizException("库存不足");
        }


        StockPreorderPublishLogDO publishLogDO = buildStockPreorderPublishLogDO( publishStock,  one);
        PublishStockMessage publishStockMessage = buuildPublishStockMessage(publishStock,one,publishLogDO.getFlowNo());

        Integer updatePreStock = one.getPreStock()+ quantity;
        Integer updateUsableStock = usableStock - quantity;
        boolean update = stockService.lambdaUpdate()
                .eq(StockDO::getId, one.getId())
                .eq(StockDO::getVersion, one.getVersion())
                .set(StockDO::getPreStock, updatePreStock)
                .set(StockDO::getUsableStock, updateUsableStock)
                .set(StockDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新库存失败");
            throw new BizException("更新库存失败");
        }
        this.save(publishLogDO);
        rocketMqClient.sendMessage(StorageClientTopicName.PUBLISH_STOCK_TOPIC, JsonUtils.toJsonString(publishStockMessage));

    }

    private StockPreorderPublishLogDO buildStockPreorderPublishLogDO(PublishStockDTO publishStock, StockDO one){
        String flowNo = CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.PUBLISH_PREORDER_STOCK_FLOW) ;
        StockPreorderPublishLogDO stockPreorderPublishLogDO = new StockPreorderPublishLogDO();
        stockPreorderPublishLogDO.setFlowNo(flowNo);
        stockPreorderPublishLogDO.setStockId(one.getId());
        stockPreorderPublishLogDO.setSkuCode(publishStock.getSkuCode());
        stockPreorderPublishLogDO.setChangeQuantity(publishStock.getChangeQuantity());
        stockPreorderPublishLogDO.setEventType(StockPreorderEventTypeEnum.PUBLISH);
        stockPreorderPublishLogDO.setPublishPlatform(publishStock.getPublishPlatform());
        stockPreorderPublishLogDO.setWarehouseId(publishStock.getWarehouseId());
        stockPreorderPublishLogDO.setStatus(PublishStatusEnum.SUCCESS);

        return stockPreorderPublishLogDO;
    }

    private PublishStockMessage buuildPublishStockMessage(PublishStockDTO publishStock, StockDO one,String flowNo){
        PublishStockMessage publishStockMessage = new PublishStockMessage();
        publishStockMessage.setFlowNo(flowNo);
        publishStockMessage.setSkuCode(publishStock.getSkuCode());
        publishStockMessage.setChangeQuantity(publishStock.getChangeQuantity());
        publishStockMessage.setWarehouseId(publishStock.getWarehouseId());
        publishStockMessage.setMerchantId(publishStock.getMerchantId());
        publishStockMessage.setGoodsName(one.getGoodsName());
        publishStockMessage.setUnit(one.getUnit());
        publishStockMessage.setWarehouseName(one.getWarehouseName());
        publishStockMessage.setEventType(StockPreorderEventTypeEnum.PUBLISH);
        publishStockMessage.setPublishPlatform(publishStock.getPublishPlatform());

        return publishStockMessage;
    }
}
