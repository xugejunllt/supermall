package com.lanf.welfare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.cache.aop.DistributedLock;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.tcc.service.ITccOperationService;
import com.lanf.welfare.mapper.CouponMapper;
import com.lanf.welfare.model.bo.DiscountInfoBO;
import com.lanf.welfare.model.bo.FullDiscountUseConditionBO;
import com.lanf.welfare.model.dto.CalculateDiscountAmountDTO;
import com.lanf.welfare.model.dto.ReceiveShopCouponDTO;
import com.lanf.welfare.model.dto.UseMultipleCouponDTO;
import com.lanf.welfare.model.entity.CouponDO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.entity.OrderCouponDO;
import com.lanf.welfare.model.enums.CouponTemplateStatus;
import com.lanf.welfare.model.vo.CalculateDiscountAmountVO;
import com.lanf.welfare.service.ICouponService;
import com.lanf.welfare.service.ICouponTemplateService;
import com.lanf.welfare.service.IOrderCouponService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 优惠券 服务实现类
 *
 *
 * @since 2024-08-01
 */
@Slf4j
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, CouponDO> implements ICouponService {

    @Autowired
    private ICouponTemplateService couponTemplateService;



    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ITccOperationService tccOperationService;
    @Autowired
    private IOrderCouponService orderCouponService;


    @DistributedLock(key = "#dto.userId")//防止重复领取
    @Override
    public void receiveShopCoupon(ReceiveShopCouponDTO dto) {

        validateCouponTemplate( dto);
        //扣减店铺优惠券数量
        ductCouponCount(dto.getCouponTemplateId(), dto.getShopId());
        //插入优惠卷
        CouponDO couponDO = buildCouponDO(dto);

//        //保存
//        SendMqMessageDTO sendMqMessageDTO = buildSendMqMessageDTO(dto);
//
//        transactionTemplate.execute(status -> {
//            try {
//                this.save(couponDO);
//                sendMqMessageService.createSendMqMessage(sendMqMessageDTO);
//                // 如果一切正常，事务会自动提交
//                return null;
//            } catch (Exception e) {
//                // 发生异常时手动回滚
//                status.setRollbackOnly();
//                throw e;
//
//            }
//        });
//        //发送mq 扣減DB优惠卷模板数量
//        sendMqMessageService.sendMqMessage(sendMqMessageDTO);
    }


    private CouponDO buildCouponDO(ReceiveShopCouponDTO dto){

        Long couponTemplateId = dto.getCouponTemplateId();
        CouponTemplateDO templateDO = couponTemplateService.getById(couponTemplateId);

        CouponDO couponDO = new CouponDO();
        couponDO.setCouponTemplateId(couponTemplateId);
        couponDO.setUserId(dto.getUserId());
        couponDO.setCouponType(templateDO.getCouponType());
        couponDO.setShopId(templateDO.getShopId());
        couponDO.setName(templateDO.getName());
        couponDO.setTitle(templateDO.getTitle());
        couponDO.setStatus(0);
        couponDO.setType(templateDO.getType());
        couponDO.setUseCondition(templateDO.getUseCondition());
        couponDO.setUseStartTime(templateDO.getUseStartTime());
        couponDO.setUseEndTime(templateDO.getUseEndTime());
        couponDO.setCouponTemplateVersion(templateDO.getVersion());
        couponDO.setVersion(1L);


        return couponDO;
    }

//    private SendMqMessageDTO buildSendMqMessageDTO(ReceiveShopCouponDTO dto){
//
//        String key = dto.getUserId()+":"+dto.getCouponTemplateId();
//        String bizKeyValue = EventCodeEnum.buildBizKey(key, EventCodeEnum.DEDUCT_COUPON_TEMPLATE_COUNT.getCode());
//        DeductCouponTemplateCountMsg msg = new DeductCouponTemplateCountMsg();
//        msg.setBizKeyValue(bizKeyValue);
//        msg.setCouponTemplateId(dto.getCouponTemplateId());
//        msg.setDeductCount(1);
//
//        return new SendMqMessageDTO(TopicName.DEDUCT_COUPON_TEMPLATE_COUNT_TOPIC,msg);
//    }



    private void ductCouponCount(Long couponTemplateId, Long shopId){
//        DeductShopCouponRemainCountCacheBO bo = couponCacheService.
//                deductShopCouponRemainCountCache(shopId, couponTemplateId);
        int resultStatus = -1;
        //key不存在
        Map<String, String> remainCount = couponTemplateService.buildShopCouponRemainCount(shopId);
        if (remainCount.isEmpty()){
            log.info("店铺优惠卷不存在");
            throw new BizException("店铺优惠卷不存在");
        }

    }

    private void validateCouponTemplate(ReceiveShopCouponDTO dto){

        Long couponTemplateId = dto.getCouponTemplateId();
        Long shopId = dto.getShopId();
        CouponTemplateDO templateDO = couponTemplateService.getById(couponTemplateId);

        if ( templateDO == null){
            log.warn("优惠卷模板不存在");
            throw new BizException("优惠卷模板不存在");
        }
        if ( !shopId.equals(templateDO.getShopId())){
            log.warn("优惠卷模板不属于该店铺");
            throw new BizException("优惠卷模板不属于该店铺");
        }
        if ( !CouponTemplateStatus.PUSH.getCode().equals(templateDO.getStatus())){
            log.warn("优惠卷模板未发布");
            throw new BizException("优惠卷模板未发布");
        }
        if (templateDO.getReceiveStartTime().getTime() > new Date().getTime()){
            log.warn("未到领取时间");
            throw new BizException("未到领取时间");
        }
        if (templateDO .getReceiveEndTime().getTime() < new Date().getTime()){
            log.warn("已超过领取时间");
            throw new BizException("已超过领取时间");
        }
        //每个用户领取数量限制
        Integer receiveCount = templateDO.getReceiveCount();
        List<CouponDO> couponDOList = this.lambdaQuery().eq(CouponDO::getUserId, dto.getUserId()).list();
        if ( couponDOList.size() >= receiveCount){
            log.warn("超出领取数量限制");
            throw new BizException("超出领取数量限制");
        }


    }

    @Override
    public CalculateDiscountAmountVO calculateDiscountAmount(CalculateDiscountAmountDTO dto) {


        return calculateDiscountAmount( dto,null);
    }

    private CalculateDiscountAmountVO calculateDiscountAmount(CalculateDiscountAmountDTO dto,List<Long> couponIds){

        // 查询可使用的优惠券
        List<CouponDO> availableCoupons = findAvailableCoupons(dto.getUserId(), couponIds);
        if (availableCoupons.isEmpty()){
            log.info("无可用优惠券");
            CalculateDiscountAmountVO vo = new CalculateDiscountAmountVO();
            vo.setTotalDiscountAmount(new BigDecimal(0));
            return vo;
        }
        // 进一步根据订单金额筛选满足使用条件的优惠券，并确保每种type只保留一个
        List<CouponDO> filteredCoupons = filterCouponsByOrderAmountAndType(availableCoupons, dto.getTotalAmount());

        return buildCalculateDiscountAmountResult(filteredCoupons);
    }
    /**
     * 查询用户可用的优惠券列表
     *
     * @param userId 用户ID
     * @param couponIds 优惠券ID列表（可选）
     * @return 可用优惠券列表
     */
    private List<CouponDO> findAvailableCoupons(Long userId, List<Long> couponIds) {
        return this.lambdaQuery()
                .eq(CouponDO::getUserId, userId)                        // 指定用户
                .eq(CouponDO::getStatus, 0)                             // 状态为可用（未使用）
                .ge(CouponDO::getUseStartTime, new Date())              // 使用开始时间小于等于当前时间
                .le(CouponDO::getUseEndTime, new Date())                // 使用结束时间大于等于当前时间
                .in(couponIds != null && !couponIds.isEmpty(), CouponDO::getId, couponIds)
                .list();
    }
    private List<DiscountInfoBO> buildDiscountInfoBOList(List<CouponDO> coupons){

        List<DiscountInfoBO> list = new ArrayList<>();
        for (CouponDO coupon : coupons){
            DiscountInfoBO bo = new DiscountInfoBO();
            bo.setName(coupon.getName());
            bo.setTitle(coupon.getTitle());
            // 解析优惠券金额
            bo.setDiscountAmount(parseDiscountAmount(coupon));
            list .add(bo);
        }
        return list;
    }
    /**
     * 根据订单金额筛选满足使用条件的优惠券，并确保每种type只保留一个
     *
     * @param availableCoupons 可用优惠券列表
     * @param orderAmount 订单金额
     * @return 筛选后的优惠券列表
     */
    private List<CouponDO> filterCouponsByOrderAmountAndType(List<CouponDO> availableCoupons, BigDecimal orderAmount) {
        return availableCoupons.stream()
                .filter(coupon -> isCouponUsable(coupon, orderAmount))
                .collect(Collectors.groupingBy(CouponDO::getType))   // 按type分组
                .values()                                           // 获取每组的集合
                .stream()                                           // 对每组进行处理
                .map(typeGroup -> typeGroup.get(0))                 // 每组只取第一个优惠券
                .collect(Collectors.toList());
    }

    /**
     * 构建计算折扣金额的结果对象
     *
     * @param filteredCoupons 筛选后的优惠券列表
     * @return 计算折扣金额结果对象
     */
    private CalculateDiscountAmountVO buildCalculateDiscountAmountResult(List<CouponDO> filteredCoupons) {
        if (filteredCoupons.isEmpty()) {
            log.info("没有满足条件的优惠券");

            return null;
        }
        //折扣信息
        List<DiscountInfoBO> discountInfoBOS = buildDiscountInfoBOList(filteredCoupons);
        //计算优惠总金额
        BigDecimal totalDiscountAmount = calculateTotalDiscountAmount(discountInfoBOS);
        // 构建返回结果
        CalculateDiscountAmountVO result = new CalculateDiscountAmountVO();
        result.setTotalDiscountAmount(totalDiscountAmount);
        result.setDiscountInfoBOList(discountInfoBOS);

        return result;
    }
    private BigDecimal parseDiscountAmount(CouponDO coupon){

        if (coupon.getType() == 0){

            /**
             *  满减券  是否满足消费金额
             *
             */
            String useCondition = coupon.getUseCondition();

            FullDiscountUseConditionBO bo = JsonUtils.toObject(useCondition, FullDiscountUseConditionBO.class);
            return bo.getDiscountMoney() ;
        }
        return BigDecimal.ZERO ;
    }
    /**
     * 计算总优惠金额
     */
    private BigDecimal calculateTotalDiscountAmount(List<DiscountInfoBO> discountInfoBOS) {

     // 直接从 discountInfoBOS 转换为 BigDecimal 数组
        BigDecimal[] decimalArray = discountInfoBOS.stream()
                .map(DiscountInfoBO::getDiscountAmount)
                .toArray(BigDecimal[]::new);

        return BigDecimalUtil.add((decimalArray));
    }
    private boolean isCouponUsable(CouponDO coupon, BigDecimal orderAmount){

        if (coupon.getType() == 0){

            /**
             *  满减券  是否满足消费金额
             *
             */
            String useCondition = coupon.getUseCondition();

            FullDiscountUseConditionBO bo = JsonUtils.toObject(useCondition, FullDiscountUseConditionBO.class);
            return BigDecimalUtil.ge(orderAmount, bo.getFullMoney());
        }

        return false;
    }

    @Transactional
    @HmilyTCC(confirmMethod = "confirmUseMultipleCoupon", cancelMethod = "cancelUseMultipleCoupon")
    @Override
    public CalculateDiscountAmountVO useMultipleCoupon(UseMultipleCouponDTO dto) {

        List<Long> couponIds = dto.getCouponIds();

        List<CouponDO> availableCoupons = findAvailableCoupons(dto.getUserId(), couponIds);

        if (availableCoupons.isEmpty()){
            log.info("没有可用的优惠券");
            return null;
        }
        // 进一步根据订单金额筛选满足使用条件的优惠券，并确保每种type只保留一个
        List<CouponDO> filteredCoupons = filterCouponsByOrderAmountAndType(availableCoupons, dto.getTotalAmount());
        CalculateDiscountAmountVO amountResult = buildCalculateDiscountAmountResult(filteredCoupons);

        if (amountResult == null){
            return null;
        }
        /**
         * 冻结优惠卷
         */
        Map<Long, CouponDO> couponDOMap = availableCoupons.stream()
                .collect(Collectors.toMap(CouponDO::getId, coupon -> coupon));
        List<Long> useCouponIds = amountResult.getDiscountInfoBOList().stream()
                .map(DiscountInfoBO::getCouponId).collect(Collectors.toList());

        String bizKey = buildUseMultipleCouponBizKey(dto.getOrderId());

        List<OrderCouponDO> couponDOList = new ArrayList<>();
        tccOperationService.tryOperation(bizKey,null);
        for (Long couponId : useCouponIds){
            CouponDO couponDO = couponDOMap.get(couponId);

            boolean update = this.lambdaUpdate()
                    .eq(CouponDO::getId, couponId)
                    .eq(CouponDO::getVersion, couponDO.getVersion())
                    .set(CouponDO::getStatus, 1)
                    .set(CouponDO::getVersion, couponDO.getVersion() + 1)
                    .update();
            if ( !update){
                throw new BizException("优惠卷更新失败");
            }
            OrderCouponDO orderCouponDO = new OrderCouponDO();
            orderCouponDO.setCouponId(couponId);
            orderCouponDO.setOrderId(dto.getOrderId());
            couponDOList.add(orderCouponDO);
        }
        orderCouponService.saveBatch(couponDOList);

        return amountResult;
    }
    /**
     * 构建业务键用于TCC事务操作
     *
     * @param orderId 订单ID
     * @return 业务键
     */
    private String buildUseMultipleCouponBizKey(Long orderId) {
        return "useMultipleCoupon:" + orderId;
    }
    public void confirmUseMultipleCoupon(UseMultipleCouponDTO dto) {

        log.info("执行confirmUseMultipleCoupon:{}", dto);
        String bizKey = buildUseMultipleCouponBizKey(dto.getOrderId());
        try {
            tccOperationService.confirmOperation(bizKey);
        } catch (Exception e) {
            log.error("执行confirmUseMultipleCoupon异常",e);
            throw e;
        }


    }
    @Transactional
    public void cancelUseMultipleCoupon(UseMultipleCouponDTO dto) {
        
        log.info("执行cancelUseMultipleCoupon:{}", dto);

        List<OrderCouponDO> couponDOList = orderCouponService.lambdaQuery().eq(OrderCouponDO::getOrderId, dto.getOrderId())
                .list();

        List<Long> orderCouponIds = couponDOList.stream().map(OrderCouponDO::getId).collect(Collectors.toList());
        List<Long> couponIds = couponDOList.stream().map(OrderCouponDO::getCouponId).collect(Collectors.toList());
        List<CouponDO> couponDOList1 = this.lambdaQuery()
                .in(BaseEntity::getId, couponIds)
                .eq(CouponDO::getStatus, 1)
                .list();
        /**
         * DB操作
         */
        String bizKey = buildUseMultipleCouponBizKey(dto.getOrderId());

        boolean operation = tccOperationService.cancelOperation(bizKey);
        if ( !operation){
            log.info("cancel已执行");
            return;
        }
        for (CouponDO couponDO : couponDOList1){
            boolean update = this.lambdaUpdate()
                    .eq(CouponDO::getId, couponDO.getId())
                    .eq(CouponDO::getVersion, couponDO.getVersion())
                    .set(CouponDO::getStatus, 0)
                    .set(CouponDO::getVersion, couponDO.getVersion() + 1)
                    .update();
            if ( !update){
                throw new BizException("优惠卷更新失败");
            }
        }
        orderCouponService.removeByIds(orderCouponIds);


    }

}
