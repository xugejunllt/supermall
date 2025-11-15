package com.lanf.user.service.impl;

import com.lanf.bizcache.service.SmsRateLimitService;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.common.utils.PhoneValidator;
import com.lanf.constant.enums.SmsCodeEnum;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.lock.service.DistributedLocker;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.SendSmsDTO;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.user.mapper.UserMapper;
import com.lanf.user.model.dto.RegisterUserDTO;
import com.lanf.user.model.entity.UserDO;
import com.lanf.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.web.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * User 接口
 * </p>
 *
 * @author jarven
 * @since 2025-10-27
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements IUserService {

    @Autowired
    private DistributedLocker distributedLocker;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private SmsRateLimitService smsRateLimitService;
    @Autowired
    private RedisCache redisCache;

    @Override
    @DistributedLock(key = "#dto.phoneNumber")
    public void registerUser(RegisterUserDTO dto) {

        //校验
        validateRegisterUser(dto);
        UserDO userDO = BeanCopyUtils.copyBean(dto, UserDO.class);
        fillUser( userDO);
        this.save(userDO);
        //发送mq 注册事件


    }
    private void fillUser(UserDO userDO){

        userDO.setStatus(1);
        userDO.setNickName("asd");
        userDO.setHeadImageUrl("");
    }

    private void validateRegisterUser(RegisterUserDTO dto) {

        String phoneNumber = dto.getPhoneNumber();
        //校验手机格式
        PhoneValidator.ValidationResult validationResult = PhoneValidator.validatePhone( phoneNumber);

        if (!validationResult.isValid()) {
            log.info(validationResult.getMessage());
            throw new BizException(validationResult.getMessage());
        }

        //校验短信验证码
        String codeKey = String.format(CacheConstants.REGISTER_CODE_KEY, phoneNumber);
        String code = redisCache.getCacheObject(codeKey);
        if ( !dto.getCode().equals(code)){
            log.info("验证码错误");
            throw new BizException("验证码错误");
        }

        //校验是否已经注册过了
        List<UserDO> list = this.lambdaQuery().eq(UserDO::getPhoneNumber, phoneNumber).list();
        if (list.size() > 1) {
            log.info("该手机号已被注册");
            throw new BizException("该手机号已被注册");

        }


    }

    @Override
    @DistributedLock(key = "#phoneNumber")
    public void registerSendCode(String phoneNumber) {

        validateRegisterSendCode(phoneNumber);
        //移除空格、横杠等特殊字符
        phoneNumber = PhoneValidator.cleanPhoneNumber(phoneNumber);
        //生成随机验证码
        String code = CodeGenerateUtils.generateFourDigitCode();
        //缓存验证码
        cacheRegisterCode(phoneNumber, code);
        //发送验证码
        sendCode(code, phoneNumber);

    }

    @Override
    public void loginSendCode(String phoneNumber) {

    }

    private void cacheRegisterCode(String phoneNumber, String code) {

        String codeKey = String.format(CacheConstants.REGISTER_CODE_KEY, phoneNumber);

        redisCache.setCacheObject(codeKey, code, 60, TimeUnit.SECONDS);

    }

    private void sendCode(String code, String phoneNumber) {

        List<String> parameterValueList = Arrays.asList(code);
        SendSmsDTO sendSmsDTO = new SendSmsDTO();
        sendSmsDTO.setTemplateCode(SmsCodeEnum.SMS_CODE_1001.getCode());
        sendSmsDTO.setPhone(phoneNumber);
        sendSmsDTO.setParameterValueList(parameterValueList);
        //转成 json
        String message = JsonUtils.toJsonString(sendSmsDTO);
        rocketMqClient.sendMessage(TopicName.SEND_SMS_TOPIC, message);

    }

    private void validateRegisterSendCode(String phoneNumber) {

        //校验手机格式
        PhoneValidator.ValidationResult validationResult = PhoneValidator.validatePhone(phoneNumber);
        if (!validationResult.isValid()) {
            log.info(validationResult.getMessage());
            throw new BizException(validationResult.getMessage());

        }
        //校验发送频率
        boolean canSend = smsRateLimitService.canSend(phoneNumber);
        if (!canSend) {
            log.info("该手机号超过最大发送次数");
            throw new BizException("该手机号超过最大发送次数");

        }
    }


}
