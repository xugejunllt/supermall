package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.query.RefundOrderFlowPageQuery;
import com.lanf.api.pay.model.vo.RefundOrderFlowPageVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.mapper.RefundOrderFlowMapper;
import com.lanf.pay.model.entity.RefundOrderFlowDO;
import com.lanf.pay.service.pay.IRefundOrderFlowService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 退款单表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-02
 */
@Service
public class RefundOrderFlowServiceImpl extends ServiceImpl<RefundOrderFlowMapper, RefundOrderFlowDO> implements IRefundOrderFlowService {

    @Override
    public BigDecimal sumReturnMoney(String payFinishDate) {
        return baseMapper.sumReturnMoney(payFinishDate);
    }

    @Override
    public PageResult<RefundOrderFlowPageVO> refundOrderFlowPageQuery(RefundOrderFlowPageQuery query) {
        Page<RefundOrderFlowDO> page = this.page(new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<RefundOrderFlowDO>()
                        .orderByDesc(RefundOrderFlowDO::getCreateTime));
        List<RefundOrderFlowDO> records = page.getRecords();
        if (records.isEmpty()) {
            return null;
        }
        List<RefundOrderFlowPageVO> pageVOS = BeanCopyUtils.copyBeanList(records, RefundOrderFlowPageVO.class);
        PageResult<RefundOrderFlowPageVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(pageVOS);
        result.setSize(page.getSize());
        return result;
    }
}
