package com.lanf.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.mapper.TradeOrderMapper;
import com.lanf.pay.model.dto.CreatePayOrderDTO;
import com.lanf.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.pay.model.dto.TradeOrderQuantitySumDTO;
import com.lanf.pay.model.entity.PayOrderDO;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.entity.TradeOrderItemDO;
import com.lanf.pay.model.query.TradeOrderBathQuery;
import com.lanf.pay.model.query.TradeOrderQuery;
import com.lanf.pay.model.vo.CreatePayOrderVO;
import com.lanf.pay.model.vo.OrderTradeVO;
import com.lanf.pay.model.vo.TradeOrderApiVO;
import com.lanf.pay.model.vo.TradeOrderBathVO;
import com.lanf.pay.service.IBathPayOrderService;
import com.lanf.pay.service.IPayOrderService;
import com.lanf.pay.service.ITradeOrderItemService;
import com.lanf.pay.service.ITradeOrderService;
import com.lanf.pay.utils.PryServiceUtils;
import com.lanf.rocketmq.model.message.RefundDTO;
import com.lanf.tcc.service.ITccOperationService;
import com.lanf.welfare.api.WelfareApiService;
import com.lanf.welfare.model.dto.UseCouponDTO;
import com.lanf.welfare.model.vo.UseCouponVO;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 交易订单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-14
 */
@Slf4j
@Service
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrderDO> implements ITradeOrderService {

    @Autowired
    private IPayOrderService payOrderService;
    @Autowired
    private IBathPayOrderService bathPayOrderService;
    @Autowired
    private ITradeOrderItemService tradeOrderItemService;
    @Autowired
    private WelfareApiService welfareApiService;
    @Autowired
    private ITccOperationService tccOperationService;

    @HmilyTCC(confirmMethod = "confirmCreateTradeOrder", cancelMethod = "cancelCreateTradeOrder")
    @Override
    public void createTradeOrder(CreateTradeOrderDTO dto) {

    }


    @Transactional
    public void confirmCreateTradeOrder(CreateTradeOrderDTO dto){

        log.info("confirmCreateTradeOrder:{}",dto);
        TradeOrderDO tradeOrderDO1 = this.getById(dto.getTradeOrderId());
        if (tradeOrderDO1 != null) {
            log.info("交易单已存在");
            return;
        }
        TradeOrderDO tradeOrderDO = buildTradeOrderDO(dto);
        try {
            this.save(tradeOrderDO);
        } catch (DuplicateKeyException e) {
           log.info("交易单已存在");
        }

    }
    public void cancelCreateTradeOrder(CreateTradeOrderDTO dto){

        log.info("cancelCreateTradeOrder");

    }

    private static TradeOrderDO buildTradeOrderDO(CreateTradeOrderDTO dto) {
        String outTradeNo = PryServiceUtils.generateOutTradeNo(dto.getOrderId());

        TradeOrderDO tradeOrderDO = new TradeOrderDO();
        tradeOrderDO.setBathPayOrderId(-1L);
        tradeOrderDO.setUserId(dto.getUserId());
        tradeOrderDO.setOrderId(dto.getOrderId());
        tradeOrderDO.setOutTradeNo(outTradeNo);
        tradeOrderDO.setTradeMoney(dto.getTradeMoney());
        tradeOrderDO.setPayType(dto.getPayType());
        tradeOrderDO.setPayStatus(0);
        tradeOrderDO.setBathPay(0);
        tradeOrderDO.setVersion(1L);
        return tradeOrderDO;
    }






    @Override
    public CreatePayOrderVO createPayOrder(List<CreatePayOrderDTO> dto) {


        return null;
    }

    private BigDecimal getActualPayMoney(List<BigDecimal> discountMoneyList, BigDecimal orderMoney) {

        BigDecimal actualPayMoney = orderMoney;
        for (BigDecimal a : discountMoneyList) {
            actualPayMoney = BigDecimalUtil.subtract(orderMoney, a);
        }

        return actualPayMoney;
    }

    private Map<Long, UseCouponVO> useCoupon(List<CreatePayOrderDTO> dto) {

        List<UseCouponDTO> dtoList = new ArrayList<>();
        Map<Long, UseCouponVO> useCouponVOMap = new HashMap<>();
        dto.forEach(a -> {
            BigDecimal orderMoney = a.getOrderMoney();
            Long couponId = a.getCouponId();
            if (couponId != null) {
                UseCouponDTO useCouponDTO = new UseCouponDTO();
                useCouponDTO.setUserId(a.getUserId());
                useCouponDTO.setCouponId(a.getCouponId());
                useCouponDTO.setOrderMoney(orderMoney);
                dtoList.add(useCouponDTO);

            }

        });
        if (dtoList.isEmpty()) {

            return useCouponVOMap;
        }
        Result<List<UseCouponVO>> listResult = welfareApiService.bathUseCoupon(dtoList);
        Integer code = listResult.getCode();
        if (code == 200 && !listResult.getData().isEmpty()) {
            //使用成功
            listResult.getData().forEach(a -> useCouponVOMap.put(a.getShopId(), a));

        } else {
            throw new BizException(listResult.getCode(), listResult.getMessage());
        }
        return useCouponVOMap;
    }

    /**
     * 查询订单交易信息
     */
    @Override
    public OrderTradeVO queryOrderTradeByOrderId(Long orderId) {

        //查询用户下单支付的交易单
        TradeOrderDO tradeOrderDO = null;
        if (tradeOrderDO == null) {
            //即使交易没有完成 也不会报错
            return null;
        }

        PayOrderDO payOrderDO = payOrderService.lambdaQuery().eq(PayOrderDO::getBizOrderId, orderId).one();
        if (payOrderDO == null) {
            //即使交易没有完成 也不会报错
            return null;
        }

        TradeOrderItemDO discountInfo = null;

        List<TradeOrderItemDO> list = tradeOrderItemService.lambdaQuery().eq(TradeOrderItemDO::getTradeOrderId, tradeOrderDO.getId()).list();
        for (TradeOrderItemDO a : list) {
            if (a.getPayType().equals(3)) {
                discountInfo = a;
                break;
            }
        }
        //优惠方式
        Integer discountType = null;
        String discountTypeName = null;

        if (discountInfo != null) {
            discountType = discountInfo.getPayType();
            discountTypeName = "优惠券";
        }
        /**
         * 构建返回信息
         */
        OrderTradeVO tradeVO = new OrderTradeVO();
        tradeVO.setOrderId(orderId);
        tradeVO.setPayType(payOrderDO.getPayType());
        tradeVO.setPayMoney(payOrderDO.getPayMoney());
//        tradeVO.setOrderMoney(tradeOrderDO.getOrderMoney());
//        tradeVO.setSource(tradeOrderDO.getSource());
//        tradeVO.setPayFinishTime(payOrderDO.getPayFinishTime());
//        tradeVO.setDiscountMoney(tradeOrderDO.getDiscountMoney());
        tradeVO.setDiscountType(discountType);
        tradeVO.setDiscountTypeName(discountTypeName);
        tradeVO.setPayTypeName(getPayTypeName(payOrderDO.getPayType()));
        tradeVO.setReceiptMoney(payOrderDO.getReceiptMoney());
        tradeVO.setIncomeAccount(payOrderDO.getIncomeAccount());
        tradeVO.setShopId(payOrderDO.getShopId());
        tradeVO.setPayStatus(payOrderDO.getPayStatus());
        return tradeVO;
    }

    private String getPayTypeName(Integer payType) {

        if (payType.equals(0)) {
            return "支付宝";
        }

        if (payType.equals(1)) {
            return "微信";
        }

        if (payType.equals(2)) {
            return "银联";
        }
        return "支付宝";

    }

    @Transactional
    @Override
    public void refund(RefundDTO dto) {




    }

    @Override
    public TradeOrderApiVO tradeOrderQuery(TradeOrderQuery query) {

//        Long orderId = query.getOrderId();
//        Integer source = query.getSource();
//
//        TradeOrderDO tradeOrderDO = this.lambdaQuery().eq(TradeOrderDO::getBizOrderId, orderId).eq(TradeOrderDO::getSource, source).one();
//        Long id = tradeOrderDO.getId();
//        PayOrderDO payOrderDO = payOrderService.lambdaQuery().eq(PayOrderDO::getTradeOrderId, id).one();
//
//        TradeOrderApiVO vo = new TradeOrderApiVO();
//        BeanCopyUtils.copy(tradeOrderDO, vo);
//        vo.setIncomeAccount(payOrderDO.getIncomeAccount());
//        vo.setAccountType(payOrderDO.getPayType());
//        vo.setActualPayMoney(payOrderDO.getPayMoney());
//        vo.setReceiptMoney(payOrderDO.getReceiptMoney());
        return null;
    }

    /**
     * 查询待优化
     * 支付完成后，写入中间表
     */
    @Override
    public Integer tradeOrderQuantitySum(TradeOrderQuantitySumDTO dto) {


        List<TradeOrderDO> tradeOrderDOList = this.lambdaQuery().select(BaseEntity::getId).
                //in(TradeOrderDO::getSource, dto.getSources()).
                eq(TradeOrderDO::getPayStatus, 2).list();
        List<Long> collect = tradeOrderDOList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        return payOrderService.lambdaQuery().in(PayOrderDO::getTradeOrderId, collect).
                eq(PayOrderDO::getPayType, dto.getPayType()).
                eq(PayOrderDO::getPayAccount, dto.getPayAccount()).
                eq(PayOrderDO::getPayStatus, 2).count();
    }

    @Override
    public List<TradeOrderBathVO> tradeOrderBathQuery(TradeOrderBathQuery query) {

        List<Long> tradeOrderIdList = query.getTradeOrderIdList();
        List<Long> orderIdList = query.getOrderIdList();
        List<PayOrderDO> orderDOList = payOrderService.lambdaQuery().
                in(tradeOrderIdList != null, PayOrderDO::getTradeOrderId, tradeOrderIdList).
                in(orderIdList != null, PayOrderDO::getBizOrderId, orderIdList).
                list();

        List<TradeOrderBathVO> voList = new ArrayList<>(orderDOList.size());

        orderDOList.forEach(a -> {
            TradeOrderBathVO vo = new TradeOrderBathVO();
            vo.setPayMoney(a.getPayMoney());
            vo.setTradeOrderId(a.getTradeOrderId());
            vo.setPayType(a.getPayType());
            vo.setOrderId(a.getBizOrderId());
            voList.add(vo);
        });

        return voList;
    }


}
