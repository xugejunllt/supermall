package com.lanf.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.seckill.model.dto.AddSecKillCouponItemDTO;
import com.lanf.seckill.model.dto.GetSecKillCouponTokenDTO;
import com.lanf.seckill.model.dto.LauncherSecKillCouponItemDTO;
import com.lanf.seckill.model.entity.SecKillCouponItemDO;
import com.lanf.seckill.model.vo.SecKillCouponItemDetailVO;
import com.lanf.seckill.model.vo.SecKillCouponItemVO;
import com.lanf.seckill.model.vo.SecKillCouponTokenVO;
import com.lanf.seckill.model.query.SecKillCouponItemPageQuery;
import com.lanf.seckill.model.vo.SecKillCouponItemPageVO;

import java.util.List;

/**
 * <p>
 * 秒杀优惠券项目表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
public interface ISecKillCouponItemService extends IService<SecKillCouponItemDO> {

    /**
     * 添加秒杀优惠券
     *
     * @param dto
     */
    void addSecKillCouponItem(AddSecKillCouponItemDTO dto);

    /**
     * 上架秒杀优惠券
     *
     * @param dto
     */
    void launcherSecKillCouponItem(LauncherSecKillCouponItemDTO dto);

    /**
     * 分页查询秒杀优惠券列表
     *
     * @param activityId
     * @return
     */
    List<SecKillCouponItemVO> seckillCouponItemList();

    /**
     * 查询秒杀优惠券详情
     *
     * @param secKillCouponItemId
     * @return
     */
    SecKillCouponItemDetailVO seckillCouponItemDetailQuery(Long secKillCouponItemId);

    /**
     * 获取秒杀优惠券Token
     *
     * @param dto
     * @return
     */
    SecKillCouponTokenVO getSecKillCouponToken(GetSecKillCouponTokenDTO dto);

    /**
     * 分页查询秒杀优惠券项目列表
     *
     * @param query 分页查询参数
     * @return 分页查询结果
     */
    PageResult<SecKillCouponItemPageVO> seckillCouponItemPageQuery(SecKillCouponItemPageQuery query);

}
