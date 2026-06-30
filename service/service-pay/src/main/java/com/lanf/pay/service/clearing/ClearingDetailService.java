package com.lanf.pay.service.clearing;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.model.entity.ClearingDetailDO;
import com.lanf.pay.model.query.ClearingDetailPageQuery;
import com.lanf.pay.model.vo.ClearingDetailPageVO;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 平台清算流水 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-20
 */
public interface ClearingDetailService extends IService<ClearingDetailDO> {

    /**
     * 分页查询清算明细
     */
    PageResult<ClearingDetailPageVO> clearingDetailPageQuery(ClearingDetailPageQuery query);

    /**
     * 根据创建时间区间统计收入金额
     */
    BigDecimal sumIncomeMoney(Date startTime, Date endTime);
}
