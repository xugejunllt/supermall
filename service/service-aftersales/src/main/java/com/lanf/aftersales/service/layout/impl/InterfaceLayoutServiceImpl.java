package com.lanf.aftersales.service.layout.impl;

import com.lanf.aftersales.model.dto.*;
import com.lanf.aftersales.model.entity.AfterSalesOrderDO;
import com.lanf.aftersales.model.entity.AfterSalesOrderItemDO;
import com.lanf.aftersales.model.enums.ReturnsAndRefundsStatusEnum;
import com.lanf.aftersales.service.IAfterSalesOrderItemService;
import com.lanf.aftersales.service.IAfterSalesOrderService;
import com.lanf.aftersales.service.layout.InterfaceLayoutService;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.goods.api.GoodsApiService;
import com.lanf.messagemanager.client.annotation.SendMessage;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.order.api.OrderApiService;
import com.lanf.order.model.vo.OrderItemVO;
import com.lanf.order.model.vo.OrderVO;
import com.lanf.pay.api.PayApiService;
import com.lanf.pay.model.vo.OrderTradeVO;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.PromiseOrderReturnMoneyDTO;
import com.lanf.rocketmq.model.message.RefundDTO;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.security.utils.UserUtil;
import com.lanf.storage.api.StorageApiService;
import com.lanf.storage.model.dto.SalesInStockOrderAddDTO;
import com.lanf.storage.model.dto.SalesInStockOrderItemAddDTO;
import com.lanf.web.exception.BizException;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
        AfterSalesOrderDO salesOrderDO = afterSalesOrderService.lambdaQuery().eq(AfterSalesOrderDO::getOrderId, orderId).one();
        if (salesOrderDO != null) {
            throw new BizException("售后单已存在");
        }

        List<OrderVO> orderVOList = orderApiService.queryByOrderId(orderIdList).getData();
        if (orderVOList == null || orderVOList.isEmpty()) {
            throw new BizException("订单信息不存在");
        }
        OrderVO orderVO = orderVOList.get(0);
        if (new Date().getTime() > orderVO.getFinishTime().getTime()){
            throw new BizException("订单履约已完成，不能进行售后");
        }
        List<OrderItemVO> inOutStockOrderItemDTOList = orderVO.getInOutStockOrderItemDTOList();
        //校验下订单状态 简单校验 就在这里进行
        Integer orderStatus = orderVO.getOrderStatus();
        if (!(orderStatus == 4 || orderStatus == 5)) {
            //只有已完成或已关闭的订单才能进行售后
            throw new BizException("订单状态异常");
        }
        /**
         * 查询交易信息
         */
        OrderTradeVO orderTradeVO = payApiService.queryOrderTradeByOrderId(orderId).getData();
        if (orderTradeVO == null) {

            throw new BizException("交易信息不存在");
        }
        /**
         * 构建
         */
        Date applicationTime = new Date();
        AfterSalesOrderDO afterSalesOrder = new AfterSalesOrderDO();
        Long id = IdUtils.generateId();
        afterSalesOrder.setUserId(UserUtil.getUserId());
        afterSalesOrder.setId(id);
        afterSalesOrder.setOrderId(orderId);
        afterSalesOrder.setOrderNumber(CodeGenerateUtils.generateOrderNumber());
        afterSalesOrder.setShopId(orderVO.getShopId());
        afterSalesOrder.setAfterSalesType(dto.getAfterSalesType());
        afterSalesOrder.setReturnsAndRefundsStatus(0);
        afterSalesOrder.setBusinessAutoAgreeTime(getBusinessAutoAgreeTime(applicationTime));
        afterSalesOrder.setApplicationTime(applicationTime);
        afterSalesOrder.setReturnReason(dto.getReturnReason());
        afterSalesOrder.setReturnMoney(orderTradeVO.getPayMoney());
        afterSalesOrder.setReturnQuantity(orderVO.getTotalQuantity());
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

    @GlobalTransactional
    @SendMessage
    @Override
    public void businessReceiver(BusinessReceiverDTO dto) {

        Long id = dto.getId();
        Long agree = dto.getAgree();
        AfterSalesOrderDO salesOrderDO = afterSalesOrderService.getById(id);

        if (!(agree == 0 || agree == 1)) {
            throw new BizException("agree单状态异常");
        }
        if (salesOrderDO == null) {
            throw new BizException("售后单不存在");
        }
        if (salesOrderDO.getReturnsAndRefundsStatus() != 3) {
            throw new BizException("售后单状态异常");
        }

        Integer returnsAndRefundsStatus = null;
        if (agree == 0) {
            returnsAndRefundsStatus = ReturnsAndRefundsStatusEnum.FINISH.getCode();
        } else {
            returnsAndRefundsStatus = ReturnsAndRefundsStatusEnum.REFUSE_FINISH.getCode();
        }

        AfterSalesOrderDO salesOrderDOUpdate = new AfterSalesOrderDO();
        salesOrderDOUpdate.setId(id);
        salesOrderDOUpdate.setReturnsAndRefundsStatus(returnsAndRefundsStatus);
        afterSalesOrderService.updateById(salesOrderDOUpdate);
        if (returnsAndRefundsStatus.equals(ReturnsAndRefundsStatusEnum.FINISH.getCode())) {
            //商家同意退款
            /**
             * 创建销售退款退款入库单
             */
            /**
             * 创建销售入库单
             */
            log.info("商家同意退款");
            SalesInStockOrderAddDTO v2 = buildSalesInStockOrderAddDTO(salesOrderDO, 2);
            List<SalesInStockOrderAddDTO> dtoList = new ArrayList<>();
            dtoList.add(v2);
            Integer code = storageApiService.salesInStockOrderAdd(dtoList).getCode();
            if (code != 200) {
                throw new BizException("创建售后单异常");
            }
            /**
             * 进行退款
             */
            RefundDTO refundDTO = new RefundDTO();
            refundDTO.setOrderId(salesOrderDO.getOrderId());
            refundDTO.setSource(2);
            refundDTO.setToUserType(1);
            //进行转账
            refundDTO.setTransAccount(true);
            refundDTO.setBizKeyValue(salesOrderDO.getOrderId()+"");
            sendMqMessageService.sendMessage(TopicName.REFUND_TOPIC, refundDTO);
            /**
             * 更新履约单退款状态
             */

            PromiseOrderReturnMoneyDTO  returnMoneyDTO = new PromiseOrderReturnMoneyDTO();
            returnMoneyDTO.setOrderId(salesOrderDO.getOrderId());
            returnMoneyDTO.setBizKeyValue(salesOrderDO.getOrderId()+"");
            sendMqMessageService.sendMessage(TopicName.PROMISE_ORDER_RETURN_MONEY_TOPIC, returnMoneyDTO);
        }


    }


    private SalesInStockOrderAddDTO buildSalesInStockOrderAddDTO(AfterSalesOrderDO salesOrderDO, Integer inOutStatus) {

        Long afterSalesOrderId = salesOrderDO.getId();
        List<AfterSalesOrderItemDO> list = afterSalesOrderItemService.lambdaQuery().eq(AfterSalesOrderItemDO::getAfterSalesOrderId, afterSalesOrderId).list();

        Integer totalQuantity = 0;
        SalesInStockOrderAddDTO dto = new SalesInStockOrderAddDTO();
        List<SalesInStockOrderItemAddDTO> salesInStockOrderItemAddDTOList = new ArrayList<>(list.size());
        dto.setSalesInStockOrderItemAddDTOList(salesInStockOrderItemAddDTOList);
        for (AfterSalesOrderItemDO a : list) {
            SalesInStockOrderItemAddDTO dto1 = new SalesInStockOrderItemAddDTO();
            dto1.setGoodsName(a.getGoodsName());
            dto1.setQuantity(a.getQuantity());
            dto1.setSkuName(a.getSkuName());
            dto1.setSkuCode(a.getSkuCode());
            salesInStockOrderItemAddDTOList.add(dto1);
            //商品总数量累计
            totalQuantity = totalQuantity + a.getQuantity();
        }

        dto.setAfterSalesOrderId(salesOrderDO.getId());
        dto.setShopId(salesOrderDO.getShopId());
        dto.setTotalQuantity(totalQuantity);
        dto.setInOutStatus(inOutStatus);

        return dto;
    }


}
