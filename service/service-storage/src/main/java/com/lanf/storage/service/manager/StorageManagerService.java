package com.lanf.storage.service.manager;

import com.lanf.storage.model.entity.WarehouseDO;

public interface StorageManagerService {

    /**
     * 获取租户默认的仓库
     *
     */
    WarehouseDO getDefaultWarehouse();

}
