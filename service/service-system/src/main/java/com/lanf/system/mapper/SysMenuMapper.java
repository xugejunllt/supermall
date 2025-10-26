package com.lanf.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.system.model.entiry.SysMenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuDO> {

    List<SysMenuDO> findListByUserId(@Param("userId")Long userId, @Param("type")Integer type, @Param("typeList")List<Integer> typeList);

    List<SysMenuDO> queryList(@Param("type") String type, @Param("notId") String notId);
}
