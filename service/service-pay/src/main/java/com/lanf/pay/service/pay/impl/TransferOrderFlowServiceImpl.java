package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.query.TransferOrderFlowPageQuery;
import com.lanf.api.pay.model.vo.TransferOrderFlowPageVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.mapper.TransferOrderFlowMapper;
import com.lanf.pay.model.entity.TransferOrderFlowDO;
import com.lanf.pay.service.pay.ITransferOrderFlowService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 转账单 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-02
 */
@Service
public class TransferOrderFlowServiceImpl extends ServiceImpl<TransferOrderFlowMapper, TransferOrderFlowDO> implements ITransferOrderFlowService {

    @Override
    public BigDecimal sumTotalAmount(String payFinishDate) {
        return baseMapper.sumTotalAmount(payFinishDate);
    }

    @Override
    public PageResult<TransferOrderFlowPageVO> transferOrderFlowPageQuery(TransferOrderFlowPageQuery query) {
        Page<TransferOrderFlowDO> page = this.page(new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<TransferOrderFlowDO>()
                        .orderByDesc(TransferOrderFlowDO::getCreateTime));
        List<TransferOrderFlowDO> records = page.getRecords();
        if (records.isEmpty()) {
            return null;
        }
        List<TransferOrderFlowPageVO> pageVOS = BeanCopyUtils.copyBeanList(records, TransferOrderFlowPageVO.class);
        PageResult<TransferOrderFlowPageVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(pageVOS);
        result.setSize(page.getSize());
        return result;
    }
}
