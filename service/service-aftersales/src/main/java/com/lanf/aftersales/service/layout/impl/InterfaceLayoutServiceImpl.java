package com.lanf.aftersales.service.layout.impl;

import com.lanf.aftersales.model.dto.AfterSalesOrderAddDTO;
import com.lanf.aftersales.model.dto.BusinessReceiverDTO;
import com.lanf.aftersales.model.entity.AfterSalesOrderDO;
import com.lanf.aftersales.model.entity.AfterSalesOrderItemDO;
import com.lanf.aftersales.model.enums.MainStatusEnum;
import com.lanf.aftersales.model.enums.SubStatus;
import com.lanf.aftersales.service.IAfterSalesOrderItemService;
import com.lanf.aftersales.service.IAfterSalesOrderService;
import com.lanf.aftersales.service.layout.InterfaceLayoutService;
import com.lanf.common.utils.*;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.aftersales.mq.AftersalesClientTopicName;
import com.lanf.aftersales.mq.message.SalesInStockOrderAddMessage;
import com.lanf.aftersales.mq.message.SalesInStockOrderItemAdd;
import com.lanf.api.goods.api.GoodsApiService;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.mybatis.utils.IdUtils;
import com.lanf.order.api.OrderApiService;
import com.lanf.order.model.vo.OrderItemVO;
import com.lanf.order.model.vo.OrderVO;
import com.lanf.client.pay.api.PayApiService;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.api.StorageApiService;
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
    private PayApiService payApiService;
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
    @Autowired
    private ISendMqMessageService sendMqMessageService;

    @Transactional
    @Override
    public void afterSalesOrderAdd(AfterSalesOrderAddDTO dto) {

        Long orderId = dto.getOrderId();
        List<Long> orderIdList = new ArrayList<>();
        orderIdList.add(orderId);
        /**
         * 校验
         */
        AfterSalesOrderDO salesOrderDO = afterSalesOrderService.lambdaQuery()
                .eq(AfterSalesOrderDO::getOrderId, orderId).one();

        if (!(MainStatusEnum.CLOSED.getCode()
                .equals(salesOrderDO.getMainStatus())
                || MainStatusEnum.SUCCESS.getCode()
                .equals(salesOrderDO.getMainStatus()))) {
            throw new BizException("已存在处理中的售后单");
        }

        List<OrderVO> orderVOList = RpcResultParser.parseResult(orderApiService.queryByOrderId(orderIdList));
        if (orderVOList == null || orderVOList.isEmpty()) {
            log.error("订单信息查询异常{}", orderIdList);
            throw new BizException("订单信息查询异常");
        }
        OrderVO orderVO = orderVOList.get(0);
        if (new Date().getTime() > orderVO.getFinishTime().getTime()) {
            throw new BizException("订单履约已完成，不能进行售后");
        }
        List<OrderItemVO> inOutStockOrderItemDTOList = orderVO.getInOutStockOrderItemDTOList();
        //校验下订单状态 简单校验 就在这里进行
        Integer orderStatus = orderVO.getOrderStatus();
        if (!(orderStatus == 4 || orderStatus == 5)) {
            /**
             * 待评价、已完成订单才能发起售后
             */
            throw new BizException("订单状态异常");
        }

        /**
         * 构建
         */
        Date applicationTime = new Date();
        AfterSalesOrderDO afterSalesOrder = new AfterSalesOrderDO();
        Long id = IdUtils.generateId();
        afterSalesOrder.setUserId(UserUtils.getUserId());
        afterSalesOrder.setId(id);
        afterSalesOrder.setOrderId(orderId);
        afterSalesOrder.setOrderNumber(CodeGenerateUtils.generateOrderNumber());
        afterSalesOrder.setShopId(orderVO.getShopId());
        afterSalesOrder.setAfterSalesType(dto.getAfterSalesType());
        afterSalesOrder.setBusinessAutoAgreeTime(getBusinessAutoAgreeTime(applicationTime));
        afterSalesOrder.setApplicationTime(applicationTime);
        afterSalesOrder.setReturnReason(dto.getReturnReason());
        afterSalesOrder.setReturnQuantity(orderVO.getTotalQuantity());
        afterSalesOrder.setMainStatus(MainStatusEnum.WAIT_SELLER_AGREE.getCode());
        afterSalesOrder.setSubStatus(SubStatus.WAIT_MANUAL.getCode());
        //
        List<AfterSalesOrderItemDO> afterSalesOrderItemList = BeanCopyUtils.copyBeanList(inOutStockOrderItemDTOList, AfterSalesOrderItemDO.class);
        afterSalesOrderItemList.forEach(a -> {
            a.setAfterSalesOrderId(id);
        });
        /**
         * 保存
         */
        afterSalesOrderService.save(afterSalesOrder);
        iAfterSalesOrderItemService.saveBatch(afterSalesOrderItemList);
        /**
         * 发送延迟消息 商家自动同意
         */

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
