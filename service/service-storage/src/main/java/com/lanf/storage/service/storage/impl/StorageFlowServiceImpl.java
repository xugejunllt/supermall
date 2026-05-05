package com.lanf.storage.service.storage.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.mapper.StorageFlowMapper;
import com.lanf.storage.model.query.StorageFlowPageQuery;
import com.lanf.storage.service.storage.IStorageFlowService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 入库明细 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-07
 */
@Service
public class StorageFlowServiceImpl extends ServiceImpl<StorageFlowMapper, StorageFlowDO> implements IStorageFlowService {


    @Override
    public PageResult<StorageFlowDO> storageFlowPage(StorageFlowPageQuery query) {

        IPage<StorageFlowDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<StorageFlowDO> companyPage = this.lambdaQuery().
                eq(!ObjectUtils.isEmpty(query.getBizNumber()), StorageFlowDO::getBizNumber, query.getBizNumber()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        return PageResult.toPageResult(companyPage);
    }
}
