package com.lanf.storage.service.storage.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.IdUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.finance.mq.message.SalesInStockOrderAddMessage;
import com.lanf.finance.mq.message.SalesInStockOrderItemAdd;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.storage.mapper.AftersalesIntStockOrderMapper;
import com.lanf.storage.model.dto.AfterSalesIntStockDTO;
import com.lanf.storage.model.entity.AfterSalesIntStockOrderDO;
import com.lanf.storage.model.entity.InOutStockOrderItemDO;
import com.lanf.storage.service.storage.IAfterSalesIntStockOrderService;
import com.lanf.storage.service.storage.IInOutStockOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 售后出库单 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-24
 */
@Service
public class AftersalesIntStockOrderServiceImpl extends ServiceImpl<AftersalesIntStockOrderMapper, AfterSalesIntStockOrderDO> implements IAfterSalesIntStockOrderService {


    @Autowired
    private IInOutStockOrderItemService iInOutStockOrderItemService;

    @Override
    public void addAfterSalesIntStockOrder(SalesInStockOrderAddMessage message) {

        List<InOutStockOrderItemDO> inOutStockOrderItemDOList = new ArrayList<>();

        AfterSalesIntStockOrderDO one = this.lambdaQuery().eq(AfterSalesIntStockOrderDO::
                getAfterSalesOrderId, message.getAfterSalesOrderId()).one();
        if (one != null) {
            throw new BizException("售后单已存在");
        }

        List<SalesInStockOrderItemAdd> salesInStockOrderItemAddDTOList = message.getSalesInStockOrderItemAddDTOList();
        int totalQuantity = 0;

        for (SalesInStockOrderItemAdd sales : salesInStockOrderItemAddDTOList) {
            totalQuantity = totalQuantity + sales.getQuantity();

        }
        Long id = IdUtils.generateId();
        AfterSalesIntStockOrderDO afterSalesIntStockOrderDO = new AfterSalesIntStockOrderDO();
        afterSalesIntStockOrderDO.setId(id);
        afterSalesIntStockOrderDO.setCode(message.getAfterSalesOrderId().toString());
        afterSalesIntStockOrderDO.setAfterSalesOrderId(message.getAfterSalesOrderId());
        afterSalesIntStockOrderDO.setExpectQuantity(totalQuantity);
        afterSalesIntStockOrderDO.setStorageStatus(0);
        //
        List<SalesInStockOrderItemAdd> inOutStockOrderItemDTOList = message.getSalesInStockOrderItemAddDTOList();
        inOutStockOrderItemDTOList.forEach(b -> {
            //
            InOutStockOrderItemDO inOutStockOrderItemDO = new InOutStockOrderItemDO();
            inOutStockOrderItemDO.setGoodsName(b.getGoodsName());
            inOutStockOrderItemDO.setSkuCode(b.getSkuCode());
            inOutStockOrderItemDO.setTotalQuantity(b.getQuantity());
            inOutStockOrderItemDO.setSurplusQuantity(b.getQuantity());
            inOutStockOrderItemDO.setUnit(b.getSkuName());
            inOutStockOrderItemDO.setInOutStockOrderId(id);
            inOutStockOrderItemDOList.add(inOutStockOrderItemDO);
        });

        //进行保存
        this.save(afterSalesIntStockOrderDO);
        iInOutStockOrderItemService.saveBatch(inOutStockOrderItemDOList);
    }

    @Transactional
    @Override
    public void inStock(AfterSalesIntStockDTO dto) {

        AfterSalesIntStockOrderDO one = this.getById(dto.getId());
        if (one == null) {
            log.error("售后单不存在");
            throw new BizException("售后单已存在");
        }
        if (one.getStorageStatus()!=0){
            log.warn("售后单已入库");
            throw new BizException("售后单已入库");
        }
        boolean update = this.lambdaUpdate().eq(BaseEntity::getId, dto.getId())
                .eq(AfterSalesIntStockOrderDO::getVersion, one.getVersion())
                .set(AfterSalesIntStockOrderDO::getStorageStatus, 1)
                .set(AfterSalesIntStockOrderDO::getActualQuantity, one.getExpectQuantity())
                .set(AfterSalesIntStockOrderDO::getWarehouseId, dto.getWarehouseId())
                .update();
        if (!update) {
            log.error("更新失败");
            throw new BizException("更新失败");
        }
        z
    }
}
