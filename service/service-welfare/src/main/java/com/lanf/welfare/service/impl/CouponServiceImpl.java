package com.lanf.welfare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.messagemanager.client.model.dto.SendMqMessageDTO;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.enums.EventCodeEnum;
import com.lanf.rocketmq.model.message.DeductCouponTemplateCountMsg;
import com.lanf.welfare.mapper.CouponMapper;
import com.lanf.welfare.model.bo.DeductShopCouponRemainCountCacheBO;
import com.lanf.welfare.model.bo.DiscountInfoBO;
import com.lanf.welfare.model.bo.FullDiscountUseConditionBO;
import com.lanf.welfare.model.dto.CalculateDiscountAmountDTO;
import com.lanf.welfare.model.dto.ReceiveShopCouponDTO;
import com.lanf.welfare.model.entity.CouponDO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.enums.CouponTemplateStatus;
import com.lanf.welfare.model.vo.CalculateDiscountAmountVO;
import com.lanf.welfare.service.ICouponService;
import com.lanf.welfare.service.ICouponTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.*;
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
    private  CouponCacheService couponCacheService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private ISendMqMessageService sendMqMessageService;

    @DistributedLock(key = "#dto.userId")//防止重复领取
    @Override
    public void receiveShopCoupon(ReceiveShopCouponDTO dto) {

        validateCouponTemplate( dto);
        //扣减店铺优惠券数量
        ductCouponCount(dto.getCouponTemplateId(), dto.getShopId());
        //插入优惠卷
        CouponDO couponDO = buildCouponDO(dto);

        //保存
        SendMqMessageDTO sendMqMessageDTO = buildSendMqMessageDTO(dto);

        transactionTemplate.execute(status -> {
            try {
                this.save(couponDO);
                sendMqMessageService.createSendMqMessage(sendMqMessageDTO);
                // 如果一切正常，事务会自动提交
                return null;
            } catch (Exception e) {
                // 发生异常时手动回滚
                status.setRollbackOnly();
                throw e;

            }
        });
        //发送mq 扣減DB优惠卷模板数量
        sendMqMessageService.sendMqMessage(sendMqMessageDTO);
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

    private SendMqMessageDTO buildSendMqMessageDTO(ReceiveShopCouponDTO dto){

        String key = dto.getUserId()+":"+dto.getCouponTemplateId();
        String bizKeyValue = EventCodeEnum.buildBizKey(key, EventCodeEnum.DEDUCT_COUPON_TEMPLATE_COUNT.getCode());
        DeductCouponTemplateCountMsg msg = new DeductCouponTemplateCountMsg();
        msg.setBizKeyValue(bizKeyValue);
        msg.setCouponTemplateId(dto.getCouponTemplateId());
        msg.setDeductCount(1);

        return new SendMqMessageDTO(TopicName.DEDUCT_COUPON_TEMPLATE_COUNT_TOPIC,msg);
    }



    private void ductCouponCount(Long couponTemplateId, Long shopId){
        DeductShopCouponRemainCountCacheBO bo = couponCacheService.
                deductShopCouponRemainCountCache(shopId, couponTemplateId);
        Integer resultStatus = bo.getResultStatus();
        if ( resultStatus == -1){
            //key不存在
            Map<String, String> remainCount = couponTemplateService.buildShopCouponRemainCount(shopId);
            if (remainCount.isEmpty()){
                log.info("店铺优惠卷不存在");
                throw new BizException("店铺优惠卷不存在");
            }

        } else if (resultStatus == 0){
            log.info("店铺优惠卷数量不足");
            throw new BizException("店铺优惠卷数量不足");
        } else if (resultStatus == 1){
            log.info("扣减店铺优惠卷成功");
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

        // 查询可使用的优惠券
        List<CouponDO> availableCoupons = this.lambdaQuery()
                .eq(CouponDO::getUserId, dto.getUserId())           // 指定用户
                .eq(CouponDO::getStatus, 0)                         // 状态为可用（未使用）
                .ge(CouponDO::getUseStartTime, new Date())          // 使用开始时间小于等于当前时间
                .le(CouponDO::getUseEndTime, new Date())            // 使用结束时间大于等于当前时间
                .list();

        if (availableCoupons.isEmpty()){
            log.info("无可用优惠券");
            CalculateDiscountAmountVO vo = new CalculateDiscountAmountVO();
            vo.setTotalDiscountAmount(new BigDecimal(0));
            return vo;
        }
        // 进一步根据订单金额筛选满足使用条件的优惠券，并确保每种type只保留一个
        List<CouponDO> filteredCoupons = availableCoupons.stream()
                .filter(coupon -> isCouponUsable(coupon, dto.getTotalAmount()))
                .collect(Collectors.groupingBy(CouponDO::getType))   // 按type分组
                .values()                                           // 获取每组的集合
                .stream()                                           // 对每组进行处理
                .map(typeGroup -> typeGroup.get(0))                 // 每组只取第一个优惠券
                .collect(Collectors.toList());

        if (filteredCoupons.isEmpty()){
            log.info("没有满足条件的优惠券");
            CalculateDiscountAmountVO vo = new CalculateDiscountAmountVO();
            vo.setTotalDiscountAmount(new BigDecimal(0));
            return vo;
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

}
