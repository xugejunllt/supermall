package com.lanf.seckill.service.strategy;

import com.lanf.seckill.model.dto.PlaceDTO;

public interface SecKillStrategy {

    /**
     * 执行秒杀
     *
     * @param dto 秒杀请求参数
     * @return 秒杀结果
     */
     void executeSecKill(PlaceDTO dto);

    /**
     * 获取支持的秒杀模式
     *
     * @return 秒杀模式编码
     */
    Integer getSupportedMode();
}
