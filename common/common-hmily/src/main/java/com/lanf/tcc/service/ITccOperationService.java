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
    boolean confirmOperation(String bizKey);
    boolean cancelOperation(String bizKey);
    void addInterruptedFlag(String bizKey,String interruptedException);


    String getParameter(String bizKey);
}
