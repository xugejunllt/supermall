package com.lanf.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.bizcache.service.BizCacheService;
import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.finance.model.entity.LiquidationDO;
import com.lanf.finance.model.entity.LiquidationFlowDO;
import com.lanf.finance.mapper.LiquidationMapper;
import com.lanf.finance.model.entity.PayAccountDO;
import com.lanf.finance.model.entity.SettlementFlowDO;
import com.lanf.finance.service.ILiquidationFlowService;
import com.lanf.finance.service.ILiquidationService;
import com.lanf.finance.service.IPayAccountService;
import com.lanf.finance.service.ISettlementFlowService;
import com.lanf.pay.api.PayApiService;
import com.lanf.pay.model.dto.TransferAccountsDTO;
import com.lanf.pay.model.vo.TransferAccountsVO;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.LiquidationDTO;
import com.lanf.rocketmq.model.message.MoneyFlowDTO;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.system.api.SystemService;
import com.lanf.system.model.vo.ShopVO;
import com.lanf.web.exception.BizException;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 清算单
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-20
 */
@Slf4j

@Service
public class LiquidationServiceImpl extends ServiceImpl<LiquidationMapper, LiquidationDO> implements ILiquidationService {

    @Autowired
    private PayApiService payApiService;
    @Autowired
    private ILiquidationFlowService liquidationFlowService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private SystemService systemService;
    @Autowired
    private ISettlementFlowService settlementFlowService;
    @Autowired
    private IPayAccountService payAccountService;

    @Transactional
    @Override
    public void createLiquidation(LiquidationDTO dto) {

        Long orderId = dto.getOrderId();
        Integer source = dto.getSource();
        BigDecimal payMoney = dto.getPayMoney();
        LiquidationDO one = this.lambdaQuery().eq(LiquidationDO::getOrderId, orderId).eq(LiquidationDO::getSource,source).one();
        if (one != null) {
            throw new BizException("该支付订单已清算");
        }
        /**
         * 清分
         */
        LiquidationDO liquidationDO = new LiquidationDO();
        List<SettlementFlowDO> settlementFlowDOList = new ArrayList<>();
        List<LiquidationFlowDO> liquidationFlowDOList = new ArrayList<>();
        //用户下单
        Long liquidationId = IdUtils.generateId();

        liquidationDO.setId(liquidationId);
        liquidationDO.setOrderId(orderId);
        liquidationDO.setSource(source);
        liquidationDO.setPayMoney(payMoney);
        BigDecimal rate = BizCacheService.getByType(0).getRate();
        Long platformShopId = systemService.getPlatformShopId().getData();
        if (source.equals(0)) {
            /**
             * 用户下单
             */

            BigDecimal incomeMoney = payMoney;
            //平台收入
            LiquidationFlowDO liquidationFlowDO = init();
            liquidationFlowDO.setLiquidationId(liquidationId);
            liquidationFlowDO.setRate(rate);
            liquidationFlowDO.setIncome(0);
            liquidationFlowDO.setIncomeMoney(incomeMoney);
            liquidationFlowDO.setShopId(platformShopId);
            liquidationFlowDO.setAccountType(dto.getAccountType());
            liquidationFlowDO.setIncomeAccount(dto.getPlatformAccount());
            liquidationFlowDO.setPayFinishTime(dto.getPayFinishTime());
            liquidationFlowDOList.add(liquidationFlowDO);
            //平台支出
            BigDecimal incomeMoney2 = BigDecimalUtils.subtract(payMoney, dto.getReceiptMoney());
            LiquidationFlowDO liquidationFlowDO2 = init();
            liquidationFlowDO2.setLiquidationId(liquidationId);
            liquidationFlowDO2.setRate(rate);
            liquidationFlowDO2.setIncome(1);
            liquidationFlowDO2.setIncomeMoney(incomeMoney2);
            liquidationFlowDO2.setShopId(platformShopId);
            liquidationFlowDO2.setAccountType(dto.getAccountType());
            liquidationFlowDO2.setIncomeAccount(dto.getPlatformAccount());
            liquidationFlowDO2.setPayFinishTime(dto.getPayFinishTime());
            liquidationFlowDOList.add(liquidationFlowDO2);

        } else if (source.equals(1)) {

            /**
             * 订单履约完成
             * 转账给商户
             */
            log.info("订单履约完成,进行结算");
            //结算，转账给商家
            ShopVO shopVO = systemService.shopQuery(Arrays.asList(dto.getShopId())).getData().get(0);
            BigDecimal busRate = BigDecimalUtils.subtract(new BigDecimal(100), rate).
                    multiply(new BigDecimal(0.01));
            String incomeAccount = getBusAccount(dto, shopVO.getBusinessId());
            //平台支出 费率*支付金额
            BigDecimal incomeMoney = BigDecimalUtils.multiply(payMoney, busRate);
            TransferAccountsDTO transferAccountsDTO = new TransferAccountsDTO();
            transferAccountsDTO.setUserId(shopVO.getBusinessId());
            transferAccountsDTO.setShopId(dto.getShopId());
            transferAccountsDTO.setSource(0);
            transferAccountsDTO.setToUserType(0);
            transferAccountsDTO.setOutBizNo(orderId.toString());
            transferAccountsDTO.setToAccountType(dto.getAccountType());
            transferAccountsDTO.setIncomeAccount(incomeAccount);
            transferAccountsDTO.setPayType(dto.getAccountType());
            transferAccountsDTO.setTransAmount(incomeMoney);
            transferAccountsDTO.setCertNo("360726199711144356");
            transferAccountsDTO.setName("刘强");
            transferAccountsDTO.setOrderTitle("用户下单收入");
            Result<TransferAccountsVO> transferAccountsVOResult = payApiService.transferAccounts(transferAccountsDTO);
            if (!transferAccountsVOResult.getCode().equals(200)) {
                throw new BizException("转账异常");
            }
            TransferAccountsVO transferAccountsVO = transferAccountsVOResult.getData();
            if ( !transferAccountsVO.getStatus().equals(0)) {
                //保存收入为0 让订单对账有数据
                log.info("低于最低转账金额，无需转账，无需结算,收入支出金额为0");
                incomeMoney = new BigDecimal(0);
            }

            Date payFinishTime = transferAccountsVO.getPayFinishTime();
            LiquidationFlowDO liquidationFlowDO2 = init();
            liquidationFlowDO2.setLiquidationId(liquidationId);
            liquidationFlowDO2.setRate(rate);
            liquidationFlowDO2.setIncome(1);
            liquidationFlowDO2.setIncomeMoney(incomeMoney);
            liquidationFlowDO2.setShopId(platformShopId);
            liquidationFlowDO2.setAccountType(dto.getAccountType());
            liquidationFlowDO2.setIncomeAccount(dto.getPlatformAccount());
            liquidationFlowDO2.setPayFinishTime(payFinishTime);
            liquidationFlowDOList.add(liquidationFlowDO2);
            //商家收入
            LiquidationFlowDO liquidationFlowDO3 = init();;
            liquidationFlowDO3.setLiquidationId(liquidationId);
            liquidationFlowDO3.setRate(rate);
            liquidationFlowDO3.setIncome(0);
            liquidationFlowDO3.setIncomeMoney(incomeMoney);
            liquidationFlowDO3.setShopId(dto.getShopId());
            liquidationFlowDO3.setAccountType(dto.getAccountType());
            liquidationFlowDO3.setIncomeAccount(incomeAccount);
            liquidationFlowDO3.setPayFinishTime(payFinishTime);
            liquidationFlowDOList.add(liquidationFlowDO3);

        } else if (source.equals(2)){

            //用户退款 平台支出
            LiquidationFlowDO flowDO = init();
            flowDO.setLiquidationId(liquidationId);
            flowDO.setRate(rate);
            flowDO.setIncome(1);
            flowDO.setIncomeMoney(dto.getReceiptMoney());
            flowDO.setShopId(platformShopId);
            flowDO.setAccountType(dto.getAccountType());
            flowDO.setIncomeAccount(dto.getPlatformAccount());
            flowDO.setPayFinishTime(dto.getPayFinishTime());
            liquidationFlowDOList.add(flowDO);
        }

        /**
         * 生成结算单
         */

        liquidationFlowDOList.forEach(a -> {

            SettlementFlowDO settlementFlowDO = new SettlementFlowDO();
            settlementFlowDO.setLiquidationFlowId(a.getId());
            settlementFlowDOList.add(settlementFlowDO);

        });
        this.save(liquidationDO);
        liquidationFlowService.saveBatch(liquidationFlowDOList);
        settlementFlowService.saveBatch(settlementFlowDOList);
        /**
         * 资金结算
         */
        MoneyFlowDTO moneyFlowDTO = new MoneyFlowDTO();
        moneyFlowDTO.setOrderId(orderId);
        moneyFlowDTO.setSource(dto.getSource());
        rocketMqClient.sendMessage(TopicName.MONEY_FLOW_TOPIC, moneyFlowDTO);
    }
    private LiquidationFlowDO init(){
        LiquidationFlowDO liquidationFlowDO = new LiquidationFlowDO();
        liquidationFlowDO.setId(IdUtils.generateId());
        return  liquidationFlowDO;
    }
    private String getBusAccount(LiquidationDTO dto, Long businessId) {
        PayAccountDO payAccountDO = payAccountService.lambdaQuery().eq(PayAccountDO::getAccountType, dto.getAccountType()).
                eq(PayAccountDO::getBusinessId, businessId).one();

        return payAccountDO.getAccount();

    }

}
