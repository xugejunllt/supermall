package com.lanf.storage.service.supplier;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.api.storage.model.dto.AddSupplierDTO;
import com.lanf.storage.model.entity.SupplierDO;
import com.lanf.api.storage.model.query.SupplierPageQuery;
import com.lanf.api.storage.model.vo.SupplierListVO;
import com.lanf.api.storage.model.vo.SupplierPageVO;

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

    void  addSupplier(AddSupplierDTO supplier);

    PageResult<SupplierPageVO> supplierPageQuery(SupplierPageQuery query);

    List<SupplierListVO> supplierListQuery();

}
