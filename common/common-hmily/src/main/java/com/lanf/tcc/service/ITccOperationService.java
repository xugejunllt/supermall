package com.lanf.tcc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.tcc.model.TccOperationDO;

/**
 * <p>
 * TCC操作记录服务类，用于实现TCC分布式事务各阶段的幂等性校验和状态追踪。
 * </p>
 *
 * @author jarven
 * @since 2026-01-03
 */
public interface ITccOperationService extends IService<TccOperationDO> {

    /**
     * 记录TCC Try阶段操作，用于幂等性校验。
     *
     * @param bizKey    业务唯一键，标识一次TCC操作
     * @param parameter 业务参数，通常为JSON字符串
     */
    boolean tryOperation(String bizKey, String parameter);

    /**
     * 记录TCC Try阶段操作，用于幂等性校验（支持分库分表）。
     *
     * @param bizKey        业务唯一键，标识一次TCC操作
     * @param parameter     业务参数，通常为JSON字符串
     * @param shardingValue 分片值，用于分库分表场景
     */
    boolean tryOperation(String bizKey, String parameter, String shardingValue);

    /**
     * 确认TCC Try阶段操作，标记为已Confirm。
     *
     * @param bizKey 业务唯一键，标识一次TCC操作
     * @return true 确认成功；false 已确认过或不存在
     */
    boolean confirmOperation(String bizKey);

    /**
     * 确认TCC Try阶段操作，标记为已Confirm（支持分库分表）。
     *
     * @param bizKey        业务唯一键，标识一次TCC操作
     * @param shardingValue 分片值，用于分库分表场景
     * @return true 确认成功；false 已确认过或不存在
     */
    boolean confirmOperation(String bizKey, String shardingValue);

    /**
     * 取消TCC Try阶段操作，标记为已Cancel。
     *
     * @param bizKey 业务唯一键，标识一次TCC操作
     * @return true 取消成功；false 已取消过或不存在
     */
    boolean cancelOperation(String bizKey);

    /**
     * 取消TCC Try阶段操作，标记为已Cancel（支持分库分表）。
     *
     * @param bizKey        业务唯一键，标识一次TCC操作
     * @param shardingValue 分片值，用于分库分表场景
     * @return true 取消成功；false 已取消过或不存在
     */
    boolean cancelOperation(String bizKey, String shardingValue);



    /**
     * 添加中断标记，记录TCC操作执行过程中的异常信息（支持分库分表）。
     *
     * @param bizKey               业务唯一键，标识一次TCC操作
     * @param interruptedException 中断异常信息描述
     * @param shardingValue        分片值，用于分库分表场景
     */
    void addInterruptedFlag(String bizKey, String interruptedException, String shardingValue);

    /**
     * 获取TCC Try阶段记录的业务参数。
     *
     * @param bizKey 业务唯一键，标识一次TCC操作
     * @return 业务参数字符串，若不存在则返回null
     */
    String getParameter(String bizKey);

    /**
     * 获取TCC Try阶段记录的业务参数（支持分库分表）。
     *
     * @param bizKey        业务唯一键，标识一次TCC操作
     * @param shardingValue 分片值，用于分库分表场景
     * @return 业务参数字符串，若不存在则返回null
     */
    String getParameter(String bizKey, String shardingValue);

}
