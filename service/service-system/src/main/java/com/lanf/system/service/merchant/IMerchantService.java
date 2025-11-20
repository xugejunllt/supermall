package com.lanf.system.service.merchant;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.system.model.dto.MerchantRegisterDTO;
import com.lanf.system.model.entiry.MerchantDO;
import com.lanf.system.model.query.CompanyPageQuery;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author
 * @since 2024-05-28
 */
public interface IMerchantService extends IService<MerchantDO> {

    /**
     * 商家注册
     * @param
     */
    void registerMerchant(MerchantRegisterDTO companyRegisterDTO);
    void auditing(Long id,Integer status);

    PageResult<MerchantDO>  companyPage(CompanyPageQuery query);

}
