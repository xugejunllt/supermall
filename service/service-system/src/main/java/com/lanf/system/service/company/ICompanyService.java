package com.lanf.system.service.company;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.system.model.dto.CompanyRegisterDTO;
import com.lanf.system.model.entiry.CompanyDO;
import com.lanf.system.model.query.CompanyPageQuery;
import com.lanf.system.model.vo.CompanyRegisterVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-28
 */
public interface ICompanyService extends IService<CompanyDO> {

    /**
     * g公司注册
     * @param companyRegisterDTO
     */
    CompanyRegisterVO companyRegister(CompanyRegisterDTO companyRegisterDTO);
    void auditing(Long id,Integer status);

    PageResult<CompanyDO>  companyPage(CompanyPageQuery query);

}
