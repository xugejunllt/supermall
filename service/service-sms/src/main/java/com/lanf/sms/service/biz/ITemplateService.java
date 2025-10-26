package com.lanf.sms.service.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.sms.model.dto.BathSendSmsDTO;
import com.lanf.rocketmq.model.message.SendSmsDTO;
import com.lanf.sms.model.dto.TemplateAddDTO;
import com.lanf.sms.model.entity.TemplateDO;
import com.lanf.sms.service.manager.SmsService;


/**
 * <p>
 * 短信模板 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-30
 */
public interface ITemplateService extends IService<TemplateDO> {


    void templateAdd(TemplateAddDTO dto);
    boolean bathSendSms(BathSendSmsDTO dto, SmsService smsService);
    void sendSms(SendSmsDTO dto);

    /**
     *
     *
     * 运营活动批量发送(提升到达时间)-负载均衡算法
     *
     */
    void   bathSendSms(BathSendSmsDTO dto);

}
