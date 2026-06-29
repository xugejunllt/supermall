package com.lanf.pay.mq.listener;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.JsonUtils;
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
import com.lanf.pay.service.reconciliation.SignCustomerIFundBillDetailService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
    private SignCustomerIFundBillDetailService fundBillDetailService;
    @Qualifier("threadPoolTaskScheduler")
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    @Autowired
    private PaymentServiceFactory paymentServiceFactory;

    @Override
    public void onMessage(BillSynchronizerMessage message) {

        log.info("开始下载解析账单:{}", JsonUtils.toJsonString(message));

        try {
            billSynchronizerMessage( message);
        } catch (Exception e) {
           log.error("下载异常",e);
        }

    }

    public void billSynchronizerMessage(BillSynchronizerMessage message){
        String billDate = message.getBillDate();
        PayChannelEnum payChannel = message.getPayChannel();

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
        if (BillDownloadStatusEnum.DOWNLOADING.equals(one.getStatus())) {
            log.info("解析任务进行中");
            return;
        }
        /**
         * 乐观锁 保证只有一个线程执行成功
         */
        boolean update = channelBillDownloadProgressService.lambdaUpdate()
                .eq(ChannelBillDownloadProgressDO::getId, one.getId())
                .eq(ChannelBillDownloadProgressDO::getVersion, one.getVersion())
                .set(ChannelBillDownloadProgressDO::getStatus, BillDownloadStatusEnum.DOWNLOADING)
                .set(ChannelBillDownloadProgressDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新失败");
            throw new MessageRetryConsumeException("更新失败");

        }


        Integer code = message.getPayChannel().getCode();
        PaymentService paymentService = paymentServiceFactory.getPaymentService(code);

        //1.获取下载账单下载地址
        BillDownloadUrlResultBO billDownloadUrlResultBO = null;
        File excelFile = null;

        String batchId = one.getBatchId();
        PayChannelEnum channel = message.getPayChannel();
        String billType = message.getBillType();


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
                    .set(ChannelBillDownloadProgressDO::getStatus, BillDownloadStatusEnum.INIT)
                    .update();

            throw new MessageRetryConsumeException("查询下载地址失败");
        }
        /**
         * 3.异步解析账单
         */
        ParseExcelTask task =    new ParseExcelTask(  batchId,  channel, fundBillDetailService, excelFile, billType);
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
            String fileName = String.format("%s_%s_%d.zip",
                    batchId, channel.name(), System.currentTimeMillis());
            File zipFile = new File(tempDir + fileName);

            // 下载文件（使用 HttpURLConnection 或 HttpClient）
            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(zipFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            connection.disconnect();
            log.info("文件下载成功: {}", zipFile.getAbsolutePath());

            // 解压 zip 文件
            String extractDir = tempDir + "extract_" + System.currentTimeMillis() + "/";
            File extractFolder = new File(extractDir);
            extractFolder.mkdirs();
            unzip(zipFile.getAbsolutePath(), extractDir);

            // 查找解压后的 CSV 文件
            File csvFile = findCsvFile(extractFolder);
            if (csvFile == null) {
                throw new BizException("解压后未找到 CSV 文件");
            }
            log.info("找到 CSV 文件: {}", csvFile.getAbsolutePath());
            return csvFile;

        } catch (Exception e) {
            log.error("文件下载失败: ", e);
            throw new BizException("文件下载失败");
        }
    }

    /**
     * 解压 zip 文件到指定目录
     */
    private static void unzip(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 支付宝账单 zip 文件名含中文，需指定 GBK 编码
        try (ZipFile zipFile = new ZipFile(zipFilePath, Charset.forName("GBK"))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    File parent = entryFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    try (InputStream is = zipFile.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(entryFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    /**
     * 递归查找目录下的 CSV 文件
     */
    private static File findCsvFile(File dir) {
        if (!dir.isDirectory()) {
            return null;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                File found = findCsvFile(file);
                if (found != null) {
                    return found;
                }
            } else if (file.getName().toLowerCase().endsWith("账务明细.csv")) {
                return file;
            }
        }
        return null;
    }

    static  class  ParseExcelTask implements Runnable{


        private final String batchId;
        private final PayChannelEnum channel;
        private final SignCustomerIFundBillDetailService fundBillDetailService;
        private final File excelFile;
        private final String billType;

        public ParseExcelTask(String batchId, PayChannelEnum channel,
                              SignCustomerIFundBillDetailService fundBillDetailService,
                              File excelFile, String billType) {
            this.batchId = batchId;
            this.channel = channel;
            this.fundBillDetailService = fundBillDetailService;
            this.excelFile = excelFile;
            this.billType = billType;
        }

        @Override
        public void run() {

            //3.解析账单
            try (InputStream inputStream = Files.newInputStream(excelFile.toPath())) {

                fundBillDetailService.importFromExcel(inputStream, batchId, channel,excelFile, billType);

            } catch (IOException e) {
                log.error("解析账单失败",e);
            }
        }
    }

}
