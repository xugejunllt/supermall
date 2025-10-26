package com.lanf.system.mapper.company;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.system.model.entiry.CompanyDO;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-28
 */
public interface CompanyMapper extends BaseMapper<CompanyDO> {

    @InterceptorIgnore(tenantLine = "true")
    CompanyDO selectById(Long id);
}
