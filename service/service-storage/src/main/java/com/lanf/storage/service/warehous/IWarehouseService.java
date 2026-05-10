package com.lanf.storage.service.warehous;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.web.PageResult;
import com.lanf.storage.model.dto.WarehouseAddDTO;
import com.lanf.storage.model.entity.WarehouseDO;
import com.lanf.storage.model.query.WarehousePageQuery;

/**
 * <p>
 * 仓库 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
public interface IWarehouseService extends IService<WarehouseDO> {

    void  addWarehouse(WarehouseAddDTO warehouse);

    PageResult<WarehouseDO>  warehousePage(WarehousePageQuery query);

}
