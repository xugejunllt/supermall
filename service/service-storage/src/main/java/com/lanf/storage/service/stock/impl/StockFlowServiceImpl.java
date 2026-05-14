package com.lanf.storage.service.stock.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.storage.mapper.StockFlowMapper;
import com.lanf.storage.model.entity.StockFlowDO;
import com.lanf.storage.model.query.StockFlowPageQuery;
import com.lanf.storage.model.vo.StockFlowPageVO;
import com.lanf.storage.service.stock.IStockFlowService;
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
    public PageResult<StockFlowPageVO> stockFlowPageQuery(StockFlowPageQuery query) {
        IPage<StockFlowDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<StockFlowDO> companyPage = this.lambdaQuery().
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);
        if (companyPage.getRecords().isEmpty()){
            return PageResult.emptyResult();
        }
        PageResult<StockFlowPageVO> result = new PageResult<>();
        result.setTotal(companyPage.getTotal());
        result.setSize(companyPage.getSize());
        result.setRecords(BeanCopyUtils.copyBeanList(companyPage.getRecords(), StockFlowPageVO.class));
        return result;

    }
}
