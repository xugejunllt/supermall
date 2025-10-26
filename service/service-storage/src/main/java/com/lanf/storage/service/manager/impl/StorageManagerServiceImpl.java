package com.lanf.storage.service.manager.impl;

import com.lanf.storage.model.entity.WarehouseDO;
import com.lanf.storage.service.manager.StorageManagerService;
import com.lanf.storage.service.warehous.IWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class StorageManagerServiceImpl implements StorageManagerService {


    @Autowired
    private IWarehouseService warehouseService;

    @Override
    public WarehouseDO getDefaultWarehouse() {


        return warehouseService.lambdaQuery().one();
    }
}
