package com.lanf.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.seckill.model.entity.SecKillItemDO;
import com.lanf.seckill.model.query.SecKillItemPageQuery;
import com.lanf.seckill.model.vo.SecKillItemPageVO;

/**
 * <p>
 * 秒杀商品表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
public interface ISecKillItemService extends IService<SecKillItemDO> {

    /**
     * 分页查询秒杀商品列表
     *
     * @param query 分页查询参数
     * @return 分页查询结果
     */
    PageResult<SecKillItemPageVO> seckillItemPageQuery(SecKillItemPageQuery query);

}
