package com.lanf.file.service.manager.impl;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.lanf.common.utils.DateUtils;
import com.lanf.file.model.bo.FileUploadResultBO;
import com.lanf.file.model.dto.FileUploadDTO;
import com.lanf.file.model.dto.FileUploadFileDTO;
import com.lanf.file.service.manager.FileManagerService;
import com.lanf.file.service.manager.FileService;
import com.lanf.file.service.manager.impl.config.AliyunOssConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

@Slf4j

public class AliyunFileServiceImpl extends AbstractFileService {

    @Autowired
    private AliyunOssConfig aliyunOssConfig;

    @Override
    public FileUploadResultBO upload(FileUploadDTO dto) {

        String bucketName = aliyunOssConfig.getBucketName();
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。

       // String objectName = "images/exampleobject.txt";
        String objectName = dto.getDir()+dto.getFileName();
        // 创建OSSClient实例。
        OSS ossClient = getOssClient();
        // 接收返回的url
        String url = "";
        try {
            // 填写Byte数组。
            byte[] content = dto.getContent();
            // 创建PutObjectRequest对象

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new ByteArrayInputStream(content));
            // 获取文件url
            // url过期时间
            Date date = DateUtils.addHour(new Date(), (long) (24*368*10));

            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            url = ossClient.generatePresignedUrl(bucketName, objectName, date).toString();

            log.info("文件上传成功");
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (Exception ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }


        return new FileUploadResultBO(url);
    }


    private  OSS getOssClient() {
     //   String endpoint = ossProperty.getEndpoint();
        return new OSSClientBuilder().build(aliyunOssConfig.getEndpoint(), aliyunOssConfig.getAccessKeyId(), aliyunOssConfig.getAccessKeySecret());
    }



}
