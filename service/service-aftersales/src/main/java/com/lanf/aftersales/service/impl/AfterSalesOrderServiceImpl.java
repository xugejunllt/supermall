package com.lanf.aftersales.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.aftersales.mapper.AfterSalesOrderMapper;
import com.lanf.aftersales.model.bo.AfterSalesOrderPageBO;
import com.lanf.aftersales.model.dto.BusinessAgreeDTO;
import com.lanf.aftersales.model.dto.UnderAfterSaleDTO;
import com.lanf.aftersales.model.dto.UserDeliveryDTO;
import com.lanf.aftersales.model.entity.AfterSalesOrderDO;
import com.lanf.aftersales.model.entity.AfterSalesOrderItemDO;
import com.lanf.aftersales.model.enums.AfterSalesTypeEnum;
import com.lanf.aftersales.model.enums.MainStatusEnum;
import com.lanf.aftersales.model.enums.SubStatus;
import com.lanf.aftersales.model.query.AfterSalesOrderPageQuery;
import com.lanf.aftersales.model.vo.AfterSalesOrderItemPageVO;
import com.lanf.aftersales.model.vo.AfterSalesOrderPageVO;
import com.lanf.aftersales.mq.AftersalesClientTopicName;
import com.lanf.aftersales.mq.message.CloseOrderMessage;
import com.lanf.aftersales.service.IAfterSalesOrderItemService;
import com.lanf.aftersales.service.IAfterSalesOrderService;
import com.lanf.client.pay.model.enums.RefundEventTypeEnum;
import com.lanf.client.pay.mq.PayClientTopicName;
import com.lanf.client.pay.mq.message.ProcessRefundMessage;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.security.utils.UserUtils;
import com.lanf.storage.api.StorageApiService;
import com.lanf.system.api.SystemService;
import com.lanf.system.model.vo.ShopVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private RocketMqClient rocketMqClient;

    @Autowired
    private StorageApiService storageApiService;
    @Autowired
    private IAfterSalesOrderItemService afterSalesOrderItemService;
    @Autowired
    private SystemService systemService;

    /**
     * 分布式锁更新 用组件封装 代码更简单
     */
    @Override
    public void businessAgree(BusinessAgreeDTO dto) {

        Long id = dto.getId();
        AfterSalesOrderDO salesOrderDO = this.getById(id);
        if (salesOrderDO == null) {
            log.error("售后单不存在");
            throw new BizException("售后单不存在");
        }
        if ( !MainStatusEnum.WAIT_SELLER_AGREE.getCode()
                .equals(salesOrderDO.getMainStatus())) {
            throw new BizException("售后单状态异常");
        }
        boolean update = this.lambdaUpdate().eq(AfterSalesOrderDO::getId, id)
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
        if ( !MainStatusEnum.WAIT_BUYER_RETURN.getCode()
                .equals(salesOrderDO.getMainStatus())) {
            throw new BizException("售后单状态异常");
        }
        boolean update = this.lambdaUpdate().eq(AfterSalesOrderDO::getId, id)
                .eq(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion())
                .set(AfterSalesOrderDO::getMainStatus, MainStatusEnum.WAIT_SELLER_RECEIVE.getCode())
                .set(AfterSalesOrderDO::getSubStatus, SubStatus.NO_SIGN.getCode())
                .set(AfterSalesOrderDO::getVersion, salesOrderDO.getVersion() + 1)
                .update();
        if ( !update) {
            throw new BizException("售后单更新失败");
        }
    }



    /**
     * 在性能表现上 列表和单个查询差不多 可以合并成一个接口  即接口多返回写字段也没关系
     * 通常是主表和字表信息一起返回就够用的那种
     */
    @Override
    public PageResult<AfterSalesOrderPageVO> afterSalesOrderPageQuery(AfterSalesOrderPageQuery query) {


        AfterSalesOrderPageBO v2 = new AfterSalesOrderPageBO();
        v2.setQuery(query);
        v2.setPage(query.getPage());
        v2.setPageSize(query.getPageSize());
        v2.setUserId(query.getUserId());
        if (UserUtils.getShopId()!=null){
            v2.setShopId(UserUtils.getShopId());
        }

        return afterSalesOrderPageQuery(v2);
    }

    @Override
    public AfterSalesOrderPageVO afterSalesOrderDetail(Long id) {


        AfterSalesOrderPageBO v2 = new AfterSalesOrderPageBO();
        v2.setAfterSalesOrderId(id);
        //不查数量
        v2.setPage(0);
        PageResult<AfterSalesOrderPageVO> pageResult = afterSalesOrderPageQuery(v2);
        List<AfterSalesOrderPageVO> records = pageResult.getRecords();
        if (records.isEmpty()) {
            return null;
        }

        return records.get(0);
    }


    private PageResult<AfterSalesOrderPageVO> afterSalesOrderPageQuery(AfterSalesOrderPageBO query) {

        Long userId = query.getUserId();
        Long afterSalesOrderId1 = query.getAfterSalesOrderId();
        Long shopId1 = query.getShopId();
        AfterSalesOrderPageQuery queryQuery = query.getQuery();

        LambdaQueryChainWrapper<AfterSalesOrderDO> lambdaQuery = this.lambdaQuery();
        if (userId != null) {
            lambdaQuery.eq(AfterSalesOrderDO::getUserId, userId);
        }
        if (afterSalesOrderId1 != null) {
            lambdaQuery.eq(AfterSalesOrderDO::getId, afterSalesOrderId1);
        }
        if (shopId1 != null) {
            lambdaQuery.eq(AfterSalesOrderDO::getShopId, shopId1);
        }
        if (queryQuery != null) {
            //
        }
        IPage<AfterSalesOrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<AfterSalesOrderDO> purchaseStorageOrderPage = lambdaQuery.
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (purchaseStorageOrderPage.getRecords().isEmpty()) {

            return PageResult.emptyResult(AfterSalesOrderPageVO.class);
        }
        List<AfterSalesOrderDO> records = page.getRecords();
        List<Long> afterSalesOrderIdList = records.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<AfterSalesOrderItemDO> orderItemDOList = afterSalesOrderItemService.lambdaQuery().
                in(AfterSalesOrderItemDO::getAfterSalesOrderId, afterSalesOrderIdList).list();
        Map<Long, List<AfterSalesOrderItemDO>> afterSalesOrderItemDOMap = new HashMap<>();
        orderItemDOList.forEach(a -> {

            Long afterSalesOrderId = a.getAfterSalesOrderId();
            List<AfterSalesOrderItemDO> afterSalesOrderItemDOS = afterSalesOrderItemDOMap.get(afterSalesOrderId);
            if (afterSalesOrderItemDOS == null) {
                afterSalesOrderItemDOS = new ArrayList<>();
                afterSalesOrderItemDOMap.put(afterSalesOrderId, afterSalesOrderItemDOS);
            }
            afterSalesOrderItemDOS.add(a);

        });


        List<Long> shopIdList = records.stream().map(AfterSalesOrderDO::getShopId).collect(Collectors.toList());
        List<ShopVO> shopVOList = systemService.shopQuery(shopIdList).getData();
        Map<Long, ShopVO> shopVOMap = shopVOList.stream()
                .collect(Collectors.toMap(ShopVO::getId, Function.identity()));

        List<AfterSalesOrderPageVO> afterSalesOrderPageVOS = new ArrayList<>(records.size());
        records.forEach(a -> {

            Long shopId = a.getShopId();
            String shopName = shopVOMap.get(shopId).getName();
            Integer incomeStatus = a.getIncomeStatus();
            List<AfterSalesOrderItemDO> afterSalesOrderItemDOS = afterSalesOrderItemDOMap.get(a.getId());
            String incomeStatusName = null;
            if (incomeStatus.equals(0)) {
                incomeStatusName = "待退款";
            }
            if (incomeStatus.equals(1)) {
                incomeStatusName = "已退款";
            }

            List<AfterSalesOrderItemPageVO> afterSalesOrderItemPageVOS1 = BeanCopyUtils.copyBeanList(afterSalesOrderItemDOS, AfterSalesOrderItemPageVO.class);

            AfterSalesOrderPageVO vo = BeanCopyUtils.copyBean(a, AfterSalesOrderPageVO.class);
            vo.setShopId(shopId);
            vo.setShopName(shopName);
            vo.setAfterSalesTypeName(AfterSalesTypeEnum.getAfterSalesTypeEnum(a.getAfterSalesType()).getName());
            vo.setIncomeStatusName(incomeStatusName);
//            vo.setReturnsAndRefundsStatusName(ReturnsAndRefundsStatusEnum.
//                    getReturnsAndRefundsStatusEnum(vo.getReturnsAndRefundsStatus()).getName());

            vo.setAfterSalesOrderItemPageVOS(afterSalesOrderItemPageVOS1);
            afterSalesOrderPageVOS.add(vo);
        });

        PageResult<AfterSalesOrderPageVO> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(afterSalesOrderPageVOS);
        pageResult.setSize(afterSalesOrderPageVOS.size());


        return pageResult;
    }

    @Override
    public void afterSalesInStockFinish(Long id) {

        AfterSalesOrderDO salesOrderDO = this.getById(id);
        if (salesOrderDO == null) {
            log.error("售后单不存在");
            throw new BizException("售后单不存在");
        }
        if ( ! SubStatus.SIGNED.getCode()
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

    @Override
    public void completeRefund(Long id) {
        log.info("完成退款开始:afterSalesOrderId={}", id);

        AfterSalesOrderDO salesOrderDO = this.getById(id);
        if (salesOrderDO == null) {
            log.error("售后单不存在:afterSalesOrderId={}", id);
            throw new BizException("售后单不存在");
        }

        if (!SubStatus.REFUND_PROCESS.getCode().equals(salesOrderDO.getSubStatus())) {
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
         * 进行退款
         */
        ProcessRefundMessage message = new ProcessRefundMessage();
        message.setBizOrderId(salesOrderDO.getId());
        message.setRefundEventTypeEnum(RefundEventTypeEnum.CANCEL_PAID_ORDER);
        rocketMqClient.sendMessage(PayClientTopicName.PROCESS_REFUND_TOPIC,
                JsonUtils.toJsonString(message));
        /**
         * 关闭订单
         */
        CloseOrderMessage closeOrderMessage = new CloseOrderMessage();
        closeOrderMessage.setOrderId(salesOrderDO.getOrderId());
        rocketMqClient.sendMessage(AftersalesClientTopicName.AFTER_SALES_CLOSE_ORDER_TOPIC,
                JsonUtils.toJsonString(closeOrderMessage));
    }

    @Override
    public boolean isUnderAfterSale(UnderAfterSaleDTO dto) {

        Long orderId = dto.getOrderId();
        List<AfterSalesOrderDO> list = this.lambdaQuery()
                .eq(AfterSalesOrderDO::getOrderId, orderId)
                .list();
        if ( list.isEmpty()) {
            log.info("该订单没有售后单:orderId={}", orderId);
            return true;
        }

        boolean allClosed = list.stream()
                .allMatch(order -> MainStatusEnum.CLOSED.getCode().equals(order.getMainStatus()));
        if (allClosed) {
            log.info("该订单所有售后单均已关闭:orderId={}", orderId);
            return true;
        } else {
            log.info("该订单存在进行中的售后单:orderId={}", orderId);
            return false;
        }
    }

}
