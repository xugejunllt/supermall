package com.lanf.order.service.aftersales.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.aftersales.mq.AftersalesClientTopicName;
import com.lanf.aftersales.mq.message.SalesInStockOrderAddMessage;
import com.lanf.aftersales.mq.message.SalesInStockOrderItemAdd;
import com.lanf.api.order.model.dto.BusinessAgreeDTO;
import com.lanf.api.order.model.dto.BusinessReceiverDTO;
import com.lanf.api.order.model.dto.CompleteRefundDTO;
import com.lanf.api.order.model.vo.AfterSalesOrderForUserDetailVO;
import com.lanf.api.order.model.vo.AfterSalesOrderForUserPageVO;
import com.lanf.api.order.model.vo.AfterSalesOrderItemVO;
import com.lanf.api.order.mq.constant.OrderClientTopicName;
import com.lanf.api.order.mq.message.AfterSalesRefundMessage;
import com.lanf.common.utils.*;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.order.MainStatusEnum;
import com.lanf.constant.model.enums.order.SubStatus;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.IdUtils;
import com.lanf.constant.utils.UserContext;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.mapper.AfterSalesOrderMapper;
import com.lanf.order.model.dto.AddAfterSalesOrderDTO;
import com.lanf.order.model.dto.UserDeliveryDTO;
import com.lanf.order.model.entity.AfterSalesOrderDO;
import com.lanf.order.model.entity.AfterSalesOrderItemDO;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.entity.OrderItemDO;
import com.lanf.order.service.aftersales.IAfterSalesOrderItemService;
import com.lanf.order.service.aftersales.IAfterSalesOrderService;
import com.lanf.order.service.order.IOrderItemService;
import com.lanf.order.service.order.IOrderService;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 售后单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-19
 */
@Slf4j
@Service
public class AfterSalesOrderServiceImpl extends ServiceImpl<AfterSalesOrderMapper, AfterSalesOrderDO> implements IAfterSalesOrderService {

    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;


    @Autowired
    private IAfterSalesOrderItemService afterSalesOrderItemService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IOrderItemService orderItemService;

    @Override
    public void addAfterSalesOrder(AddAfterSalesOrderDTO dto) {


        Long orderId = dto.getOrderId();
        Long userId = UserContext.getUserId();
        /**
         * 校验
         */
        AfterSalesOrderDO salesOrderDO = this.lambdaQuery()
                .eq(AfterSalesOrderDO::getOrderId, orderId).one();
        if (salesOrderDO != null){
            if (! (MainStatusEnum.CLOSED
                    .equals(salesOrderDO.getMainStatus())
                    || MainStatusEnum.SUCCESS
                    .equals(salesOrderDO.getMainStatus()))) {
                throw new BizException("已存在处理中的售后单");
            }
        }

        OrderDO one = orderService.lambdaQuery().eq(OrderDO::getId, orderId)
                .eq(OrderDO::getUserId, userId)
                .eq(BaseEntity::getId, orderId).one();
        if (one == null) {
            log.error("订单不存在");
            throw new BizException("订单不存在");
        }
//        if (!(OrderStatusEnum.WAIT_COMMENT.equals(one.getStatus())
//                || OrderStatusEnum.COMPLETED.equals(one.getStatus()))) {
//            log.warn("当前订单状态不支持售后");
//            throw new BizException("当前订单状态不支持售后");
//        }

        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery()
                .eq(OrderItemDO::getOrderId, orderId)
                .eq(OrderItemDO::getUserId, userId)
                .list();

        AfterSalesOrderDO afterSalesOrder = new AfterSalesOrderDO();
        Long id = IdUtils.generateId();
        afterSalesOrder.setUserId(UserContext.getUserId());
        afterSalesOrder.setId(id);
        afterSalesOrder.setOrderId(orderId);
        afterSalesOrder.setOrderNumber(CodeGenerateUtils.generateOrderNumber());
        afterSalesOrder.setAfterSalesType(dto.getAfterSalesType());
        /**
         * 1天自动审核通过
         */
        afterSalesOrder.setBusinessAutoAgreeTime(new Date(DateUtils.getExpireTimestampFromDays(1)));
        afterSalesOrder.setReturnReason(dto.getReturnReason());
        afterSalesOrder.setMainStatus(MainStatusEnum.WAIT_SELLER_AGREE);
        afterSalesOrder.setSubStatus(SubStatus.WAIT_MANUAL);
        afterSalesOrder.setReturnMoney(one.getActualPayMoney());
        afterSalesOrder.setTenantId(one.getTenantId());
        //
        List<AfterSalesOrderItemDO> saveAfterSalesOrderItem = new ArrayList<>(orderItemDOList.size());
        for (OrderItemDO a : orderItemDOList) {
            AfterSalesOrderItemDO afterSalesOrderItem = new AfterSalesOrderItemDO();
            afterSalesOrderItem.setAfterSalesOrderId(id);
            afterSalesOrderItem.setGoodsId(a.getGoodsId());
            afterSalesOrderItem.setGoodsName(a.getGoodsName());
            afterSalesOrderItem.setSkuName(a.getSkuName());
            afterSalesOrderItem.setSkuCode(a.getSkuCode());
            afterSalesOrderItem.setSkuPictureAddress(a.getSkuPictureAddress());
            afterSalesOrderItem.setQuantity(a.getQuantity());
            afterSalesOrderItem.setUnitPrice(a.getUnitPrice());
            afterSalesOrderItem.setTotalMoney(BigDecimalUtil.multiply(a.getUnitPrice(), new
                    BigDecimal(a.getQuantity())));
            afterSalesOrderItem.setTenantId(one.getTenantId());
            saveAfterSalesOrderItem.add(afterSalesOrderItem);
        }
        /**
         * 保存
         */
        this.save(afterSalesOrder);
        afterSalesOrderItemService.saveBatch(saveAfterSalesOrderItem);
        /**
         * 发送延迟消息 商家自动同意
         */
    }

    @Override
    public PageResult<AfterSalesOrderForUserPageVO> afterSalesOrderForUserPageQuery(PageQuery query) {

        IPage<AfterSalesOrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<AfterSalesOrderDO> result =  this.lambdaQuery()
                .orderByDesc(BaseEntity::getId)
                .page(page);

        if (result.getRecords().isEmpty()){
            return PageResult.emptyResult();
        }
        List<AfterSalesOrderDO> records = result.getRecords();

        List<AfterSalesOrderForUserPageVO> afterSalesOrderPageForUserVOList = BeanCopyUtils.copyBeanList(records, AfterSalesOrderForUserPageVO.class);


        PageResult<AfterSalesOrderForUserPageVO> result1 = new PageResult<>();
        result1.setTotal(page.getTotal());
        result1.setRecords(afterSalesOrderPageForUserVOList);
        result1.setSize(page.getSize());

        return result1;
    }

    @Override
    public AfterSalesOrderForUserDetailVO afterSalesOrderForUserDetailQuery(Long id) {

        AfterSalesOrderDO aDo = this.getById(id);
        if (aDo == null){

            return null;
        }
        List<AfterSalesOrderItemDO> list = afterSalesOrderItemService.lambdaQuery().eq(AfterSalesOrderItemDO::getAfterSalesOrderId, id)
                .list();
        List<AfterSalesOrderItemVO> afterSalesOrderItemVOS = BeanCopyUtils.copyBeanList(list, AfterSalesOrderItemVO.class);
        AfterSalesOrderForUserDetailVO afterSalesOrderForUserDetailVO = BeanCopyUtils.copyBean(aDo, AfterSalesOrderForUserDetailVO.class);
        afterSalesOrderForUserDetailVO.setAfterSalesOrderItemVOList(afterSalesOrderItemVOS);
        return afterSalesOrderForUserDetailVO;
    }

    @Override
    public void businessAgree(BusinessAgreeDTO dto) {
        Long id = dto.getId();
        AfterSalesOrderDO salesOrderDO = this.getById(id);
        if (salesOrderDO == null) {
            log.error("售后单不存在");
            throw new BizException("售后单不存在");
        }
        if ( !MainStatusEnum.WAIT_SELLER_AGREE
                .equals(salesOrderDO.getMainStatus())) {
            throw new BizException("售后单状态异常");
        }
        boolean update = this.lambdaUpdate()
                .eq(AfterSalesOrderDO::getId, id)
                .eq(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion())
                .set(AfterSalesOrderDO::getMainStatus, MainStatusEnum.WAIT_BUYER_RETURN.getCode())
                .set(AfterSalesOrderDO::getSubStatus, SubStatus.WAIT_LOGISTICS.getCode())
                .set(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion() + 1)
                .update();
        if ( !update) {
            throw new BizException("售后单更新失败");
        }
    }

    @Override
    public void userDelivery(UserDeliveryDTO dto) {
        Long id = dto.getId();
        AfterSalesOrderDO salesOrderDO = this.getById(id);
        if (salesOrderDO == null) {
            log.error("售后单不存在");
            throw new BizException("售后单不存在");
        }
        if ( !MainStatusEnum.WAIT_BUYER_RETURN
                .equals(salesOrderDO.getMainStatus())) {
            throw new BizException("售后单状态异常");
        }
        boolean update = this.lambdaUpdate()
                .eq(AfterSalesOrderDO::getId, id)
                .eq(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion())
                .set(AfterSalesOrderDO::getExpressNumber, dto.getExpressNumber())
                .set(AfterSalesOrderDO::getExpressCompany, dto.getExpressCompany())
                .set(AfterSalesOrderDO::getMainStatus, MainStatusEnum.WAIT_SELLER_RECEIVE.getCode())
                .set(AfterSalesOrderDO::getSubStatus, SubStatus.NO_SIGN.getCode())
                .set(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion() + 1)
                .update();
        if ( !update) {
            throw new BizException("售后单更新失败");
        }
    }
    @Transactional
    @Override
    public void businessReceiver(BusinessReceiverDTO dto) {

        Long id = dto.getId();
        AfterSalesOrderDO salesOrderDO = this.getById(id);

        if (salesOrderDO == null) {
            log.error("售后单不存在{}", id);
            throw new BizException("售后单不存在");
        }
        if (!SubStatus.NO_SIGN.equals(salesOrderDO.getSubStatus())) {
            throw new BizException("售后单状态异常");
        }
        boolean update = this.lambdaUpdate().eq(AfterSalesOrderDO::getId, id)
                .eq(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion())
                .set(AfterSalesOrderDO::getSubStatus, SubStatus.SIGNED.getCode())
                .set(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion() + 1)
                .update();
        if ( !update) {
            throw new BizException("售后单更新失败");
        }
        /**
         * 创建销售退款退款入库单
         */
        mqSendMessageUtils.sendMessage(AftersalesClientTopicName.AFTER_SALES_CREATE_IN_ORDER_TOPIC,
                JsonUtils.toJsonString(buildSalesInStockOrderAddDTO(salesOrderDO)),null);

    }



    private SalesInStockOrderAddMessage buildSalesInStockOrderAddDTO(AfterSalesOrderDO salesOrderDO) {

        Long afterSalesOrderId = salesOrderDO.getId();
        List<AfterSalesOrderItemDO> list = afterSalesOrderItemService.lambdaQuery().eq(AfterSalesOrderItemDO::getAfterSalesOrderId, afterSalesOrderId).list();


        SalesInStockOrderAddMessage dto = new SalesInStockOrderAddMessage();
        List<SalesInStockOrderItemAdd> salesInStockOrderItemAddDTOList = new ArrayList<>(list.size());
        dto.setSalesInStockOrderItemAddDTOList(salesInStockOrderItemAddDTOList);
        for (AfterSalesOrderItemDO a : list) {
            SalesInStockOrderItemAdd dto1 = new SalesInStockOrderItemAdd();
            dto1.setGoodsName(a.getGoodsName());
            dto1.setQuantity(a.getQuantity());
            dto1.setSkuName(a.getSkuName());
            dto1.setSkuCode(a.getSkuCode());
            dto1.setTenantId(salesOrderDO.getTenantId());
            salesInStockOrderItemAddDTOList.add(dto1);

        }
        dto.setAfterSalesOrderId(salesOrderDO.getId());
        dto.setTenantId(salesOrderDO.getTenantId());
        return dto;
    }
    @Override
    public void completeRefund(CompleteRefundDTO dto) {

        Long id = dto.getId();
        AfterSalesOrderDO salesOrderDO = this.getById(id);
        if (salesOrderDO == null) {
            log.error("售后单不存在:afterSalesOrderId={}", id);
            throw new BizException("售后单不存在");
        }

        if (!SubStatus.REFUND_PROCESS.equals(salesOrderDO.getSubStatus())) {
            log.error("售后单状态异常:afterSalesOrderId={},subStatus={}", id, salesOrderDO.getSubStatus());
            throw new BizException("售后单状态异常，当前不是退款处理中状态");
        }

        boolean update = this.lambdaUpdate()
                .eq(AfterSalesOrderDO::getId, id)
                .eq(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion())
                .set(AfterSalesOrderDO::getMainStatus, MainStatusEnum.SUCCESS.getCode())
                .set(AfterSalesOrderDO::getSubStatus, SubStatus.REFUND_DONE.getCode())
                .set(AfterSalesOrderDO::getIncomeStatus, 1)
                .set(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion() + 1)
                .update();

        if (!update) {
            log.error("售后单更新失败:afterSalesOrderId={}", id);
            throw new BizException("售后单更新失败");
        }
        /**
         * 进行售后退款
         */
        AfterSalesRefundMessage message = new AfterSalesRefundMessage();
        message.setOrderId(salesOrderDO.getOrderId());
        message.setAfterSalesOrderId(id);
        mqSendMessageUtils.sendMessage(OrderClientTopicName.AFTER_SALES_REFUND_TOPIC,
                JsonUtils.toJsonString(message),null);

    }

    @Override
    public void afterSalesInStockFinish(Long id) {
        AfterSalesOrderDO salesOrderDO = this.getById(id);
        if (salesOrderDO == null) {
            log.error("售后单不存在");
            throw new BizException("售后单不存在");
        }
        if ( ! SubStatus.SIGNED
                .equals(salesOrderDO.getSubStatus())) {
            throw new BizException("售后单状态异常");
        }

        boolean update = this.lambdaUpdate().eq(AfterSalesOrderDO::getId, id)
                .eq(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion())
                .set(AfterSalesOrderDO::getMainStatus, MainStatusEnum.WAIT_CONFIRM.getCode())
                .set(AfterSalesOrderDO::getSubStatus, SubStatus.REFUND_PROCESS.getCode())
                .set(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion() + 1)
                .update();
        if ( !update) {
            throw new BizException("售后单更新失败");
        }

    }


}
