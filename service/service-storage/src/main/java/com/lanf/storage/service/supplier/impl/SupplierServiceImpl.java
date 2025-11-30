package com.lanf.storage.service.supplier.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.mapper.SupplierMapper;
import com.lanf.storage.model.dto.SupplierAddDTO;
import com.lanf.storage.model.entity.SupplierDO;
import com.lanf.storage.model.query.SupplierPageQuery;
import com.lanf.storage.service.supplier.ISupplierService;
import com.lanf.constant.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 供应商 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, SupplierDO> implements ISupplierService {

    @Override
    public void addSupplier(SupplierAddDTO supplier) {


        SupplierDO supplierDO = this.lambdaQuery().eq(SupplierDO::getName, supplier.getName()).one();
        if (supplierDO != null) {
            throw new BizException("供应商名称重复");
        }
        SupplierDO saveSupplier = new SupplierDO();
        BeanCopyUtils.copy(supplier, saveSupplier);
        saveSupplier.setCode(CodeGenerateUtils.generaCode());
        this.save(saveSupplier);

    }

    @Override
    public PageResult<SupplierDO> supplierPage(SupplierPageQuery query) {

        IPage<SupplierDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<SupplierDO> companyPage = this.lambdaQuery().
                eq(!StringUtils.isEmpty(query.getName()), SupplierDO::getCode, query.getName()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        return PageResult.toPageResult(companyPage);

    }

    @Override
    public List<SupplierDO> supplierList() {


        return this.list();
    }

}
