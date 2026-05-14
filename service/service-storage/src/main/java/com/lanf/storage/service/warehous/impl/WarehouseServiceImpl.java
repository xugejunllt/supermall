package com.lanf.storage.service.warehous.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.storage.mapper.WarehouseMapper;
import com.lanf.storage.model.dto.AddWarehouseDTO;
import com.lanf.storage.model.entity.WarehouseDO;
import com.lanf.storage.model.query.WarehousePageQuery;
import com.lanf.storage.model.vo.WarehousePageVO;
import com.lanf.storage.service.warehous.IWarehouseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 仓库 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, WarehouseDO> implements IWarehouseService {

    @Override
    public void addWarehouse(AddWarehouseDTO warehouse) {

        WarehouseDO one = this.lambdaQuery().eq(WarehouseDO::getName, warehouse.getName()).one();
        if (one != null) {
            throw new BizException("仓库名称重复");
        }

        WarehouseDO warehouse1 = new WarehouseDO();
        BeanCopyUtils.copy(warehouse, warehouse1);
        warehouse1.setStatus(1);
        warehouse1.setCode(CodeGenerateUtils.generaCode());
        this.save(warehouse1);

    }

    @Override
    public PageResult<WarehousePageVO> warehousePageQuery(WarehousePageQuery query) {

        IPage<WarehouseDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<WarehouseDO> companyPage = this.lambdaQuery().
                eq(!StringUtils.isEmpty(query.getCode()), WarehouseDO::getCode, query.getCode()).
                like(!StringUtils.isEmpty(query.getName()), WarehouseDO::getName, query.getName()).page(page);
        if (companyPage.getRecords().isEmpty()){

            return PageResult.emptyResult();
        }
        PageResult<WarehousePageVO> result = new PageResult<>();
        result.setTotal(companyPage.getTotal());
        result.setRecords(BeanCopyUtils.copyBeanList(companyPage.getRecords(), WarehousePageVO.class));
        result.setSize(companyPage.getSize());

        return result;
    }
}
