package com.lanf.pay;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.bo.BillDownloadUrlResultBO;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.pay.service.reconciliation.SignCustomerIFundBillDetailService;
import com.lanf.pay.task.SignCustomerBillReconciliationTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Slf4j
@SpringBootTest
public class PayTest {
    @Autowired
    private PaymentServiceFactory paymentServiceFactory;

    @Autowired
    private SignCustomerIFundBillDetailService signCustomerIFundBillDetailService;
    @Autowired
    private SignCustomerIFundBillDetailService fundBillDetailService;
    @Autowired
    private SignCustomerBillReconciliationTask customerBillReconciliationTask;


    @Test
    public void billDownloadTest(){

        PaymentService paymentService = paymentServiceFactory.getPaymentService(0);


        BillDownloadUrlResultBO billDownloadUrlResultBO = paymentService.queryBillDownloadUrl("trade", "2026-06-28");
        //2.下载文件到本地临时目录
            String billDownloadUrl = billDownloadUrlResultBO.getBillDownloadUrl();

            log.info("下载地址是: {}", billDownloadUrl);
    }
    @Test
    public void importTest(){
        String billType = "signcustomer";
        File excelFile = new File("D:\\file\\支付宝交易账单\\20885505611352360156_20260628_账务明细.csv");
        //3.解析账单
        try (InputStream inputStream = Files.newInputStream(excelFile.toPath())) {

            fundBillDetailService.importFromExcel(inputStream, "2026-06-28",
                    PayChannelEnum.ALI_PAY,excelFile, billType);

        } catch (IOException e) {
            log.error("解析账单失败",e);
        }
    }

    @Test
    public void billTradeSynchronizerTaskTest() throws InterruptedException {

        customerBillReconciliationTask.billTradeSynchronizerTask();


    }
    @Test
    public void isTradeAllBillParsedTask() throws InterruptedException {

        customerBillReconciliationTask.isTradeAllBillParsedTask();
        Thread.sleep(10000000000L);
    }


}
