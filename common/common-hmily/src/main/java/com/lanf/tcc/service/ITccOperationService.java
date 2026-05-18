package com.lanf.tcc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.tcc.model.TccOperationDO;

/**
 * <p>
 * tcc操作记录 用于去重 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-01-03
 */
public interface ITccOperationService extends IService<TccOperationDO> {


    void tryOperation(String bizKey,String  parameter);

    void tryOperation(String bizKey,String  parameter,String shardingValue);

    boolean confirmOperation(String bizKey);

    boolean confirmOperation(String bizKey,String shardingValue);

    boolean cancelOperation(String bizKey);

    boolean cancelOperation(String bizKey,String shardingValue);

    void addInterruptedFlag(String bizKey,String interruptedException);

    void addInterruptedFlag(String bizKey,String interruptedException,String shardingValue);

    String getParameter(String bizKey);

    String getParameter(String bizKey,String shardingValue);

}
