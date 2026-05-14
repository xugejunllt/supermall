package com.lanf.storage.service.warehous;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.storage.model.dto.AddWarehouseDTO;
import com.lanf.storage.model.entity.WarehouseDO;
import com.lanf.storage.model.query.WarehousePageQuery;
import com.lanf.storage.model.vo.WarehousePageVO;

/**
 * <p>
 * 仓库 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
public interface IWarehouseService extends IService<WarehouseDO> {

    void  addWarehouse(AddWarehouseDTO warehouse);

    PageResult<WarehousePageVO> warehousePageQuery(WarehousePageQuery query);

}
