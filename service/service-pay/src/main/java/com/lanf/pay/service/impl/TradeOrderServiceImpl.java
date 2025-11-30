package com.lanf.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.mapper.TradeOrderMapper;
import com.lanf.pay.model.dto.CreatePayOrderDTO;
import com.lanf.pay.model.dto.TradeOrderQuantitySumDTO;
import com.lanf.pay.model.entity.BathPayOrderDO;
import com.lanf.pay.model.entity.PayOrderDO;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.entity.TradeOrderItemDO;
import com.lanf.pay.model.query.TradeOrderBathQuery;
import com.lanf.pay.model.query.TradeOrderQuery;
import com.lanf.pay.model.vo.*;
import com.lanf.pay.service.IBathPayOrderService;
import com.lanf.pay.service.IPayOrderService;
import com.lanf.pay.service.ITradeOrderItemService;
import com.lanf.pay.service.ITradeOrderService;
import com.lanf.rocketmq.model.message.RefundDTO;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.welfare.api.WelfareApiService;
import com.lanf.welfare.model.dto.UseCouponDTO;
import com.lanf.welfare.model.vo.UseCouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 交易订单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-14
 */
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


    @Override
    public CreatePayOrderVO createPayOrder(List<CreatePayOrderDTO> dto) {

        dto.forEach(a -> {

            List<TradeOrderDO> tradeOrderDO = this.lambdaQuery().eq(TradeOrderDO::getBizOrderId, a.getBizOrderId()).list();
            if (!tradeOrderDO.isEmpty()) {
                throw new BizException("交易订单已存在");
            }

        });

        List<TradeOrderDO> tradeOrderDOList = new ArrayList<>();
        List<PayOrderDO> payOrderDOList = new ArrayList<>();
        List<CreatePayOrderItemVO> createPayOrderItemVOList = new ArrayList<>();
        BigDecimal totalMoney = new BigDecimal(0);
        //实际支付总金额
        BigDecimal totalActualPayMoney = new BigDecimal(0);
        //批量支付单id
        Long bathPayOrderId = IdUtils.generateId();
        List<TradeOrderItemDO> tradeOrderItemDOList = new ArrayList<>();
        //
        //使用优惠券
        Map<Long, UseCouponVO> useCouponVOMap = useCoupon(dto);

        //////
        for (CreatePayOrderDTO b : dto) {
            /**
             * 优惠信息
             */
            //优惠金额
            List<BigDecimal> discountMoneyList = new ArrayList<>();
            UseCouponVO useCouponVO = useCouponVOMap.get(b.getShopId());
            if (useCouponVO != null) {
                discountMoneyList.add(useCouponVO.getDiscountMoney());
            }

            /**
             * 计算
             */
            BigDecimal orderMoney = b.getOrderMoney();

            //实际订单支付金额
            BigDecimal actualPayMoney = getActualPayMoney(discountMoneyList, orderMoney);
            //构建TradeOrderDO
            TradeOrderDO tradeOrderDO = new TradeOrderDO();
            tradeOrderDOList.add(tradeOrderDO);
            Long id = IdUtils.generateId();
            tradeOrderDO.setId(id);
            tradeOrderDO.setOrderNumber(CodeGenerateUtils.generateOrderNumber());
            tradeOrderDO.setUserId(b.getUserId());
            tradeOrderDO.setShopId(b.getShopId());
            tradeOrderDO.setBizOrderId(b.getBizOrderId());
            tradeOrderDO.setSource(b.getSource());
            tradeOrderDO.setPlaceOrderTime(new Date());
            tradeOrderDO.setOrderMoney(orderMoney);
            tradeOrderDO.setPayStatus(0);
            tradeOrderDO.setBathPayOrderId(bathPayOrderId);
            /**
             * 构建TradeOrderItemDO
             */
            TradeOrderItemDO tradeOrderItemDO = new TradeOrderItemDO();
            tradeOrderItemDO.setTradeOrderId(id);
            tradeOrderItemDO.setBizOrderId(b.getBizOrderId());
            tradeOrderItemDO.setTradeMoney(actualPayMoney);
            tradeOrderItemDO.setPayType(0);
            tradeOrderItemDOList.add(tradeOrderItemDO);
            if (useCouponVO != null) {
                TradeOrderItemDO v2 = new TradeOrderItemDO();
                v2.setTradeOrderId(id);
                v2.setBizOrderId(useCouponVO.getCouponId());
                v2.setTradeMoney(useCouponVO.getDiscountMoney());
                v2.setPayType(1);
                tradeOrderItemDOList.add(v2);
            }

            //构建PayOrderDO
            PayOrderDO payOrderDO = new PayOrderDO();
            payOrderDOList.add(payOrderDO);
            payOrderDO.setOrderNumber(CodeGenerateUtils.generateOrderNumber());
            payOrderDO.setUserId(b.getUserId());
            payOrderDO.setShopId(b.getShopId());
            payOrderDO.setPayStatus(0);
            payOrderDO.setPayMoney(actualPayMoney);
            payOrderDO.setTradeOrderId(id);
            payOrderDO.setBizOrderId(b.getBizOrderId());
            payOrderDO.setBathPayOrderId(bathPayOrderId);
            //构建返回信息
            CreatePayOrderItemVO createPayOrderItemVO = new CreatePayOrderItemVO();
            createPayOrderItemVO.setShopId(b.getShopId());
            createPayOrderItemVO.setTotalMoney(orderMoney);
            createPayOrderItemVO.setActualPayMoney(actualPayMoney);
            createPayOrderItemVOList.add(createPayOrderItemVO);
            //实际支付总金额
            totalActualPayMoney = BigDecimalUtils.add(totalActualPayMoney, actualPayMoney);
        }
        BathPayOrderDO bathPayOrderDO = new BathPayOrderDO();
        CreatePayOrderDTO createPayOrderDTO = dto.get(0);
        bathPayOrderDO.setUserId(createPayOrderDTO.getUserId());
        bathPayOrderDO.setMainOrderId(createPayOrderDTO.getMainOrderId());
        bathPayOrderDO.setBatchNo(CodeGenerateUtils.generateOrderNumber());
        bathPayOrderDO.setBatchNum(payOrderDOList.size());
        bathPayOrderDO.setBatchFee(totalActualPayMoney);
        bathPayOrderDO.setId(bathPayOrderId);


        //进行保存
        this.saveBatch(tradeOrderDOList);
        payOrderService.saveBatch(payOrderDOList);
        bathPayOrderService.save(bathPayOrderDO);
        tradeOrderItemService.saveBatch(tradeOrderItemDOList);
        //构建返回信息
        CreatePayOrderVO createPayOrderVO = new CreatePayOrderVO();
        createPayOrderVO.setTotalMoney(totalMoney);
        createPayOrderVO.setCreatePayOrderItemVOList(createPayOrderItemVOList);

        return createPayOrderVO;
    }

    private BigDecimal getActualPayMoney(List<BigDecimal> discountMoneyList, BigDecimal orderMoney) {

        BigDecimal actualPayMoney = orderMoney;
        for (BigDecimal a : discountMoneyList) {
            actualPayMoney = BigDecimalUtils.subtract(orderMoney, a);
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
        TradeOrderDO tradeOrderDO = this.lambdaQuery().eq(TradeOrderDO::getBizOrderId, orderId).eq(TradeOrderDO::getSource, 0)
                .one();
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
        tradeVO.setOrderMoney(tradeOrderDO.getOrderMoney());
        tradeVO.setSource(tradeOrderDO.getSource());
        tradeVO.setPayFinishTime(payOrderDO.getPayFinishTime());
        tradeVO.setDiscountMoney(tradeOrderDO.getDiscountMoney());
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

        Long orderId = query.getOrderId();
        Integer source = query.getSource();

        TradeOrderDO tradeOrderDO = this.lambdaQuery().eq(TradeOrderDO::getBizOrderId, orderId).eq(TradeOrderDO::getSource, source).one();
        Long id = tradeOrderDO.getId();
        PayOrderDO payOrderDO = payOrderService.lambdaQuery().eq(PayOrderDO::getTradeOrderId, id).one();

        TradeOrderApiVO vo = new TradeOrderApiVO();
        BeanCopyUtils.copy(tradeOrderDO, vo);
        vo.setIncomeAccount(payOrderDO.getIncomeAccount());
        vo.setAccountType(payOrderDO.getPayType());
        vo.setActualPayMoney(payOrderDO.getPayMoney());
        vo.setReceiptMoney(payOrderDO.getReceiptMoney());
        return vo;
    }

    /**
     * 查询待优化
     * 支付完成后，写入中间表
     */
    @Override
    public Integer tradeOrderQuantitySum(TradeOrderQuantitySumDTO dto) {


        List<TradeOrderDO> tradeOrderDOList = this.lambdaQuery().select(BaseEntity::getId).
                in(TradeOrderDO::getSource, dto.getSources()).
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
