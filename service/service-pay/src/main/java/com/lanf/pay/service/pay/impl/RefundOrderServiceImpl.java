package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.query.RefundOrderPageQuery;
import com.lanf.api.pay.model.vo.RefundOrderPageVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.mapper.RefundOrderMapper;
import com.lanf.pay.model.entity.RefundOrderDO;
import com.lanf.pay.service.pay.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 退款单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-27
 */
@Slf4j
@Service
public class RefundOrderServiceImpl extends ServiceImpl<RefundOrderMapper, RefundOrderDO> implements IRefundOrderService {

    @Override
    public PageResult<RefundOrderPageVO> refundOrderPageQuery(RefundOrderPageQuery query) {
        Page<RefundOrderDO> page = this.page(new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<RefundOrderDO>()
                        .orderByDesc(RefundOrderDO::getCreateTime));
        List<RefundOrderDO> records = page.getRecords();
        if (records.isEmpty()) {
            return null;
        }
        List<RefundOrderPageVO> pageVOS = BeanCopyUtils.copyBeanList(records, RefundOrderPageVO.class);
        PageResult<RefundOrderPageVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(pageVOS);
        result.setSize(page.getSize());
        return result;
    }




}
