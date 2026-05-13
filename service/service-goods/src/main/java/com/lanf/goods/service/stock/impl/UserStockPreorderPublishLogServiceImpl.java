package com.lanf.goods.service.stock.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.goods.mapper.UserStockPreorderPublishLogMapper;
import com.lanf.goods.model.dto.RecycleStockDTO;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.entity.UserStockPreorderPublishLogDO;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.service.stock.IUserStockPreorderPublishLogService;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.model.enums.PublishStatusEnum;
import com.lanf.storage.model.enums.StockPreorderEventTypeEnum;
import com.lanf.storage.mq.constant.StorageClientTopicName;
import com.lanf.storage.mq.message.RecycleStockMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 库存预售发布记录 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-05
 */
@Service
public class UserStockPreorderPublishLogServiceImpl extends ServiceImpl<UserStockPreorderPublishLogMapper, UserStockPreorderPublishLogDO> implements IUserStockPreorderPublishLogService {

    @Autowired
    private IStockService stockService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void recycleStock(RecycleStockDTO recycleStockDTO) {
        String skuCode = recycleStockDTO.getSkuCode();
        Integer quantity = recycleStockDTO.getChangeQuantity();
        Long warehouseId = recycleStockDTO.getWarehouseId();

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

        UserStockPreorderPublishLogDO publishLogDO = buildStockPreorderPublishLogDO( recycleStockDTO,  one);
        RecycleStockMessage publishStockMessage = buildPublishStockMessage(recycleStockDTO,one,publishLogDO.getFlowNo());

        Integer updatePreStock = one.getUsableStock() - quantity;
        boolean update = stockService.lambdaUpdate()
                .eq(StockDO::getId, one.getId())
                .eq(StockDO::getVersion, one.getVersion())
                .set(StockDO::getUsableStock, updatePreStock)
                .set(StockDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新库存失败");
            throw new BizException("更新库存失败");
        }
        this.save(publishLogDO);
        rocketMqClient.sendMessage(StorageClientTopicName.RECYCLE_STOCK_TOPIC, JsonUtils.toJsonString(publishStockMessage));

    }

    private UserStockPreorderPublishLogDO buildStockPreorderPublishLogDO(RecycleStockDTO publishStock, StockDO one){
        String flowNo = CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.PUBLISH_PREORDER_STOCK_FLOW) ;
        UserStockPreorderPublishLogDO stockPreorderPublishLogDO = new UserStockPreorderPublishLogDO();
        stockPreorderPublishLogDO.setFlowNo(flowNo);
        stockPreorderPublishLogDO.setStockId(one.getId());
        stockPreorderPublishLogDO.setSkuCode(publishStock.getSkuCode());
        stockPreorderPublishLogDO.setChangeQuantity(publishStock.getChangeQuantity());
        stockPreorderPublishLogDO.setEventType(StockPreorderEventTypeEnum.RECYCLE);
        stockPreorderPublishLogDO.setPublishPlatform(publishStock.getPublishPlatform());
        stockPreorderPublishLogDO.setWarehouseId(publishStock.getWarehouseId());
        stockPreorderPublishLogDO.setMerchantId(publishStock.getMerchantId());
        stockPreorderPublishLogDO.setStatus(PublishStatusEnum.SUCCESS);

        return stockPreorderPublishLogDO;
    }

    private RecycleStockMessage buildPublishStockMessage(RecycleStockDTO publishStock, StockDO one, String flowNo){
        RecycleStockMessage publishStockMessage = new RecycleStockMessage();
        publishStockMessage.setFlowNo(flowNo);
        publishStockMessage.setSkuCode(publishStock.getSkuCode());
        publishStockMessage.setChangeQuantity(publishStock.getChangeQuantity());
        publishStockMessage.setWarehouseId(publishStock.getWarehouseId());
        publishStockMessage.setEventType(StockPreorderEventTypeEnum.RECYCLE);
        publishStockMessage.setPublishPlatform(publishStock.getPublishPlatform());

        return publishStockMessage;
    }
}
