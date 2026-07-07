package com.lanf.storage.service.stock.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.storage.model.dto.PublishStockDTO;
import com.lanf.api.storage.model.query.StockPreorderPublishLogPageQuery;
import com.lanf.api.storage.model.vo.StockPreorderPublishLogPageVO;
import com.lanf.api.storage.mq.constant.StorageClientTopicName;
import com.lanf.api.storage.mq.message.PublishStockMessage;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.constant.model.enums.storage.PublishStatusEnum;
import com.lanf.constant.model.enums.storage.StockPreorderEventTypeEnum;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.UserContext;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.mapper.StockPreorderPublishLogMapper;
import com.lanf.storage.model.entity.StockDO;
import com.lanf.storage.model.entity.StockPreorderPublishLogDO;
import com.lanf.storage.model.entity.WarehouseDO;
import com.lanf.storage.service.stock.IStockPreorderPublishLogService;
import com.lanf.storage.service.stock.IStockService;
import com.lanf.storage.service.warehous.IWarehouseService;
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
    @Autowired
    private IWarehouseService warehouseService;
    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;

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

        mqSendMessageUtils.sendMessage(StorageClientTopicName.PUBLISH_STOCK_TOPIC,
                JsonUtils.toJsonString(publishStockMessage),null);

    }

    @Override
    public PageResult<StockPreorderPublishLogPageVO> stockPreorderPublishLogPageQuery(StockPreorderPublishLogPageQuery query) {

        IPage<StockPreorderPublishLogDO> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<StockPreorderPublishLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(!org.apache.commons.lang3.StringUtils.isEmpty(query.getSkuCode()), StockPreorderPublishLogDO::getSkuCode, query.getSkuCode())
                .eq(query.getStockId() != null, StockPreorderPublishLogDO::getStockId, query.getStockId())
                .eq(query.getWarehouseId() != null, StockPreorderPublishLogDO::getWarehouseId, query.getWarehouseId())
                .eq(query.getPublishPlatform() != null, StockPreorderPublishLogDO::getPublishPlatform, query.getPublishPlatform())
                .eq(query.getStatus() != null, StockPreorderPublishLogDO::getStatus, query.getStatus())
                .orderByDesc(StockPreorderPublishLogDO::getCreateTime);

        IPage<StockPreorderPublishLogDO> result = this.page(page, wrapper);

        PageResult<StockPreorderPublishLogPageVO> pageResult = new PageResult<>();
        pageResult.setRecords(BeanCopyUtils.copyBeanList(result.getRecords(), StockPreorderPublishLogPageVO.class));
        pageResult.setTotal(result.getTotal());
        pageResult.setSize(result.getSize());

        return pageResult;
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
        stockPreorderPublishLogDO.setWarehouseName(one.getWarehouseName());
        return stockPreorderPublishLogDO;
    }

    private PublishStockMessage buuildPublishStockMessage(PublishStockDTO publishStock, StockDO one,
                                                          String flowNo){

        WarehouseDO warehouseDO = warehouseService.getById(publishStock.getWarehouseId());


        PublishStockMessage publishStockMessage = new PublishStockMessage();
        publishStockMessage.setFlowNo(flowNo);
        publishStockMessage.setSkuCode(publishStock.getSkuCode());
        publishStockMessage.setChangeQuantity(publishStock.getChangeQuantity());
        publishStockMessage.setWarehouseId(publishStock.getWarehouseId());
        publishStockMessage.setTenantId(UserContext.getTenantId());
        publishStockMessage.setWarehouseName(one.getWarehouseName());
        publishStockMessage.setEventType(StockPreorderEventTypeEnum.PUBLISH);
        publishStockMessage.setPublishPlatform(publishStock.getPublishPlatform());
        publishStockMessage.setGoodsId(publishStock.getGoodsId());
        publishStockMessage.setAreaCode(warehouseDO.getAreaCode());
        publishStockMessage.setLatitude(warehouseDO.getLatitude());
        publishStockMessage.setLongitude(warehouseDO.getLongitude());
        return publishStockMessage;
    }
}
