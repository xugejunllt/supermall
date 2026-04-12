package com.lanf.pay.service.trade.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.pay.mapper.PrepayPayTypeMapper;
import com.lanf.pay.model.entity.PrepayPayTypeDO;
import com.lanf.pay.service.trade.IPrepayPayTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PrepayPayTypeServiceImpl extends ServiceImpl<PrepayPayTypeMapper, PrepayPayTypeDO> implements IPrepayPayTypeService {

    @Override
    public boolean saveIfAbsent(String outTradeNo, Integer payType) {
        PrepayPayTypeDO existingRecord = this.lambdaQuery()
                .eq(PrepayPayTypeDO::getOutTradeNo, outTradeNo)
                .eq(PrepayPayTypeDO::getPayType, payType)
                .one();

        if (existingRecord != null) {
            log.debug("预支付类型记录已存在:outTradeNo={},payType={}", outTradeNo, payType);
            return  true;
        }

        PrepayPayTypeDO prepayPayTypeDO = new PrepayPayTypeDO();
        prepayPayTypeDO.setOutTradeNo(outTradeNo);
        prepayPayTypeDO.setPayType(payType);

        try {
            this.save(prepayPayTypeDO);

            return false;

        } catch (DuplicateKeyException e) {

            log.info("预支付类型记录已存在（唯一索引冲突）:outTradeNo={},payType={}", outTradeNo, payType);
            return true;
        }

    }

}
