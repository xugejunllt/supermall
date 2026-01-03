package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.goods.model.entity.TccOperationDO;

/**
 * <p>
 * tcc操作记录 用于去重 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-01-03
 */
public interface ITccOperationService extends IService<TccOperationDO> {


    void tryOperation(String bizKey);
    boolean confirmOperation(String bizKey);
    boolean cancelOperation(String bizKey);
}
