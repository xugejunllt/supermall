package com.lanf.system.service.impl.company;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.system.mapper.company.ShopMapper;
import com.lanf.system.model.entiry.CompanyDO;
import com.lanf.system.model.entiry.ShopDO;
import com.lanf.system.model.query.ShopPageQuery;
import com.lanf.system.model.vo.ShopVO;
import com.lanf.system.service.company.ICompanyService;
import com.lanf.system.service.company.IShopService;
import com.lanf.system.service.manager.SystemManagerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-28
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, ShopDO> implements IShopService {

    @Autowired
    private SystemManagerService systemManagerService;
    @Lazy
    @Autowired
    private ICompanyService companyService;

    @Override
    public PageResult<ShopDO> shopPage(ShopPageQuery query) {

        systemManagerService.ignoreTableName();
        IPage<ShopDO> page = this.lambdaQuery().
                like(!StringUtils.isEmpty(query.getName()), ShopDO::getName, query.getName()).
                page(PageResult.buildIPage(query, ShopDO.class));

        return PageResult.toPageResult(page);
    }

    @Override
    public List<ShopVO> shopQuery(List<Long> idList) {

        List<ShopDO> shopDOList = this.lambdaQuery().in(BaseEntity::getId, idList).list();
        if (shopDOList.isEmpty()){

            return new ArrayList<>();
        }
        return BeanCopyUtils.copyBeanList(shopDOList,ShopVO.class);
    }

    @Override
    public Long getPlatformShopId() {

        ThreadLocalUtils.addIgnoreTableName(true);
        CompanyDO companyDO = companyService.lambdaQuery().eq(CompanyDO::getTenantCode, Constants.ADMIN_TENANT_CODE).one();

        ShopDO one = this.lambdaQuery().eq(ShopDO::getBusinessId, companyDO.getId()).one();

        return one.getId();
    }

    @Override
    public String getTenantCodeByShopId(Long shopId) {

        ShopDO shopDO = this.getById(shopId);
        Long businessId = shopDO.getBusinessId();
        ThreadLocalUtils.addIgnoreTableName(true);
        CompanyDO companyDO = companyService.getById(businessId);

        return companyDO.getTenantCode();
    }


}
