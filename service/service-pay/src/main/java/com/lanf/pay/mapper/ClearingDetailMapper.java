package com.lanf.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.pay.model.entity.ClearingDetailDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 平台清算流水 Mapper 接口
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-20
 */
public interface ClearingDetailMapper extends BaseMapper<ClearingDetailDO> {

    /**
     * 根据创建时间区间统计收入金额
     */
    @Select("SELECT COALESCE(SUM(income_money), 0) FROM clearing_detail WHERE create_time \u003e= #{startTime} AND create_time \u003c= #{endTime}")
    BigDecimal sumIncomeMoney(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
