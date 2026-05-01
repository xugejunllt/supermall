package com.lanf.pay.mq.listener;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.IStringUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.model.bo.BillDownloadUrlResultBO;
import com.lanf.pay.model.entity.ChannelBillDownloadProgressDO;
import com.lanf.pay.model.enums.BillDownloadStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.BillSynchronizerMessage;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.pay.service.reconciliation.IChannelBillDownloadProgressService;
import com.lanf.pay.service.reconciliation.IFundBillDetailService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

/**
 * 同步账单
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.BILL_SYNCHRONIZER_TOPIC,
        consumerGroup = PayMqGroupName.BILL_SYNCHRONIZER_GROUP
)
public class BillSynchronizerListener implements RocketMQListener<BillSynchronizerMessage> {

    @Autowired
    private IChannelBillDownloadProgressService channelBillDownloadProgressService;

    @Autowired
    private IFundBillDetailService fundBillDetailService;
    @Qualifier("taskScheduler")
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;


    @Override
    public void onMessage(BillSynchronizerMessage message) {


        String billDate = message.getBillDate();
        PayChannelEnum payChannel = message.getPayChannel();


        String flowNo = message.getFlowNo();
        ChannelBillDownloadProgressDO one = channelBillDownloadProgressService.lambdaQuery()
                .eq(ChannelBillDownloadProgressDO::getBatchId, billDate)
                .eq(ChannelBillDownloadProgressDO::getPayChannel, payChannel)
                .one();
        if (one == null) {
            log.error("该批次不存在");
            return;
        }
        if (BillDownloadStatusEnum.COMPLETED.equals(one.getStatus())) {
            log.info("解析任务已完成");
            return;
        }
        if (!IStringUtils.isEmpty(one.getFlowNo())) {
            log.info("解析任务正在执行");
            return;
        }
        /**
         * 乐观锁 保证只有一个线程执行成功
         */
        boolean update = channelBillDownloadProgressService.lambdaUpdate()
                .eq(ChannelBillDownloadProgressDO::getId, one.getId())
                .eq(ChannelBillDownloadProgressDO::getVersion, one.getVersion())
                .set(ChannelBillDownloadProgressDO::getFlowNo, flowNo)
                .set(ChannelBillDownloadProgressDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新失败");
            throw new MessageRetryConsumeException("更新失败");

        }

        /**
         * 这里继续优化 先下载 下载完成 解析账单
         */
        Integer code = message.getPayChannel().getCode();
        PaymentService paymentService = PaymentServiceFactory.getPaymentService(code);

        //1.获取下载账单下载地址
        BillDownloadUrlResultBO billDownloadUrlResultBO = null;
        File excelFile = null;

        String batchId = one.getBatchId();
        PayChannelEnum channel = message.getPayChannel();
        try {
            billDownloadUrlResultBO = paymentService.queryBillDownloadUrl(message.getBillType(), message.getBillDate());
            //2.下载文件到本地临时目录
            String billDownloadUrl = billDownloadUrlResultBO.getBillDownloadUrl();
            excelFile = downloadFileToLocal(billDownloadUrl, batchId, channel);
        } catch (Exception e) {
            /**
             * 清空流水号 这样就能重试下载任务
             */
             channelBillDownloadProgressService.lambdaUpdate()
                    .eq(ChannelBillDownloadProgressDO::getId, one.getId())
                    .set(ChannelBillDownloadProgressDO::getFlowNo, null)
                    .update();

            throw new MessageRetryConsumeException("查询下载地址失败");
        }
        /**
         * 3.异步解析账单
         */
        ParseExcelTask task =    new ParseExcelTask(  batchId,  channel, fundBillDetailService, excelFile);
        taskScheduler.execute( task);

    }


    /**
     * 下载文件到本地临时目录
     */
    private static File downloadFileToLocal(String downloadUrl, String batchId,
                                            PayChannelEnum channel) {

        try {
            // 创建临时目录
            String tempDir = System.getProperty("java.io.tmpdir") + "/bill_download/";
            File dir = new File(tempDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成临时文件名
            String fileName = String.format("%s_%s_%d.xlsx",
                    batchId, channel.name(), System.currentTimeMillis());
            File tempFile = new File(tempDir + fileName);

            // 下载文件（使用 HttpURLConnection 或 HttpClient）
            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            connection.disconnect();
            log.info("文件下载成功: {}", tempFile.getAbsolutePath());

            return tempFile;

        } catch (Exception e) {
            log.error("文件下载失败: url={}", downloadUrl, e);
            throw new BizException("文件下载失败");
        }
    }

    static  class  ParseExcelTask implements Runnable{


        private final String batchId;
        private final PayChannelEnum channel;
        private final IFundBillDetailService fundBillDetailService;
        private final File excelFile;

        public ParseExcelTask( String batchId, PayChannelEnum channel, IFundBillDetailService fundBillDetailService,File excelFile) {
            this.batchId = batchId;
            this.channel = channel;
            this.fundBillDetailService = fundBillDetailService;
            this.excelFile = excelFile;
        }

        @Override
        public void run() {

            //3.解析账单
            try (InputStream inputStream = Files.newInputStream(excelFile.toPath())) {

                fundBillDetailService.importFromExcel(inputStream, batchId, channel,excelFile);

            } catch (IOException e) {
                log.error("解析账单失败",e);
            }
        }
    }

}
