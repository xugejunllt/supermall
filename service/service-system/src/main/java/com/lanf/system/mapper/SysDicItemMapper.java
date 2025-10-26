package com.lanf.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.system.model.entiry.SysDicItemDO;
import com.lanf.system.model.vo.SysDicItemQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author tanlingfei
* @version 1.0
* @description 字典选项 Mapper层
* @date 2020-04-13 16:12:32
*/
@Repository
@Mapper
public interface SysDicItemMapper extends BaseMapper<SysDicItemDO> {
    IPage<SysDicItemDO> selectPage(Page<SysDicItemDO> page, @Param("vo") SysDicItemQueryVO sysDicItemQueryVo);
    List<SysDicItemDO> queryList(@Param("vo") SysDicItemQueryVO sysDicItemQueryVo);
}