package com.lanf.storage.service.reconciliation.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.goods.api.GoodsApiService;
import com.lanf.api.goods.model.query.ReconciliationStockFlowQuery;
import com.lanf.api.goods.model.vo.ReconciliationStockFlowVO;
import com.lanf.api.order.api.OrderApiService;
import com.lanf.api.order.model.query.ReconciliationOrderItemQuery;
import com.lanf.api.order.model.vo.ReconciliationOrderItem;
import com.lanf.api.order.model.vo.ReconciliationOrderItemVO;
import com.lanf.api.storage.model.enums.ReconciliationOrderStatusEnum;
import com.lanf.api.storage.model.query.ReconciliationOrderDetailPageQuery;
import com.lanf.api.storage.model.vo.ReconciliationOrderDetailPageVO;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.storage.mapper.ReconciliationOrderDetailMapper;
import com.lanf.storage.model.bo.AddReconciliationOrderDetailBO;
import com.lanf.storage.model.entity.ReconciliationOrderDetailDO;
import com.lanf.storage.service.reconciliation.IReconciliationOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

/**
 * <p>
 * 库存对账订单详细 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-06
 */
@Service
public class ReconciliationOrderDetailServiceImpl extends ServiceImpl<ReconciliationOrderDetailMapper, ReconciliationOrderDetailDO> implements IReconciliationOrderDetailService {
    @Autowired
    private OrderApiService orderApiService;

    @Autowired
    private GoodsApiService goodsApiService;


    @Override
    public PageResult<ReconciliationOrderDetailPageVO> reconciliationOrderDetailPageQuery(ReconciliationOrderDetailPageQuery query) {
        IPage<ReconciliationOrderDetailDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<ReconciliationOrderDetailDO> doPage = this.lambdaQuery()
                .eq(StringUtils.isNotBlank(query.getBathId()), ReconciliationOrderDetailDO::getBathId, query.getBathId())
                .eq(query.getOrderId() != null, ReconciliationOrderDetailDO::getOrderId, query.getOrderId())
                .eq(query.getOrderStatus() != null, ReconciliationOrderDetailDO::getOrderStatus, query.getOrderStatus())
                .orderByDesc(ReconciliationOrderDetailDO::getCreateTime)
                .page(page);

        if (doPage.getRecords().isEmpty()) {
            return PageResult.emptyResult();
        }

        PageResult<ReconciliationOrderDetailPageVO> result = new PageResult<>();
        result.setTotal(doPage.getTotal());
        result.setRecords(doPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        result.setSize(doPage.getSize());

        return result;
    }

    private ReconciliationOrderDetailPageVO convertToVO(ReconciliationOrderDetailDO d) {
        ReconciliationOrderDetailPageVO vo = new ReconciliationOrderDetailPageVO();
        vo.setId(d.getId());
        vo.setBathId(d.getBathId());
        vo.setOrderId(d.getOrderId());
        if (d.getOrderStatus() != null) {
            vo.setOrderStatus(com.lanf.api.storage.model.enums.ReconciliationOrderStatusEnum.valueOf(d.getOrderStatus().name()));
        }
        vo.setOrderItems(d.getOrderItems());
        vo.setStockFlows(d.getStockFlows());
        vo.setCreateTime(d.getCreateTime());
        vo.setUpdateTime(d.getUpdateTime());
        return vo;
    }

    @Override
    public void addReconciliationOrderDetail(AddReconciliationOrderDetailBO bo) {
        Long orderId = bo.getOrderId();
        ReconciliationOrderItemQuery query = new ReconciliationOrderItemQuery();
        query.setOrderId(orderId);
        query.setOrderStatus(bo.getToOrderStatus());

        OrderStatusEnum orderStatus = bo.getToOrderStatus();
        ReconciliationOrderStatusEnum reconciliationOrderStatus = null;
        switch (orderStatus) {
            case WAIT_PAY:
                reconciliationOrderStatus = ReconciliationOrderStatusEnum.PENDING_OUTBOUND;
                break;
            case OUTBOUNDED:
                reconciliationOrderStatus = ReconciliationOrderStatusEnum.OUTBOUNDED;
                break;
            case CANCELLED:
                reconciliationOrderStatus = ReconciliationOrderStatusEnum.CANCELLED;
                break;

        }
        ReconciliationOrderDetailDO oned = this.lambdaQuery()
                .eq(ReconciliationOrderDetailDO::getOrderId, orderId)
                .eq(ReconciliationOrderDetailDO::getOrderStatus, reconciliationOrderStatus)
                .one();
        if (oned != null) {
            log.warn("订单入库单已存在");
            return;
        }
        ReconciliationOrderItemVO parseResult = null;
        try {
            parseResult = RpcResultParser.parseResult(orderApiService.reconciliationOrderItemQuery(query));
        } catch (Exception e) {
            throw new MessageRetryConsumeException("查询订单轨迹异常");
        }

        ReconciliationStockFlowQuery query2 = new ReconciliationStockFlowQuery();
        query2.setOrderId(orderId);
        query2.setUserStockFlowEventType(bo.getUserStockFlowEventType());
        ReconciliationStockFlowVO flowVO = null;
        try {
            flowVO = RpcResultParser.parseResult(goodsApiService.reconciliationStockFlowQuery(query2));
        } catch (Exception e) {
            log.warn("查询库存流水异常");
            throw new MessageRetryConsumeException(" 查询库存流水异常");
        }


        List<ReconciliationOrderItem> orderItemVOS = parseResult.getOrderItemVOS();
        ReconciliationOrderDetailDO reconciliationOrderDetailDO = new ReconciliationOrderDetailDO();
        reconciliationOrderDetailDO.setOrderId(orderId);
        reconciliationOrderDetailDO.setOrderStatus(reconciliationOrderStatus);
        reconciliationOrderDetailDO.setOrderItems(JsonUtils.toJsonString(orderItemVOS));
        reconciliationOrderDetailDO.setBathId(parseResult.getCreateDate());
        reconciliationOrderDetailDO.setStockFlows(JsonUtils.toJsonString(flowVO.getReconciliationStockFlowList()));

        try {
            this.save(reconciliationOrderDetailDO);
        } catch (DuplicateKeyException e) {
            log.warn("库存对账单已存在");
        }
    }
}
