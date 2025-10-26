package com.lanf.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.finance.model.entity.ContrastBillDO;
import com.lanf.finance.model.entity.ContrastBillTrackDO;
import com.lanf.finance.model.query.ContrastBillPageQuery;
import com.lanf.finance.model.vo.ContrastBillTrackVO;
import com.lanf.finance.model.vo.ContrastBillVO;
import com.lanf.mybatis.base.PageResult;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-09-01
 */
public interface IContrastBillService extends IService<ContrastBillDO> {


    void commitContrastBillTask(Long orderId);

    void  startContrastBillTask(Long orderId);

    PageResult<ContrastBillDO> contrastBillPage(ContrastBillPageQuery query);

    List<ContrastBillTrackVO> contrastBillDetail(Long id);

}
