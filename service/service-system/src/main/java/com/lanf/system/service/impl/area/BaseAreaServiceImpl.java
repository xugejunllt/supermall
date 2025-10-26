package com.lanf.system.service.impl.area;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.system.mapper.area.BaseAreaMapper;
import com.lanf.system.model.entiry.BaseAreaDO;
import com.lanf.system.model.vo.BaseAreaVO;
import com.lanf.system.service.area.IBaseAreaService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Service
public class BaseAreaServiceImpl extends ServiceImpl<BaseAreaMapper, BaseAreaDO> implements IBaseAreaService {


    @Override
    public List<BaseAreaVO> areaTree() {
        List<Integer> types = new ArrayList<>();
        types.add(1);
        types.add(2);
        types.add(3);
        List<BaseAreaDO> baseArea = this.lambdaQuery().in(BaseAreaDO::getType, types).list();
        List<BaseAreaVO> oneLevel = new ArrayList<>();
        List<BaseAreaVO> twoLevel = new ArrayList<>();

        for (BaseAreaDO area: baseArea){

            Integer areaType = area.getType();
            if (areaType != 1){
                continue;
            }
            BaseAreaVO areaVO = new BaseAreaVO();
            areaVO.setId(area.getId());
            areaVO.setParentId(area.getParentId());
            areaVO.setCityName(area.getCityName());
            areaVO.setType(area.getType());
            areaVO.setChildList(new ArrayList<>());
            oneLevel.add(areaVO);
        }
        Map<Long,BaseAreaVO> oneLevelMap = oneLevel.stream().collect(
                Collectors.toMap(BaseAreaVO::getId, Function.identity(), (key1, key2) -> key2));

        for (BaseAreaDO area: baseArea){

            Integer areaType = area.getType();
            if (areaType != 2){
                continue;
            }
            BaseAreaVO areaVO = new BaseAreaVO();
            areaVO.setId(area.getId());
            areaVO.setParentId(area.getParentId());
            areaVO.setCityName(area.getCityName());
            areaVO.setType(area.getType());
            areaVO.setChildList(new ArrayList<>());
            twoLevel.add(areaVO);
            //添加引用关系
            Long parentId = area.getParentId();
            BaseAreaVO areaVO1 = oneLevelMap.get(parentId);
            areaVO1.getChildList().add(areaVO);

        }
        Map<Long,BaseAreaVO> twoLevelMap = twoLevel.stream().collect(
                Collectors.toMap(BaseAreaVO::getId, Function.identity(), (key1, key2) -> key2));
        for (BaseAreaDO area: baseArea){

            Integer areaType = area.getType();
            if (areaType != 3){
                continue;
            }
            BaseAreaVO areaVO = new BaseAreaVO();
            areaVO.setId(area.getId());
            areaVO.setParentId(area.getParentId());
            areaVO.setCityName(area.getCityName());
            areaVO.setType(area.getType());
            areaVO.setChildList(new ArrayList<>());
            //添加引用关系
            Long parentId = area.getParentId();
            BaseAreaVO areaVO1 = twoLevelMap.get(parentId);
            areaVO1.getChildList().add(areaVO);

        }

        return oneLevel;
    }
}
