package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.query.PayOrderFlowPageQuery;
import com.lanf.api.pay.model.vo.PayOrderFlowPageVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.mapper.PayOrderFlowMapper;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 支付流水 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2025-12-28
 */
@Service
 class PayOrderFlowServiceImpl extends ServiceImpl<PayOrderFlowMapper, PayOrderFlowDO> implements IPayOrderFlowService {

    @Override
    public BigDecimal sumReceiptMoney(String payFinishDate) {
        return baseMapper.sumReceiptMoney(payFinishDate);
    }

    @Override
    public PageResult<PayOrderFlowPageVO> payOrderFlowPageQuery(PayOrderFlowPageQuery query) {
        Page<PayOrderFlowDO> page = this.page(new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<PayOrderFlowDO>()
                        .orderByDesc(PayOrderFlowDO::getCreateTime));
        List<PayOrderFlowDO> records = page.getRecords();
        if (records.isEmpty()) {
            return null;
        }
        List<PayOrderFlowPageVO> pageVOS = BeanCopyUtils.copyBeanList(records, PayOrderFlowPageVO.class);
        PageResult<PayOrderFlowPageVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(pageVOS);
        result.setSize(page.getSize());
        return result;
    }
}
