package com.lanf.sms.service.manager.impl;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.lanf.common.utils.StrUtils;
import com.lanf.sms.model.bo.SendSmsResultBO;
import com.lanf.sms.model.dto.BathSendSmsDTO;
import com.lanf.sms.model.dto.StartSendSmsDTO;
import com.lanf.sms.model.entity.TemplateDO;
import com.lanf.sms.model.enums.ChannelEnum;
import com.lanf.sms.service.manager.SmsService;
import com.lanf.sms.service.manager.impl.config.AliyunSmsConfig;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AliyunSmsServiceImpl implements SmsService {

    @Autowired
    private AliyunSmsConfig aliyunSmsConfig;

    @Override
    public SendSmsResultBO sendSms(StartSendSmsDTO dto) {

        SendSmsResultBO sendSmsResult = new SendSmsResultBO();
        List<String> phoneList = dto.getPhones();

        try {

            IClientProfile profile = DefaultProfile.getProfile(aliyunSmsConfig.getRegionId(), aliyunSmsConfig.getAccessKeyId(), aliyunSmsConfig.getAccessKeySecret());
            DefaultProfile.addEndpoint(aliyunSmsConfig.getRegionId(), aliyunSmsConfig.getRegionId(), aliyunSmsConfig.getProduct(), aliyunSmsConfig.getDomain());
            IAcsClient acsClient = new DefaultAcsClient(profile);
            SendSmsRequest request = new SendSmsRequest();
            request.setMethod(MethodType.POST);
            // 手机号可以单个也可以多个（多个用逗号隔开，如：15*******13,13*******27,17*******56）
            request.setPhoneNumbers(StrUtils.splitJoint(phoneList, ","));
            request.setSignName(aliyunSmsConfig.getSignName());
            request.setTemplateCode(dto.getTemplateCode());
            request.setTemplateParam(dto.getTemplateParam());
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

            if ((sendSmsResponse.getCode() != null) && (sendSmsResponse.getCode().equals("OK"))) {
                log.info("发送成功,code:" + sendSmsResponse.getCode());
                sendSmsResult.setOk(true);
            } else {
                log.info("发送失败,code:" + sendSmsResponse.getMessage());
                sendSmsResult.setOk(false);
                sendSmsResult.setFailMessage(sendSmsResponse.getMessage());
                throw new RuntimeException("发送失败");
            }
        } catch (ClientException e) {
            log.error("发送失败,系统错误！", e);
            sendSmsResult.setOk(false);
            throw new RuntimeException("发送失败");
        }

        return sendSmsResult;
    }

    @Override
    public StartSendSmsDTO buildStartSendSmsDTO(TemplateDO one, BathSendSmsDTO dto, String templateParam) {

        StartSendSmsDTO sendAliSmsDTO = new StartSendSmsDTO();
        sendAliSmsDTO.setTemplateCode(one.getCode());
        sendAliSmsDTO.setPhones(dto.getPhones());
        sendAliSmsDTO.setTemplateParam(JSON.toJSONString(templateParam));

        return sendAliSmsDTO;
    }

    @Override
    public String getChanel() {

        return ChannelEnum.ALIYUN.getCode();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
