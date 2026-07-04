package com.lanf.storage.service.reconciliation.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.storage.model.query.ReconciliationDiffPageQuery;
import com.lanf.api.storage.model.vo.ReconciliationDiffPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.storage.mapper.ReconciliationDiffMapper;
import com.lanf.storage.model.entity.ReconciliationDiffDO;
import com.lanf.storage.service.reconciliation.IReconciliationDiffService;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-06
 */
@Service
public class ReconciliationDiffServiceImpl extends ServiceImpl<ReconciliationDiffMapper, ReconciliationDiffDO> implements IReconciliationDiffService {

    @Override
    public PageResult<ReconciliationDiffPageVO> reconciliationDiffPageQuery(ReconciliationDiffPageQuery query) {
        IPage<ReconciliationDiffDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<ReconciliationDiffDO> doPage = this.lambdaQuery()
                .eq(StringUtils.isNotBlank(query.getBathId()), ReconciliationDiffDO::getBathId, query.getBathId())
                .eq(query.getOrderId() != null, ReconciliationDiffDO::getOrderId, query.getOrderId())
                .eq(StringUtils.isNotBlank(query.getSkuCode()), ReconciliationDiffDO::getSkuCode, query.getSkuCode())
                .eq(query.getWarehouseId() != null, ReconciliationDiffDO::getWarehouseId, query.getWarehouseId())
                .eq(query.getStockFlowId() != null, ReconciliationDiffDO::getStockFlowId, query.getStockFlowId())
                .eq(query.getJobType() != null, ReconciliationDiffDO::getJobType, query.getJobType())
                .eq(query.getDiffType() != null, ReconciliationDiffDO::getDiffType, query.getDiffType())
                .orderByDesc(ReconciliationDiffDO::getCreateTime)
                .page(page);

        if (doPage.getRecords().isEmpty()) {
            return PageResult.emptyResult();
        }

        PageResult<ReconciliationDiffPageVO> result = new PageResult<>();
        result.setTotal(doPage.getTotal());
        result.setRecords(doPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        result.setSize(doPage.getSize());

        return result;
    }

    private ReconciliationDiffPageVO convertToVO(ReconciliationDiffDO d) {
        ReconciliationDiffPageVO vo = new ReconciliationDiffPageVO();
        vo.setId(d.getId());
        vo.setBathId(d.getBathId());
        vo.setOrderId(d.getOrderId());
        vo.setSkuCode(d.getSkuCode());
        vo.setWarehouseId(d.getWarehouseId());
        vo.setStockFlowId(d.getStockFlowId());
        if (d.getJobType() != null) {
            vo.setJobType(com.lanf.api.storage.model.enums.ReconciliationJobTypeEnum.valueOf(d.getJobType().name()));
        }
        if (d.getDiffType() != null) {
            vo.setDiffType(com.lanf.api.storage.model.enums.ReconciliationDiffTypeEnum.valueOf(d.getDiffType().name()));
        }
        vo.setCreateTime(d.getCreateTime());
        vo.setUpdateTime(d.getUpdateTime());
        return vo;
    }

}
