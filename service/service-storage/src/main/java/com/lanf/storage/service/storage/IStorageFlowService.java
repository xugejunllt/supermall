package com.lanf.storage.service.storage;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.entity.StorageFlowDO;
import com.lanf.storage.model.query.StorageFlowPageQuery;

/**
 * <p>
 * 入库明细 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-07
 */
public interface IStorageFlowService extends IService<StorageFlowDO> {

    PageResult<StorageFlowDO> storageFlowPage(StorageFlowPageQuery query);


}
