package com.lanf.storage.service.stock.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.goods.api.GoodsApiService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.constant.web.PageResult;
import com.lanf.storage.mapper.StockMapper;
import com.lanf.storage.model.entity.StockDO;
import com.lanf.storage.model.query.StockPageQuery;
import com.lanf.storage.model.vo.StockPageQueryVO;
import com.lanf.storage.model.vo.StockVO;
import com.lanf.storage.service.stock.IStockService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-07
 */
@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, StockDO> implements IStockService {


    @Autowired
    private GoodsApiService goodsApiService;
    @Override
    public PageResult<StockPageQueryVO> stockPage(StockPageQuery query) {


        IPage<StockDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<StockDO> purchaseOrderPage = this.lambdaQuery().
                eq(!StringUtils.isEmpty(query.getSkuCode()), StockDO::getSkuCode, query.getSkuCode()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (purchaseOrderPage.getRecords().isEmpty()) {

            return PageResult.emptyResult(StockPageQueryVO.class);
        }


        return PageResult.toPageResult(page, StockPageQueryVO.class);
    }

    @Override
    public List<StockVO> querySkuCodeList(List<String> skuCodeList) {

        ThreadLocalUtils.addIgnoreTableName(true);
        List<StockDO> stockDOList = this.lambdaQuery().in(StockDO::getSkuCode, skuCodeList).list();
        if (stockDOList.isEmpty()){

            return new ArrayList<>();
        }


        return  BeanCopyUtils.copyBeanList(stockDOList,StockVO.class);
    }
}
