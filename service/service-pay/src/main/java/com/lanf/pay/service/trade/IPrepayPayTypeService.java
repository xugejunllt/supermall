package com.lanf.pay.service.trade;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.entity.PrepayPayTypeDO;

import java.util.List;

public interface IPrepayPayTypeService extends IService<PrepayPayTypeDO> {

    void checkAndSavePrepayPayType(String outTradeNo, Integer payType);

    List<String> getPrepayPayTypesByOutTradeNo(String outTradeNo);

}
