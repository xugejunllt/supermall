package com.lanf.sms.service.biz.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lanf.common.utils.IStringUtils;
import com.lanf.sms.mapper.TemplateMapper;
import com.lanf.sms.model.bo.SendSmsResultBO;
import com.lanf.sms.model.dto.BathSendSmsDTO;
import com.lanf.rocketmq.model.message.SendSmsMsg;
import com.lanf.sms.model.dto.StartSendSmsDTO;
import com.lanf.sms.model.dto.TemplateAddDTO;
import com.lanf.sms.model.entity.ChannelTemplateDO;
import com.lanf.sms.model.entity.SendLogDO;
import com.lanf.sms.model.entity.TemplateDO;
import com.lanf.sms.service.biz.IChannelTemplateService;
import com.lanf.sms.service.biz.ISendLogService;
import com.lanf.sms.service.biz.ITemplateService;
import com.lanf.sms.service.manager.SmsManagerService;
import com.lanf.sms.service.manager.SmsService;
import com.lanf.sms.service.manager.impl.SmsFactory;
import com.lanf.sms.service.manager.impl.config.SmsConfig;
import com.lanf.constant.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 短信模板 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-30
 */
@Service
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, TemplateDO> implements ITemplateService {


    @Autowired
    private SmsManagerService smsManagerService;
    @Autowired
    private ISendLogService sendLogService;
    @Autowired
    private SmsConfig smsConfig;
    @Autowired
    private SmsFactory smsFactory;
    @Autowired
    private IChannelTemplateService channelTemplateService;

    @Override
    public void templateAdd(TemplateAddDTO dto) {

    }

    @Override
    public boolean bathSendSms(BathSendSmsDTO dto, SmsService smsService) {
        String templateCode = dto.getTemplateCode();
        ///
        ChannelTemplateDO templateDO = channelTemplateService.lambdaQuery().eq(ChannelTemplateDO::getCode, templateCode).
                eq(ChannelTemplateDO::getChannel,smsService.getChanel()) .one();

        if (templateDO == null){
            throw new BizException("渠道模板不存在");
        }
        ////
        TemplateDO one = this.lambdaQuery().eq(TemplateDO::getId, templateDO.getTemplateId()).one();
        if (one == null) {

            throw new BizException("模板不存在");
        }
        if (dto.getPhones().size()>1000){
            throw new RuntimeException("超过最大发送号码");
        }
        Map<String, String> templateParam = buildTemplateParam(one.getPlaceholderKey(), dto.getParameterValueList());
        StartSendSmsDTO sendAliSmsDTO = new StartSendSmsDTO();
        sendAliSmsDTO.setTemplateCode(one.getCode());
        sendAliSmsDTO.setPhones(dto.getPhones());
        sendAliSmsDTO.setTemplateParam(JSON.toJSONString(templateParam));
        SendSmsResultBO sendSmsResult = smsService.sendSms(sendAliSmsDTO);
        Integer status = 0;
        if (!sendSmsResult.getOk()) {
            status = 1;
        }
        //构建发送日志
        String sendContent = buildSendContent(one.getContent(), templateParam);
        SendLogDO sendLog = new SendLogDO();
       // sendLog.setId(1L);
        sendLog.setTemplateCode(one.getCode());
        sendLog.setScene(one.getScene());
        sendLog.setPhone(IStringUtils.splitJoint(dto.getPhones(), ","));
        sendLog.setSendContent(sendContent);
        sendLog.setStatus(status);
        sendLog.setFailMessage(sendSmsResult.getFailMessage());
        sendLog.setChannel(one.getChannel());
        sendLogService.save(sendLog);

        return sendSmsResult.getOk();
    }

    @Override
    public void sendSms(SendSmsMsg dto) {

        String phone = dto.getPhone();
        //频率控制 一分钟1一次  redis key分布式锁 key:phone_code
        //
        BathSendSmsDTO dto2 = new BathSendSmsDTO();
        dto2.setTemplateCode(dto.getTemplateCode());
        dto2.setPhones(Arrays.asList(dto.getPhone()));
        dto2.setParameterValueList(dto.getParameterValueList());
        List<SmsService> activeSmsService = smsFactory.findActiveSmsService();
        Boolean next = null;
        for (SmsService smsService : activeSmsService) {

            if (Boolean.TRUE.equals(next)) {
                break;
            }
            boolean sendSms = bathSendSms(dto2, smsService);
            if (!sendSms && smsConfig.isChannelRetry()) {
                //发送失败，切换短信渠道
                next = true;
            }
        }

    }


    private String buildSendContent(String templateContent, Map<String, String> templateParam) {

        for (Map.Entry<String, String> entry : templateParam.entrySet()) {

            templateContent = templateContent.replace(entry.getKey(), entry.getValue());
        }

        templateContent = templateContent.replace("{", "");
        templateContent = templateContent.replace("}", "");

        return templateContent;
    }


    private Map<String, String> buildTemplateParam(String placeholderKey, List<String> parameterValueList) {

        Map<String, String> templateParamMap = new HashMap<>();

        String[] split = placeholderKey.split(",");
        for (int i = 0; i < split.length; i++) {

            String v1 = parameterValueList.get(i);
            templateParamMap.put(split[i], v1);
        }

        return templateParamMap;

    }
    @Override
    public void bathSendSms(BathSendSmsDTO dto) {

        SmsService smsService = smsFactory.next();
        boolean sendSms = bathSendSms(dto,smsService);
        if (!sendSms){
            throw new RuntimeException("批量发送短信失败");
        }

    }

}
