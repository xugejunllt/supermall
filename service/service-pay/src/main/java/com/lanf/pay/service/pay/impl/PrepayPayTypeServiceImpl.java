package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.pay.mapper.PrepayPayTypeMapper;
import com.lanf.pay.model.entity.PrepayPayTypeDO;
import com.lanf.pay.service.pay.IPrepayPayTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
            return false;
        }

        PrepayPayTypeDO prepayPayTypeDO = new PrepayPayTypeDO();
        prepayPayTypeDO.setOutTradeNo(outTradeNo);
        prepayPayTypeDO.setPayType(payType);

        try {
            this.save(prepayPayTypeDO);
            log.info("保存预支付类型成功:outTradeNo={},payType={}", outTradeNo, payType);
            return true;
        } catch (DuplicateKeyException e) {
            log.info("预支付类型记录已存在（唯一索引冲突）:outTradeNo={},payType={}", outTradeNo, payType);
            return false;
        }
    }

    @Override
    public List<Integer> getPayTypesByOutTradeNo(String outTradeNo) {
        List<PrepayPayTypeDO> payTypeList = this.lambdaQuery()
                .eq(PrepayPayTypeDO::getOutTradeNo, outTradeNo)
                .select(PrepayPayTypeDO::getPayType)
                .list();

        if (  payTypeList.isEmpty()) {
            log.debug("未查询到支付方式:outTradeNo={}", outTradeNo);
            return Collections.emptyList();
        }

        return payTypeList.stream()
                .map(PrepayPayTypeDO::getPayType)
                .collect(Collectors.toList());

    }

}
