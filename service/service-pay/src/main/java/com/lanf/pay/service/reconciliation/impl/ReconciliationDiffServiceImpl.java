package com.lanf.pay.service.reconciliation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.mapper.ReconciliationDiffMapper;
import com.lanf.pay.model.entity.ReconciliationDiffDO;
import com.lanf.pay.model.query.ReconciliationDiffPageQuery;
import com.lanf.pay.model.vo.ReconciliationDiffPageVO;
import com.lanf.pay.service.reconciliation.IReconciliationDiffService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 对账差异明细表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
@Service
public class ReconciliationDiffServiceImpl extends ServiceImpl<ReconciliationDiffMapper, ReconciliationDiffDO> implements IReconciliationDiffService {

    @Override
    public PageResult<ReconciliationDiffPageVO> reconciliationDiffPageQuery(ReconciliationDiffPageQuery query) {
        Page<ReconciliationDiffDO> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<ReconciliationDiffDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getBatchId())) {
            wrapper.eq(ReconciliationDiffDO::getBatchId, query.getBatchId());
        }
        Page<ReconciliationDiffDO> resultPage = baseMapper.selectPage(page, wrapper);
        List<ReconciliationDiffPageVO> records = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return new PageResult<>(records, resultPage.getSize(), resultPage.getTotal());
    }

    private ReconciliationDiffPageVO convertToVO(ReconciliationDiffDO diffDO) {
        ReconciliationDiffPageVO vo = new ReconciliationDiffPageVO();
        vo.setId(diffDO.getId());
        vo.setBatchId(diffDO.getBatchId());
        vo.setBusinessOrderNo(diffDO.getBusinessOrderNo());
        vo.setPayChannel(diffDO.getPayChannel());
        vo.setExpectedAmount(diffDO.getExpectedAmount());
        vo.setActualAmount(diffDO.getActualAmount());
        vo.setExpectedStatus(diffDO.getExpectedStatus());
        vo.setActualStatus(diffDO.getActualStatus());
        vo.setDiffAmount(diffDO.getDiffAmount());
        vo.setDiffType(diffDO.getDiffType());
        vo.setBusinessType(diffDO.getBusinessType());
        vo.setCreateTime(diffDO.getCreateTime());
        return vo;
    }
}
