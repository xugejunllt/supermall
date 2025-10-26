package com.lanf.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.model.vo.SysUserQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserDO> {
    IPage<SysUserDO> selectPage(Page<SysUserDO> page, @Param("vo") SysUserQueryVO userQueryVo);

    @InterceptorIgnore(tenantLine = "true")
    SysUserDO getByUserName(@Param("userName") String userName,@Param("tenantCode")String tenantCode);
}

