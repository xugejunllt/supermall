package com.lanf.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.DateUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.finance.mapper.MoneyFlowMapper;
import com.lanf.finance.model.entity.*;
import com.lanf.finance.model.enums.IncomeSubjectEnum;
import com.lanf.finance.model.query.AccountMoneySumQuery;
import com.lanf.finance.model.query.MoneyFlowPageQuery;
import com.lanf.finance.model.vo.AccountMoneySumVO;
import com.lanf.finance.service.*;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.rocketmq.model.message.MoneyFlowDTO;
import com.lanf.security.utils.UserUtils;
import com.lanf.constant.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 资金流水 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-22
 */
@Service
public class MoneyFlowServiceImpl extends ServiceImpl<MoneyFlowMapper, MoneyFlowDO> implements IMoneyFlowService {

    @Autowired
    private ISettlementFlowService settlementFlowService;
    @Autowired
    private IPayAccountService payAccountService;


    @Autowired
    private MoneyFlowMapper moneyFlowMapper;


    @Autowired
    private ILiquidationService liquidationService;
    @Autowired
    private ILiquidationFlowService liquidationFlowService;


    /**
     * 这个方法批量执行 分布式锁串行执行 锁商家id
     */
    @Transactional
    @Override
    public void moneyFlowAdd(MoneyFlowDTO dto) {


        Long orderId = dto.getOrderId();

        LiquidationDO one = liquidationService.lambdaQuery().eq(LiquidationDO::getOrderId, orderId).eq(LiquidationDO::
                getSource, dto.getSource()).one();

        List<LiquidationFlowDO> liquidationFlowDOList = liquidationFlowService.lambdaQuery().eq(LiquidationFlowDO::getLiquidationId, one.getId()).list();

        List<Long> liquidationFlowIdList = liquidationFlowDOList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<SettlementFlowDO> settlementFlowDOList = settlementFlowService.lambdaQuery().in(SettlementFlowDO::getLiquidationFlowId, liquidationFlowIdList).list();

        List<MoneyFlowDO> moneyFlowDOList1 = this.lambdaQuery().eq(MoneyFlowDO::getSource, one.getSource()).eq(MoneyFlowDO::getOrderId, orderId).list();
        if (!moneyFlowDOList1.isEmpty()) {
            throw new BizException("结算单已进行资金结算");
        }
        Map<Long, SettlementFlowDO> settlementFlowMap = settlementFlowDOList.stream()
                .collect(Collectors.toMap(SettlementFlowDO::getLiquidationFlowId, Function.identity()));
        LiquidationFlowDO liquidationFlowDO = liquidationFlowDOList.get(0);
        Integer accountType = liquidationFlowDO.getAccountType();
        List<String> incomeAccountList = liquidationFlowDOList.stream().map(LiquidationFlowDO::getIncomeAccount).collect(Collectors.toList());
        List<PayAccountDO> accountDOList = payAccountService.lambdaQuery().
                eq(PayAccountDO::getAccountType, accountType).
                in(PayAccountDO::getAccount, incomeAccountList).list();
        Map<String, PayAccountDO> accountDOMap = accountDOList.stream()
                .collect(Collectors.toMap(PayAccountDO::getAccount, Function.identity()));


        Integer source = one.getSource();
        //构建MoneyFlowDO
        List<MoneyFlowDO> moneyFlowDOList = new ArrayList<>(liquidationFlowDOList.size());
        for (LiquidationFlowDO flowDO : liquidationFlowDOList) {

            PayAccountDO payAccountDO = accountDOMap.get(flowDO.getIncomeAccount());
            BigDecimal incomeMoney = flowDO.getIncomeMoney();
            BigDecimal afterRemainMoney = null;
            if (flowDO.getIncome().equals(0)) {
                //收入
                afterRemainMoney = BigDecimalUtil.add(incomeMoney, payAccountDO.getRemainMoney());
            } else {
                //支出
                afterRemainMoney = BigDecimalUtil.subtract(payAccountDO.getRemainMoney(), incomeMoney);
            }

            String incomeSubjectName = null;
            if (source.equals(0) && flowDO.getIncome().equals(0)) {
                incomeSubjectName = IncomeSubjectEnum.CODE0.getName();
            }
            if (source.equals(0) && flowDO.getIncome().equals(1)) {
                incomeSubjectName = IncomeSubjectEnum.CODE1.getName();
            }
            if (source.equals(1) && flowDO.getIncome().equals(0)) {
                incomeSubjectName = IncomeSubjectEnum.CODE4.getName();
            }
            if (source.equals(1) && flowDO.getIncome().equals(1)) {
                incomeSubjectName = IncomeSubjectEnum.CODE3.getName();
            }
            if (source.equals(2) && flowDO.getIncome().equals(1)) {
                incomeSubjectName = IncomeSubjectEnum.CODE5.getName();
            }
            SettlementFlowDO settlementFlowDO = settlementFlowMap.get(flowDO.getId());
            //构建MoneyFlowDO
            MoneyFlowDO flowDO1 = new MoneyFlowDO();
            flowDO1.setSettlementFlowId(settlementFlowDO.getId());
            flowDO1.setOrderId(orderId);
            flowDO1.setShopId(flowDO.getShopId());
            flowDO1.setIncomeSubjectName(incomeSubjectName);
            flowDO1.setIncome(flowDO.getIncome());
            flowDO1.setSource(source);
            flowDO1.setIncomeMoney(incomeMoney);
            flowDO1.setAccountType(flowDO.getAccountType());
            flowDO1.setIncomeAccount(flowDO.getIncomeAccount());
            flowDO1.setBeforeRemainMoney(payAccountDO.getRemainMoney());
            flowDO1.setAfterRemainMoney(afterRemainMoney);
            flowDO1.setTradeFinishTime(flowDO.getPayFinishTime());
            String tradeFinishTimeFormat = DateUtils.format(flowDO.getPayFinishTime(), DateUtils.DATE);
            flowDO1.setTradeFinishTimeFormat(tradeFinishTimeFormat);
            moneyFlowDOList.add(flowDO1);

        }
        this.saveBatch(moneyFlowDOList);
        moneyFlowDOList.forEach(a -> {

            BigDecimal changeMoney = BigDecimalUtil.subtract(a.getAfterRemainMoney(), a.getBeforeRemainMoney());
            int updated = moneyFlowMapper.updateRemainMoney(changeMoney, a.getAccountType(), a.getIncomeAccount());
            if (updated < 1) {
                throw new BizException("更新账户余额异常");
            }

        });


//

//        List<Long> settlementFlowIdList = dto.getSettlementFlowIdList();
//
//        List<SettlementFlowDO> settlementFlowDOList = settlementFlowService.lambdaQuery().
//                in(BaseEntity::getId, settlementFlowIdList).list();
//        List<String> accountList = new ArrayList<>();
//        settlementFlowDOList.forEach(a ->{
//            accountList.add(a.getIncomeAccount());
//        });
//
//        List<PayAccountDO> accountDOList = payAccountService.lambdaQuery().in(PayAccountDO::getAccount, accountList).list();
//
//
//        Map<String, PayAccountDO> accountMap = new HashMap<>();
//        accountDOList.forEach(a->{
//            accountMap.put(a.getAccount(),a);
//        });
//
//                List<ContrastBillTaskDO> contrastBillTaskDOList = new ArrayList<>();
//        List<MoneyFlowDO> moneyFlowDOList = new ArrayList<>(settlementFlowDOList.size());
//        List<PayAccountDO> payAccountDOUpdateList = new ArrayList<>();
//        List<EverydayAccountRemainDO> everydayAccountRemainDOList = new ArrayList<>();
//
//        for (SettlementFlowDO a : settlementFlowDOList) {
//
//            String incomeAccount = a.getIncomeAccount();
//            PayAccountDO payAccountDO = accountMap.get(incomeAccount);
//            PayAccountDO payAccountDOUpdate = new PayAccountDO();
//
//            BigDecimal incomeMoney = a.getIncomeMoney();
//            BigDecimal remainMoney = payAccountDO.getRemainMoney();
//            Date payFinishTime = a.getPayFinishTime();
//            String payFinishTimeFormat = DateUtils.format(payFinishTime, DateUtils.DATE);
//            //暂时写死 把程序调通
//            Long businessId = 1245205236008751106L;
//
//            //变更后账户余额
//            BigDecimal afterRemainMoney = null;
//            IncomeSubjectEnum subjectEnum = IncomeSubjectEnum.getByCode(a.getSource());
//            MoneyFlowDO moneyFlowDO = new MoneyFlowDO();
//            moneyFlowDO.setSettlementFlowId(a.getId());
//            moneyFlowDO.setCode(CodeGenerateUtils.generaCode());
//            moneyFlowDO.setIncome(subjectEnum.getIncome());
//            moneyFlowDO.setIncomeSubjectName(subjectEnum.getName());
//            if (subjectEnum.equals(IncomeSubjectEnum.CODE0) || subjectEnum.equals(IncomeSubjectEnum.CODE1)) {
//
//                //收入
//                //变更后账户余额
//                afterRemainMoney = BigDecimalUtils.add(payAccountDO.getRemainMoney(), incomeMoney);
//            }
//            if (subjectEnum.equals(IncomeSubjectEnum.CODE2) || subjectEnum.equals(IncomeSubjectEnum.CODE3)) {
//
//                afterRemainMoney = BigDecimalUtils.add(payAccountDO.getRemainMoney(), incomeMoney);
//            }
//            moneyFlowDO.setIncomeMoney(incomeMoney);
//            moneyFlowDO.setAccountType(a.getAccountType());
//            moneyFlowDO.setIncomeAccount(incomeAccount);
//            moneyFlowDO.setBeforeRemainMoney(remainMoney);
//            moneyFlowDO.setAfterRemainMoney(afterRemainMoney);
//            moneyFlowDO.setTradeFinishTime(payFinishTime);
//            moneyFlowDO.setTradeFinishTimeFormat(payFinishTimeFormat);
//            moneyFlowDO.setBusinessId(businessId);
//            moneyFlowDO.setTradeOrderId(a.getTradeOrderId());
//            moneyFlowDO.setSupplierName(subjectEnum.getName());
//            moneyFlowDOList.add(moneyFlowDO);
//            //构建payAccountDOUpdate
//            payAccountDOUpdate.setId(payAccountDO.getId());
//            payAccountDOUpdate.setRemainMoney(afterRemainMoney);
//            payAccountDOUpdateList.add(payAccountDOUpdate);
//            /**
//             * 添加对账任务
//             */
////            ContrastBillTaskDO billTaskDO = contrastBillTaskService.lambdaQuery().
////                    eq(ContrastBillTaskDO::getContrastBillTime, payFinishTimeFormat).
////                    eq(ContrastBillTaskDO::getBusinessId, businessId).one();
////            boolean include = include(contrastBillTaskDOList, payFinishTimeFormat, businessId);
////            if (billTaskDO == null && !include) {
////
////                ContrastBillTaskDO contrastBillTaskDO = new ContrastBillTaskDO();
////                contrastBillTaskDO.setBusinessId(businessId);
////                contrastBillTaskDO.setFinishStatus(0);
////                contrastBillTaskDO.setContrastBillTime(payFinishTimeFormat);
////                contrastBillTaskDOList.add(contrastBillTaskDO);
////            }
////            //构建EverydayAccountRemainDO
////            EverydayAccountRemainDO everydayAccountRemainDO = new EverydayAccountRemainDO();
////            everydayAccountRemainDO.setBusinessId(businessId);
////            everydayAccountRemainDO.setAccount(incomeAccount);
////            everydayAccountRemainDO.setRemainMoney(afterRemainMoney);
////            everydayAccountRemainDO.setPayFinishTimeFormat(payFinishTimeFormat);
////            everydayAccountRemainDOList.add(everydayAccountRemainDO);
//        }
//        /**
//         * 写入去重:唯一索引去重
//         *
//         */
//        this.saveBatch(moneyFlowDOList);
//        /**
//         * 乐观锁更新
//         */
//        payAccountService.updateBatchById(payAccountDOUpdateList);
//
//        if (!contrastBillTaskDOList.isEmpty()) {
//            /**
//             * 写入去重:唯一索引去重
//             */
//            contrastBillTaskService.saveBatch(contrastBillTaskDOList);
//        }
//        /**
//         * 写入或更新EverydayAccountRemainDO
//         */
//        List<EverydayAccountRemainDO> everydayAccountRemainDOSave = new ArrayList<>();
//        List<EverydayAccountRemainDO> everydayAccountRemainDOUpdate = new ArrayList<>();
//        everydayAccountRemainDOList.forEach(a -> {
//
//            String account = a.getAccount();
//            String payFinishTimeFormat = a.getPayFinishTimeFormat();
//            EverydayAccountRemainDO remainDO = everydayAccountRemainService.lambdaQuery().
//                    eq(EverydayAccountRemainDO::getAccount, account).
//                    eq(EverydayAccountRemainDO::getPayFinishTimeFormat, payFinishTimeFormat).one();
//            if (remainDO == null) {
//                everydayAccountRemainDOSave.add(a);
//            } else {
//                a.setId(remainDO.getId());
//                everydayAccountRemainDOUpdate.add(a);
//            }
//
//        });
//        if (!everydayAccountRemainDOSave.isEmpty()) {
//            everydayAccountRemainService.saveBatch(everydayAccountRemainDOSave);
//        }
//        if (!everydayAccountRemainDOUpdate.isEmpty()) {
//            everydayAccountRemainService.updateBatchById(everydayAccountRemainDOUpdate);
//        }


    }



    @Override
    public PageResult<MoneyFlowDO> moneyFlowPage(MoneyFlowPageQuery query) {

        Long shopId = null;
        String tenantCode = UserUtils.getTenantCode();
        if ( !Constants.ADMIN_TENANT_CODE.equals(tenantCode)){
            //平台管理员查询所有资金流水 租户查询自己的流水
            shopId = UserUtils.getShopId();
        }

        Date startTime = null;
        Date endTime = null;
        if (!StringUtils.isEmpty(query.getStartTime())) {
            String startTimeStr = query.getStartTime() + " 00:00:00";
            startTime = DateUtils.parse(startTimeStr, DateUtils.DATE_TIME);
        }
        if (!StringUtils.isEmpty(query.getEndTime())) {
            String endTimeStr = query.getEndTime() + " 23:59:59";
            endTime = DateUtils.parse(endTimeStr, DateUtils.DATE_TIME);

        }
        IPage<MoneyFlowDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<MoneyFlowDO> moneyFlowVOPage = this.lambdaQuery().
                ge(startTime != null, MoneyFlowDO::getTradeFinishTime, startTime).
                le(endTime != null, MoneyFlowDO::getTradeFinishTime, endTime).
                eq(shopId!=null,MoneyFlowDO::getShopId, shopId).
                orderByDesc(BaseEntity::getId).
                page(page);

        return PageResult.toPageResult(moneyFlowVOPage);
    }


    @Override
    public AccountMoneySumVO accountMoneySumQuery(AccountMoneySumQuery query) {

        Long shopId = UserUtils.getUserInfo().getShopId();
        String incomeAccount = query.getIncomeAccount();
        Date startTime = null;
        Date endTime = null;
        if (!StringUtils.isEmpty(query.getStartTime())) {
            String startTimeStr = query.getStartTime() + " 00:00:00";
            startTime = DateUtils.parse(startTimeStr, DateUtils.DATE_TIME);
        }
        if (!StringUtils.isEmpty(query.getEndTime())) {
            String endTimeStr = query.getEndTime() + " 23:59:59";
            endTime = DateUtils.parse(endTimeStr, DateUtils.DATE_TIME);
        }
        //收入金额
        double incomeSumMoney = moneyFlowMapper.sumIncomeMoney(shopId, startTime, endTime, 0, incomeAccount);
        //支出金额
        double payOutSumMoney = moneyFlowMapper.sumIncomeMoney(shopId, startTime, endTime, 1, incomeAccount);
        BigDecimal changeSumMoney = BigDecimalUtil.subtract(new BigDecimal(incomeSumMoney), new BigDecimal(payOutSumMoney));
        AccountMoneySumVO vo = new AccountMoneySumVO();
        vo.setIncomeSumMoney( BigDecimalUtil.scale(new BigDecimal(incomeSumMoney)));
        vo.setPayOutSumMoney( BigDecimalUtil.scale(new BigDecimal(payOutSumMoney)));
        vo.setChangeSumMoney(BigDecimalUtil.scale(changeSumMoney));

        return vo;
    }


}
