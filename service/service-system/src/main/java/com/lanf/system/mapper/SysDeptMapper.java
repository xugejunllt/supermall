package com.lanf.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.system.model.entiry.SysDeptDO;
import com.lanf.system.model.vo.SysDeptQueryVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDeptDO> {
    @MapKey("id")
    public List<Map> findList(@Param("vo") SysDeptQueryVO sysDeptQueryVo);
    public List<SysDeptDO> queryList(@Param("vo") SysDeptQueryVO sysDeptQueryVo);
}
