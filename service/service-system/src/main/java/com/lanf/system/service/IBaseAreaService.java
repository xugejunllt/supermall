package com.lanf.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.system.model.entiry.BaseAreaDO;
import com.lanf.system.model.vo.BaseAreaVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
public interface IBaseAreaService extends IService<BaseAreaDO> {

    List<BaseAreaVO> areaTree();
}
