package com.lanf.pay.service.reconciliation.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.mapper.SignCustomerFundBillDetailMapper;
import com.lanf.pay.model.entity.SignCustomerFundBillDetailDO;
import com.lanf.api.pay.model.enums.BillTypeEnum;
import com.lanf.api.pay.model.query.SignCustomerFundBillDetailPageQuery;
import com.lanf.api.pay.model.vo.SignCustomerFundBillDetailPageVO;
import com.lanf.pay.service.reconciliation.SignCustomerIFundBillDetailService;
import com.lanf.pay.service.reconciliation.excel.impl.AalPaySignCustomerFundBillDetailExcel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 资金账单明细表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-29
 */
@Slf4j
@Service
public class SignCustomerFundBillDetailServiceImpl extends ServiceImpl<SignCustomerFundBillDetailMapper, SignCustomerFundBillDetailDO> implements SignCustomerIFundBillDetailService {



    @Override
    public void importFromExcel(InputStream inputStream, String batchId, PayChannelEnum payChannel, File excelFile, String billType) {

        log.info("开始导入对账单 Excel: batchId={}, payChannel={},excelFile={}", batchId, payChannel,excelFile);


        Class<?> head = null;
        ReadListener<?> listener = null;


        if (BillTypeEnum.SIGN_CUSTOMER.getCode().equals(billType)){
            switch (payChannel){
                case ALI_PAY:
                    head = AalPaySignCustomerFundBillDetailExcel.class;
                    listener = new AalPaySignCustomerFundBillDetailExcel(batchId, payChannel.getCode().toString());
                    break;
                case WECHAT_PAY:
                    log.error("不支持的支付渠道: {}", payChannel);
                    break;
                default:
                    log.error("不支持的支付渠道: {}", payChannel);
                    throw new BizException("不支持的支付渠道");
            }
        }



        try {

            EasyExcel.read(inputStream, head, listener)
                    .charset(Charset.forName("GBK"))
                    .sheet()
                    .doRead();

            log.info("对账单 Excel 导入完成: batchId={}, payChannel={}", batchId, payChannel);

        } catch (Exception e) {
            log.error("对账单 Excel 导入失败: batchId={}, payChannel={}", batchId, payChannel, e);
        } finally {
//            // 无论成功或失败，都删除临时文件
//            if (excelFile != null && excelFile.exists()) {
//                boolean deleted = excelFile.delete();
//                if (deleted) {
//                    log.info("临时文件已清理: {}", excelFile.getAbsolutePath());
//                } else {
//                    log.warn("临时文件清理失败: {}", excelFile.getAbsolutePath());
//                }
//            }
        }
    }

    @Override
    public PageResult<SignCustomerFundBillDetailPageVO> signCustomerFundBillDetailPageQuery(SignCustomerFundBillDetailPageQuery query) {
        Page<SignCustomerFundBillDetailDO> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<SignCustomerFundBillDetailDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getBatchId())) {
            wrapper.eq(SignCustomerFundBillDetailDO::getPayFinishDate, query.getBatchId());
        }


        Page<SignCustomerFundBillDetailDO> resultPage = baseMapper.selectPage(page, wrapper);
        List<SignCustomerFundBillDetailPageVO> records = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return new PageResult<>(records, resultPage.getSize(), resultPage.getTotal());
    }

    private SignCustomerFundBillDetailPageVO convertToVO(SignCustomerFundBillDetailDO detailDO) {
        SignCustomerFundBillDetailPageVO vo = new SignCustomerFundBillDetailPageVO();
        vo.setId(detailDO.getId());
        vo.setPayChannel(detailDO.getPayChannel());
        vo.setPayFinishDate(detailDO.getPayFinishDate());
        vo.setMerchantOrderNo(detailDO.getMerchantOrderNo());
        vo.setFinancialSerialNo(detailDO.getFinancialSerialNo());
        vo.setBusinessSerialNo(detailDO.getBusinessSerialNo());
        vo.setOccurTime(detailDO.getOccurTime());
        vo.setCounterpartyAccount(detailDO.getCounterpartyAccount());
        vo.setIncomeAmount(detailDO.getIncomeAmount());
        vo.setExpenseAmount(detailDO.getExpenseAmount());
        vo.setAccountBalance(detailDO.getAccountBalance());
        vo.setTransactionChannel(detailDO.getTransactionChannel());
        vo.setBusinessType(detailDO.getBusinessType());
        vo.setRemark(detailDO.getRemark());
        vo.setCreateTime(detailDO.getCreateTime());
        return vo;
    }
}
