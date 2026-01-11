package com.lanf.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.IdUtils;
import com.lanf.finance.mapper.ContrastBillMapper;
import com.lanf.finance.model.bo.ContrastOrderStatusBO;
import com.lanf.finance.model.bo.ContrastParBO;
import com.lanf.finance.model.entity.*;
import com.lanf.finance.model.query.ContrastBillPageQuery;
import com.lanf.finance.model.vo.ContrastBillTrackVO;
import com.lanf.finance.service.*;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.order.api.OrderApiService;
import com.lanf.order.model.vo.OrderItemVO;
import com.lanf.order.model.vo.OrderVO;
import com.lanf.pay.api.PayApiService;
import com.lanf.pay.model.vo.OrderTradeVO;
import com.lanf.pay.model.vo.TradeStatusVO;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.ContrastBillTaskMsg;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-09-01
 */
@Slf4j
@Service
public class ContrastBillServiceImpl extends ServiceImpl<ContrastBillMapper, ContrastBillDO> implements IContrastBillService {

    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private OrderApiService orderApiService;
    @Autowired
    private PayApiService payApiService;
    @Autowired
    private IContrastBillTrackService contrastBillTrackService;
    @Autowired
    private ILiquidationService liquidationService;
    @Autowired
    private ILiquidationFlowService liquidationFlowService;
    @Autowired
    private IMoneyFlowService moneyFlowService;
    @Autowired
    private ISettlementFlowService settlementFlowService;


    @Override
    public void commitContrastBillTask(Long orderId) {
        log.info("提交对账任务");
        ContrastBillTaskMsg msg = new ContrastBillTaskMsg();
        msg.setOrderId(orderId);
        rocketMqClient.sendMessage(TopicName.CONTRAST_BILL_TASK_TOPIC, msg);

    }

    @Override
    public void startContrastBillTask(Long orderId) {

        List<ContrastBillTrackDO> contrastBillTrackDOS = new ArrayList<>();
        List<ContrastBillDO> contrastBillDOList = new ArrayList<>();
        TradeStatusVO statusVO = payApiService.queryTradeStatus(orderId).getData();
        List<OrderVO> orderVOList = orderApiService.queryByOrderId(Arrays.asList(orderId)).getData();
        OrderVO orderVO = orderVOList.get(0);
        OrderTradeVO orderTradeVO = payApiService.queryOrderTradeByOrderId(orderId).getData();
        Long shopId = orderVO.getShopId();
        ContrastParBO bo = new ContrastParBO(orderVO, orderTradeVO, statusVO);

        ContrastBillDO contrastBillDO = initContrastBillDO(orderId, contrastBillDOList, 2, shopId);
        //对比订单状态
        ContrastOrderStatusBO orderStatusBO = contrastOrderStatus(contrastBillTrackDOS, orderId, contrastBillDO.getId(), 0, bo);
        if (!orderStatusBO.isContrastResult() && !orderStatusBO.isOrderNotPay()) {
            contrastBillDO.setStatus(1);
        }
        Integer orderStatus = getContrastBillOrderStatus(orderId);
        if (orderStatus.equals(0) && !orderStatusBO.isOrderNotPay()) {
            //订单支付，没有取消退款
            contrastBillDO.setOrderStatus(0);
            payOrderHandle(orderId, contrastBillTrackDOS, contrastBillDO, bo);
        }
        if (orderStatus.equals(1) && !orderStatusBO.isOrderNotPay()) {
            //订单支付
            contrastBillDO.setOrderStatus(0);
            payOrderHandle(orderId, contrastBillTrackDOS, contrastBillDO, bo);
            //订单支付成功后，进行了退款
            ContrastBillDO contrastBillDO2 = initContrastBillDO(orderId, contrastBillDOList, 1, shopId);
            orderCancelReturnMoney(orderId, contrastBillTrackDOS, contrastBillDO2.getId());
        }

        this.saveBatch(contrastBillDOList);
        contrastBillTrackService.saveBatch(contrastBillTrackDOS);
    }


    private void orderCancelReturnMoney(Long orderId, List<ContrastBillTrackDO> contrastBillDOList, Long contrastBillId) {
        log.info("订单取消且退款对账");

    }

    private ContrastBillDO initContrastBillDO(Long orderId, List<ContrastBillDO> contrastBillDOList, Integer orderStatus, Long shopId) {
        Long contrastBillId = IdUtils.generateId();
        ContrastBillDO contrastBillDO = new ContrastBillDO();
        contrastBillDO.setId(contrastBillId);
        contrastBillDO.setOrderId(orderId);
        contrastBillDO.setScanOrderType(0);
        contrastBillDO.setStatus(0);
        contrastBillDO.setOrderStatus(orderStatus);
        contrastBillDO.setShopId(shopId);
        contrastBillDOList.add(contrastBillDO);
        return contrastBillDO;
    }

    private void payOrderHandle(Long orderId, List<ContrastBillTrackDO> contrastBillDOList, ContrastBillDO contrastBillDO, ContrastParBO bo) {

        log.info("订单支付完成对账");
        /**
         * 开始比对
         */

        //对比订单金额、支付金额开始
        boolean result = contrastOrderMoney(contrastBillDOList, orderId, contrastBillDO.getId(), 1, bo);
        if (!result) {
            contrastBillDO.setStatus(1);
            return;
        }
        //对比结算DNA
        result = contrastLiquidation(contrastBillDOList,
                orderId, contrastBillDO.getId(), 2, bo);
        if (!result) {
            contrastBillDO.setStatus(1);
            return;
        }
        result = contrastMoneyFlow(contrastBillDOList,
                orderId, contrastBillDO.getId(), 3, bo);
        if (!result) {
            contrastBillDO.setStatus(1);

        }
    }

    private void saveResult(List<ContrastBillTrackDO> contrastBillTrackDOS, ContrastBillDO contrastBillDO) {

        this.save(contrastBillDO);
        contrastBillTrackService.saveBatch(contrastBillTrackDOS);
    }

    private boolean contrastOrderMoney(List<ContrastBillTrackDO> contrastBillTrackDOS,
                                       Long orderId, Long contrastBillId, Integer trackStatus, ContrastParBO bo) {


        log.info("对比订单金额、支付金额开始");
        ContrastBillTrackDO trackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
        OrderVO orderVO = bo.getOrderVO();
        OrderTradeVO orderTradeVO = bo.getOrderTradeVO();
        TradeStatusVO statusVO = bo.getStatusVO();

        //计算订单金额
        BigDecimal orderMoney = new BigDecimal(0);
        for (OrderItemVO a : orderVO.getInOutStockOrderItemDTOList()) {

            orderMoney = BigDecimalUtil.multiply(a.getUnitPrice(), new BigDecimal(a.getQuantity())).
                    add(orderMoney);

        }
        if (BigDecimalUtil.compareTo(orderTradeVO.getOrderMoney(), orderMoney) != 0) {
            //订单金额计算错误
            trackDO.setResultStatus(1);
            trackDO.setContent("订单金额计算错误");

            return false;
        } else {
            trackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            trackDO.setContent("订单金额计算错误");

        }
        //支付金额
        BigDecimal payMoney = BigDecimalUtil.subtract(orderMoney, orderTradeVO.getDiscountMoney());
        if (BigDecimalUtil.compareTo(orderTradeVO.getPayMoney(), payMoney) != 0) {
            //订单金额计算错误
            trackDO.setResultStatus(1);
            trackDO.setContent("支付金额计算错误");
            return false;
        } else {
            trackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            trackDO.setContent("支付金额计算错误");

        }
        BigDecimal totalAmount = statusVO.getTotalAmount();
        if (BigDecimalUtil.compareTo(orderTradeVO.getPayMoney(), totalAmount) != 0) {
            trackDO.setResultStatus(1);
            trackDO.setContent("支付订单支付金额与支付平台支付金额不一致");
            return false;
        } else {
            trackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            trackDO.setContent("支付订单支付金额与支付平台支付金额不一致");

        }
        BigDecimal receiptAmount = statusVO.getReceiptAmount();
        if (BigDecimalUtil.compareTo(orderTradeVO.getReceiptMoney(), receiptAmount) != 0) {
            trackDO.setResultStatus(1);
            trackDO.setContent("支付订单实收金额与支付平台实收金额不一致");
            return false;
        } else {
            trackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            trackDO.setContent("支付订单实收金额与支付平台实收金额不一致");

        }
        log.info("对比订单金额、支付金额完成");
        return true;
    }

    private ContrastOrderStatusBO contrastOrderStatus(List<ContrastBillTrackDO> contrastBillTrackDOS,
                                                      Long orderId, Long contrastBillId, Integer trackStatus, ContrastParBO bo) {

        log.info("对比订单状态开始");
        TradeStatusVO statusVO = bo.getStatusVO();
        OrderTradeVO orderTradeVO = bo.getOrderTradeVO();
        OrderVO orderVO = bo.getOrderVO();
        //订单支付成功状态
        List<Integer> orderPaySucStatus = Arrays.asList(1, 2, 3, 4, 5);
        //支付订单支付平台支付成功状态
        List<Integer> paySucStatus = Arrays.asList(1, 2, 3);
        ContrastBillTrackDO contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);

        if (paySucStatus.contains(statusVO.getTradeStatus()) && orderTradeVO.getPayStatus().equals(0)) {
            contrastBillTrackDO.setResultStatus(1);
            contrastBillTrackDO.setContent("支付订单支付失败，三方支付订单支付成功");
            return new ContrastOrderStatusBO();
        } else {
            contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            contrastBillTrackDO.setContent("支付订单支付失败，三方支付订单支付成功");
        }
        if (!paySucStatus.contains(statusVO.getTradeStatus()) && orderTradeVO.getPayStatus().equals(1)) {
            contrastBillTrackDO.setResultStatus(1);
            contrastBillTrackDO.setContent("支付订单支付成功，三方支付订单支付失败");
            return new ContrastOrderStatusBO();
        } else {
            contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            contrastBillTrackDO.setContent("支付订单支付成功，三方支付订单支付失败");

        }
        if (orderPaySucStatus.contains(orderVO.getOrderStatus())
                && orderTradeVO.getPayStatus().equals(0)) {
            contrastBillTrackDO.setResultStatus(1);
            contrastBillTrackDO.setContent("订单支付成功，支付订单未完成支付");
            return new ContrastOrderStatusBO();
        } else {
            contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            contrastBillTrackDO.setContent("订单支付成功，支付订单未完成支付");

        }

        if (!orderPaySucStatus.contains(orderVO.getOrderStatus())
                && orderTradeVO.getPayStatus().equals(1)) {
            contrastBillTrackDO.setResultStatus(1);
            contrastBillTrackDO.setContent("订单支付失败，支付订单完成支付");
            return new ContrastOrderStatusBO();
        } else {
            contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            contrastBillTrackDO.setContent("订单支付失败，支付订单完成支付");

        }

        if (orderVO.getOrderStatus().equals(6) && orderTradeVO.getPayStatus().equals(0)) {
            //订单已取消，且未完成支付
            contrastBillTrackDO.setContent("订单未支付，已取消");
            //订单已取消,不需要继续对账
            ContrastOrderStatusBO statusBO = new ContrastOrderStatusBO();
            statusBO.setOrderNotPay(true);
            return statusBO;

        } else {
            contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            contrastBillTrackDO.setContent("订单未支付，已取消");

        }
        ContrastOrderStatusBO statusBO = new ContrastOrderStatusBO();
        statusBO.setContrastResult(true);
        log.info("对比订单状态结束");
        return statusBO;
    }

    private ContrastBillTrackDO initContrastBillTrackDO(Long contrastBillId, Integer trackStatus,
                                                        List<ContrastBillTrackDO> contrastBillTrackDOS) {


        ContrastBillTrackDO trackDO = new ContrastBillTrackDO();
        trackDO.setContrastBillId(contrastBillId);
        trackDO.setTrackStatus(trackStatus);
        trackDO.setResultStatus(0);
        contrastBillTrackDOS.add(trackDO);
        return trackDO;
    }


    private Integer getContrastBillOrderStatus(Long orderId) {

        List<OrderVO> orderVOList = orderApiService.queryByOrderId(Arrays.asList(orderId)).getData();
        OrderVO orderVO = orderVOList.get(0);
        OrderTradeVO orderTradeVO = payApiService.queryOrderTradeByOrderId(orderId).getData();
        if (orderTradeVO.getPayStatus().equals(1) && orderVO.getOrderStatus().equals(6)) {
            //订单已取消，且支付订单已支付--取消订单进行了退款
            return 1;
        }
        if (orderTradeVO.getPayStatus().equals(0) && orderVO.getOrderStatus().equals(6)) {
            //订单已取消
            return 2;
        }

        return 0;
    }

    private boolean contrastLiquidation(List<ContrastBillTrackDO> contrastBillTrackDOS,
                                        Long orderId, Long contrastBillId, Integer trackStatus, ContrastParBO bo) {


        log.info("对比结算单开始");
        ContrastBillTrackDO contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
        LiquidationDO liquidationDO = liquidationService.lambdaQuery().eq(LiquidationDO::getOrderId, orderId).eq(LiquidationDO::getSource, 0).one();
        if (liquidationDO == null) {

            contrastBillTrackDO.setResultStatus(1);
            contrastBillTrackDO.setContent("清算单不存在");
            return false;
        } else {
            contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            contrastBillTrackDO.setContent("清算单不存在");

        }
        List<LiquidationFlowDO> liquidationFlowDOList = liquidationFlowService.lambdaQuery().eq(LiquidationFlowDO::getLiquidationId, liquidationDO.getId()).list();
        OrderTradeVO orderTradeVO = bo.getOrderTradeVO();
        boolean hasIncome1 = false;
        boolean hasIncome2 = false;
        for (LiquidationFlowDO a : liquidationFlowDOList) {
            if (a.getIncome().equals(0)) {
                hasIncome1 = true;
                BigDecimal incomeMoney = a.getIncomeMoney();
                if (BigDecimalUtil.compareTo(incomeMoney, orderTradeVO.getPayMoney()) != 0) {
                    contrastBillTrackDO.setResultStatus(1);
                    contrastBillTrackDO.setContent("清算单平台收入金额错误");

                    return false;
                } else {
                    contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
                    contrastBillTrackDO.setContent("清算单平台收入金额错误");

                }

            }
            if (a.getIncome().equals(1)) {

                BigDecimal incomeMoney = a.getIncomeMoney();
                BigDecimal incomeMoney2 = BigDecimalUtil.subtract(orderTradeVO.getPayMoney(), orderTradeVO.getReceiptMoney());
                hasIncome2 = true;
                if (BigDecimalUtil.compareTo(incomeMoney, incomeMoney2) != 0) {
                    contrastBillTrackDO.setResultStatus(1);
                    contrastBillTrackDO.setContent("清算单平台支出金额错误");

                    return false;
                } else {
                    contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
                    contrastBillTrackDO.setContent("清算单平台支出金额错误");

                }

            }
        }
        if (!hasIncome1 || !hasIncome2) {
            contrastBillTrackDO.setResultStatus(1);
            contrastBillTrackDO.setContent("清算单流水数量错误");
            return false;
        } else {
            contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            contrastBillTrackDO.setContent("清算单流水数量错误");

        }
        return true;
    }

    private boolean contrastMoneyFlow(List<ContrastBillTrackDO> contrastBillTrackDOS,
                                      Long orderId, Long contrastBillId, Integer trackStatus, ContrastParBO bo) {
        log.info("对比资金流水开始");

        LiquidationDO liquidationDO = liquidationService.lambdaQuery().eq(LiquidationDO::getOrderId, orderId).eq(LiquidationDO::getSource, 0).one();
        List<LiquidationFlowDO> liquidationFlowDOList = liquidationFlowService.lambdaQuery().eq(LiquidationFlowDO::getLiquidationId, liquidationDO.getId()).list();
        Map<Long, LiquidationFlowDO> liquidationMap = liquidationFlowDOList.stream()
                .collect(Collectors.toMap(LiquidationFlowDO::getId, Function.identity()));
        List<Long> liquidationFlowIdList = liquidationFlowDOList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SettlementFlowDO> settlementFlowDOS = settlementFlowService.lambdaQuery().in(SettlementFlowDO::getLiquidationFlowId, liquidationFlowIdList).list();
        Map<Long, SettlementFlowDO> settlementFlowMap = settlementFlowDOS.stream()
                .collect(Collectors.toMap(SettlementFlowDO::getId, Function.identity()));

        List<Long> settlementFlowIdList = settlementFlowDOS.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<MoneyFlowDO> moneyFlowDOList = moneyFlowService.lambdaQuery().in(MoneyFlowDO::getSettlementFlowId, settlementFlowIdList).list();


        ContrastBillTrackDO contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);

        boolean hasIncome1 = false;
        boolean hasIncome2 = false;
        for (MoneyFlowDO a : moneyFlowDOList) {

            if (a.getIncome().equals(0)) {
                hasIncome1 = true;
            }
            if (a.getIncome().equals(1)) {
                hasIncome2 = true;
            }
        }
        if (!hasIncome1 || !hasIncome2) {
            contrastBillTrackDO.setResultStatus(1);
            contrastBillTrackDO.setContent("资金流水数量错误");
            return false;
        } else {
            contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
            contrastBillTrackDO.setContent("资金流水数量错误");

        }

        for (MoneyFlowDO a : moneyFlowDOList) {

            Long settlementFlowId = a.getSettlementFlowId();
            SettlementFlowDO settlementFlowDO = settlementFlowMap.get(settlementFlowId);
            Long liquidationFlowId = settlementFlowDO.getLiquidationFlowId();
            LiquidationFlowDO liquidationFlowDO = liquidationMap.get(liquidationFlowId);
            BigDecimal incomeMoney = liquidationFlowDO.getIncomeMoney();
            if (BigDecimalUtil.compareTo(incomeMoney, a.getIncomeMoney()) != 0) {
                contrastBillTrackDO.setResultStatus(1);
                contrastBillTrackDO.setContent("结算单" + a.getId() + "资金流水金额错误");
                return false;
            } else {
                contrastBillTrackDO = initContrastBillTrackDO(contrastBillId, trackStatus, contrastBillTrackDOS);
                contrastBillTrackDO.setContent("结算单" + a.getId() + "资金流水金额错误");

            }


        }

        return true;
    }

    @Override
    public PageResult<ContrastBillDO> contrastBillPage(ContrastBillPageQuery query) {

        IPage<ContrastBillDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<ContrastBillDO> result = this.lambdaQuery().
                        eq(query.getScanOrderType() != null, ContrastBillDO::getScanOrderType, query.getScanOrderType()).
                eq(query.getStatus() != null, ContrastBillDO::getStatus, query.getStatus()).
                eq(query.getOrderStatus() != null, ContrastBillDO::getOrderStatus, query.getOrderStatus()).
                orderByDesc(BaseEntity::getId).
                page(page);

        List<ContrastBillDO> records = result.getRecords();
        if (records.isEmpty()) {
            return PageResult.emptyResult(ContrastBillDO.class);
        }

        return PageResult.toPageResult(page);
    }

    @Override
    public List<ContrastBillTrackVO> contrastBillDetail(Long id) {

        List<ContrastBillTrackDO> contrastBillTrackDOList = contrastBillTrackService.lambdaQuery().eq(ContrastBillTrackDO::getContrastBillId, id).
                orderByDesc(BaseEntity::getId).list();

        List<ContrastBillTrackVO> contrastBillTrackVOS = BeanCopyUtils.copyBeanList(contrastBillTrackDOList, ContrastBillTrackVO.class);
        contrastBillTrackVOS.forEach(a->{
            if (a.getResultStatus().equals(0)){
                a.setResultStatusName("成功");
            } else {
                a.setResultStatusName("失败");
            }
            if (a.getTrackStatus().equals(0)){
                a.setTrackStatusName("对比订单状态");
            }
            if (a.getTrackStatus().equals(1)){
                a.setTrackStatusName("对比订单金额、支付金额是否正确");
            }
            if (a.getTrackStatus().equals(2)){
                a.setTrackStatusName("对比清分结算单是否正确");
            }
            if (a.getTrackStatus().equals(3)){
                a.setTrackStatusName("资金流水是否正确");
            }
        });
        return contrastBillTrackVOS;
    }

}
