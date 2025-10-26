package com.lanf.pay.service.impl;

import com.lanf.pay.model.dto.BathPayDTO;
import com.lanf.pay.model.dto.TradeOrderDTO;
import com.lanf.pay.model.entity.BathPayOrderDO;
import com.lanf.pay.model.entity.PayOrderDO;
import com.lanf.pay.mapper.PayOrderMapper;
import com.lanf.pay.model.vo.TradeOrderVO;
import com.lanf.pay.service.IBathPayOrderService;
import com.lanf.pay.service.IPayOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.pay.service.PayFactory;
import com.lanf.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付订单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-14
 */
@Service
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrderDO> implements IPayOrderService {



}
