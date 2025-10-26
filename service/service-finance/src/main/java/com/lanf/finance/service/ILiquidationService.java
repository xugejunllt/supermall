package com.lanf.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.finance.model.entity.LiquidationDO;
import com.lanf.rocketmq.model.message.LiquidationDTO;

/**
 * <p>
 * 清算单
 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-20
 */
public interface ILiquidationService extends IService<LiquidationDO> {


    void createLiquidation(LiquidationDTO dto);

}
