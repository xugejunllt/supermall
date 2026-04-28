package com.lanf.pay.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.entity.PrepayPayTypeDO;

import java.util.List;

public interface IPrepayPayTypeService extends IService<PrepayPayTypeDO> {

    boolean saveIfAbsent(String outTradeNo, Integer payType);

    List<Integer> getPayTypesByOutTradeNo(String outTradeNo);

}
