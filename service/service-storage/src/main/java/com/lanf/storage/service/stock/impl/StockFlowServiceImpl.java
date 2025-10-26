package com.lanf.storage.service.stock.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.security.utils.UserUtil;
import com.lanf.storage.mapper.StockFlowMapper;
import com.lanf.storage.model.entity.StockFlowDO;
import com.lanf.storage.model.query.StockFlowPageQuery;
import com.lanf.storage.service.stock.IStockFlowService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 库存流水 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Service
public class StockFlowServiceImpl extends ServiceImpl<StockFlowMapper, StockFlowDO> implements IStockFlowService {

    @Override
    public PageResult<StockFlowDO> stockFlowPage(StockFlowPageQuery query) {
        IPage<StockFlowDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<StockFlowDO> companyPage = this.lambdaQuery().
                eq(StockFlowDO::getShopId, UserUtil.getShopId()).
                eq(!ObjectUtils.isEmpty(query.getBizNumber()), StockFlowDO::getBizNumber, query.getBizNumber()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        return PageResult.toPageResult(companyPage);

    }
}
