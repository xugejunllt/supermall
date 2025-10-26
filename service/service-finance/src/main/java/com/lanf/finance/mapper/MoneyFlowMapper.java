package com.lanf.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.finance.model.bo.AccountMoneySumBO;
import com.lanf.finance.model.entity.MoneyFlowDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 资金流水 Mapper 接口
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-22
 */
public interface MoneyFlowMapper extends BaseMapper<MoneyFlowDO> {



    @Update("UPDATE pay_account set remain_money = remain_money+1  WHERE account_type=0 AND account ='3871942195@qq.com' ")
    int updateRemainMoney(@Param("changeMoney") BigDecimal changeMoney,@Param("accountType")Integer accountType,
                          @Param("incomeAccount")String incomeAccount);


    double sumIncomeMoney(@Param("shopId") Long shopId,@Param("startTime")Date startTime,@Param("endTime")Date endTime
    ,@Param("income")Integer income,@Param("incomeAccount")String incomeAccount);

}
