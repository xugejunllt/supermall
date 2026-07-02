package com.lanf.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.seckill.model.entity.SecKillRecordDO;
import com.lanf.seckill.model.query.SecKillRecordPageQuery;
import com.lanf.seckill.model.vo.SecKillRecordPageVO;

/**
 * <p>
 * 秒杀记录表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-05-09
 */
public interface ISecKillRecordService extends IService<SecKillRecordDO> {

    /**
     * 分页查询秒杀记录列表
     *
     * @param query 分页查询参数
     * @return 分页查询结果
     */
    PageResult<SecKillRecordPageVO> seckillRecordPageQuery(SecKillRecordPageQuery query);

}
