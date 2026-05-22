package com.lanf.aftersales.service.layout.impl;

import com.lanf.aftersales.model.dto.AddAfterSalesOrderDTO;
import com.lanf.aftersales.model.dto.BusinessReceiverDTO;
import com.lanf.aftersales.model.entity.AfterSalesOrderDO;
import com.lanf.aftersales.model.entity.AfterSalesOrderItemDO;
import com.lanf.aftersales.model.enums.SubStatus;
import com.lanf.aftersales.mq.AftersalesClientTopicName;
import com.lanf.aftersales.mq.message.SalesInStockOrderAddMessage;
import com.lanf.aftersales.mq.message.SalesInStockOrderItemAdd;
import com.lanf.aftersales.service.IAfterSalesOrderItemService;
import com.lanf.aftersales.service.IAfterSalesOrderService;
import com.lanf.aftersales.service.layout.InterfaceLayoutService;
import com.lanf.api.goods.api.GoodsApiService;
import com.lanf.api.order.api.OrderApiService;
import com.lanf.api.storage.api.StorageApiService;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class InterfaceLayoutServiceImpl implements InterfaceLayoutService {

    @Autowired
    private OrderApiService orderApiService;

    @Autowired
    private IAfterSalesOrderItemService iAfterSalesOrderItemService;
    @Autowired
    private IAfterSalesOrderService afterSalesOrderService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Autowired
    private StorageApiService storageApiService;
    @Autowired
    private IAfterSalesOrderItemService afterSalesOrderItemService;
    @Autowired
    private GoodsApiService goodsApiService;


    @Transactional
    @Override
    public void afterSalesOrderAdd(AddAfterSalesOrderDTO dto) {



    }

    private Date getBusinessAutoAgreeTime(Date applicationTime) {

        //48小时商家自动同意
        return DateUtils.addHour(applicationTime, 48L);
    }


    @Override
    public void businessReceiver(BusinessReceiverDTO dto) {

        Long id = dto.getId();
        AfterSalesOrderDO salesOrderDO = afterSalesOrderService.getById(id);

        if (salesOrderDO == null) {
            log.error("售后单不存在{}", id);
            throw new BizException("售后单不存在");
        }
        if (!SubStatus.NO_SIGN.getCode().equals(salesOrderDO.getSubStatus())) {
            throw new BizException("售后单状态异常");
        }
        boolean update = afterSalesOrderService.lambdaUpdate().eq(AfterSalesOrderDO::getId, id)
                .eq(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion())
                .set(AfterSalesOrderDO::getSubStatus, SubStatus.SIGNED.getCode())
                .set(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion() + 1)
                .update();
        if ( !update) {
            throw new BizException("售后单更新失败");
        }

        //商家同意退款
        /**
         * 创建销售退款退款入库单
         */
        rocketMqClient.sendMessage(AftersalesClientTopicName.AFTER_SALES_CREATE_IN_ORDER_TOPIC,
                JsonUtils.toJsonString(buildSalesInStockOrderAddDTO(salesOrderDO,0)));

    }


    private SalesInStockOrderAddMessage buildSalesInStockOrderAddDTO(AfterSalesOrderDO salesOrderDO, Integer inOutStatus) {

        Long afterSalesOrderId = salesOrderDO.getId();
        List<AfterSalesOrderItemDO> list = afterSalesOrderItemService.lambdaQuery().eq(AfterSalesOrderItemDO::getAfterSalesOrderId, afterSalesOrderId).list();

        int totalQuantity = 0;
        SalesInStockOrderAddMessage dto = new SalesInStockOrderAddMessage();
        List<SalesInStockOrderItemAdd> salesInStockOrderItemAddDTOList = new ArrayList<>(list.size());
        dto.setSalesInStockOrderItemAddDTOList(salesInStockOrderItemAddDTOList);
        for (AfterSalesOrderItemDO a : list) {
            SalesInStockOrderItemAdd dto1 = new SalesInStockOrderItemAdd();
            dto1.setGoodsName(a.getGoodsName());
            dto1.setQuantity(a.getQuantity());
            dto1.setSkuName(a.getSkuName());
            dto1.setSkuCode(a.getSkuCode());
            salesInStockOrderItemAddDTOList.add(dto1);
            //商品总数量累计
            totalQuantity = totalQuantity + a.getQuantity();
        }

        dto.setAfterSalesOrderId(salesOrderDO.getId());

        return dto;
    }


}
