package com.lanf.storage.service.supplier;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.dto.SupplierAddDTO;
import com.lanf.storage.model.entity.SupplierDO;
import com.lanf.storage.model.query.SupplierPageQuery;

import java.util.List;

/**
 * <p>
 * 供应商 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
public interface ISupplierService extends IService<SupplierDO> {

    void  addSupplier(SupplierAddDTO supplier);

    PageResult<SupplierDO> supplierPage(SupplierPageQuery query);

    List<SupplierDO> supplierList();

}
