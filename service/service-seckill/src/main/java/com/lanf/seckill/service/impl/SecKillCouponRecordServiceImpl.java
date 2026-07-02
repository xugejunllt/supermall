package com.lanf.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.seckill.mapper.SecKillCouponRecordMapper;
import com.lanf.seckill.model.entity.SecKillCouponRecordDO;
import com.lanf.seckill.service.ISecKillCouponRecordService;
import com.lanf.seckill.model.query.SecKillCouponRecordPageQuery;
import com.lanf.seckill.model.vo.SecKillCouponRecordPageVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 秒杀优惠券记录表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Service
public class SecKillCouponRecordServiceImpl extends ServiceImpl<SecKillCouponRecordMapper, SecKillCouponRecordDO> implements ISecKillCouponRecordService {

    @Override
    public PageResult<SecKillCouponRecordPageVO> seckillCouponRecordPageQuery(SecKillCouponRecordPageQuery query) {
        Long userId = query.getUserId();
        Long secKillCouponItemId = query.getSecKillCouponItemId();
        Integer status = query.getStatus();
        long page = query.getPage();
        long pageSize = query.getPageSize();

        Page<SecKillCouponRecordDO> pageParam = new Page<>(page, pageSize);

        LambdaQueryWrapper<SecKillCouponRecordDO> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(SecKillCouponRecordDO::getUserId, userId);
        }
        if (secKillCouponItemId != null) {
            wrapper.eq(SecKillCouponRecordDO::getSecKillCouponItemId, secKillCouponItemId);
        }
        if (status != null) {
            wrapper.eq(SecKillCouponRecordDO::getStatus, status);
        }
        wrapper.orderByDesc(SecKillCouponRecordDO::getCreateTime);

        Page<SecKillCouponRecordDO> resultPage = this.page(pageParam, wrapper);

        List<SecKillCouponRecordDO> records = resultPage.getRecords();
        if (records.isEmpty()) {
            return PageResult.emptyResult();
        }

        return new PageResult<>(BeanCopyUtils.copyBeanList(records, SecKillCouponRecordPageVO.class), pageSize, resultPage.getTotal());
    }

}
