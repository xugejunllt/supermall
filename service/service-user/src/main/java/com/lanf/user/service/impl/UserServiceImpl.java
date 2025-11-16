package com.lanf.user.service.impl;

import com.lanf.bizcache.service.SmsRateLimitService;
import com.lanf.common.utils.*;
import com.lanf.constant.enums.SmsCodeEnum;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.lock.service.DistributedLocker;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.SendSmsDTO;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.security.utils.UserSessionCache;
import com.lanf.user.mapper.UserMapper;
import com.lanf.user.model.dto.LoginUserDTO;
import com.lanf.user.model.dto.RegisterUserDTO;
import com.lanf.user.model.entity.UserDO;
import com.lanf.user.model.entity.UserLoginLog;
import com.lanf.user.model.vo.LoginUserVO;
import com.lanf.user.service.IUserLoginLogService;
import com.lanf.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.web.exception.BizException;
import com.lanf.web.utils.WebUtil;
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
    @Autowired
    private IUserLoginLogService userLoginLogService;
    @Autowired
    private UserSessionCache userSessionCache;

    @Override
    @DistributedLock(key = "#dto.phoneNumber")
    public void registerUser(RegisterUserDTO dto) {

        //校验
        validateRegisterUser(dto);
        UserDO userDO = BeanCopyUtils.copyBean(dto, UserDO.class);
        fillUser(userDO);
        this.save(userDO);
        //发送mq 注册事件


    }

    private void fillUser(UserDO userDO) {

        userDO.setStatus(1);
        userDO.setNickName("asd");
        userDO.setHeadImageUrl("");
        userDO.setAccount(userDO.getPhoneNumber());
    }

    private void validateRegisterUser(RegisterUserDTO dto) {

        String phoneNumber = dto.getPhoneNumber();
        //校验手机格式
        PhoneValidator.ValidationResult validationResult = PhoneValidator.validatePhone(phoneNumber);

        if (!validationResult.isValid()) {
            log.info(validationResult.getMessage());
            throw new BizException(validationResult.getMessage());
        }

        //校验短信验证码
        String codeKey = String.format(CacheConstants.REGISTER_CODE_KEY, phoneNumber);
        String code = redisCache.getCacheObject(codeKey);
        if (!dto.getCode().equals(code)) {
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
        sendCode(code, phoneNumber, SmsCodeEnum.SMS_CODE_1001.getCode());

    }

    @Override
    @DistributedLock(key = "#phoneNumber")
    public void loginSendCode(String phoneNumber) {
        validateRegisterSendCode(phoneNumber);
        //移除空格、横杠等特殊字符
        phoneNumber = PhoneValidator.cleanPhoneNumber(phoneNumber);
        //生成随机验证码
        String code = CodeGenerateUtils.generateFourDigitCode();
        //缓存验证码
        cacheLoginCode(phoneNumber, code);
        //发送验证码
        sendCode(code, phoneNumber, SmsCodeEnum.SMS_CODE_1001.getCode());

    }


    private void cacheLoginCode(String phoneNumber, String code) {

        String codeKey = String.format(CacheConstants.LOGIN_CODE_KEY, phoneNumber);

        redisCache.setCacheObject(codeKey, code, 10);

    }

    private void cacheRegisterCode(String phoneNumber, String code) {

        String codeKey = String.format(CacheConstants.REGISTER_CODE_KEY, phoneNumber);

        redisCache.setCacheObject(codeKey, code, 1);

    }

    private void sendCode(String code, String phoneNumber, String templateCode) {

        List<String> parameterValueList = Arrays.asList(code);
        SendSmsDTO sendSmsDTO = new SendSmsDTO();
        sendSmsDTO.setTemplateCode(templateCode);
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

    @Override
    @DistributedLock(key = "#dt.phoneNumber")
    public LoginUserVO login(LoginUserDTO dt) {


        validateLogin(dt);

        String sessionId = TokenUtils.generateToken();
        UserDO userDO = this.lambdaQuery().eq(UserDO::getPhoneNumber, dt.getPhoneNumber()).one();

        //加入缓存中
        userSessionCache.cacheSession(dt.getLoginChannel(), userDO.getId(), sessionId);
        //踢人
        kick(dt.getLoginChannel(), userDO.getId());
        //保存登入日志
        UserLoginLog userLoginLog = buildUserLoginLog(dt, sessionId, userDO);
        userLoginLogService.save(userLoginLog);
        //登入成功 构建返回信息
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setUserId(userDO.getId());
        loginUserVO.setToken(sessionId);

        return loginUserVO;
    }

    private void kick(Integer loginChannel, Long userId) {

        if (loginChannel.equals(1)) {
            //当前android登入 踢掉 ios端
            log.info("当前登入渠道android,踢掉ios");
            String sessionKey = String.format(CacheConstants.USER_SESSION, 2, userId);
            redisCache.deleteObject(sessionKey);
        } else if (loginChannel.equals(2)) {
            //ios端 踢掉 当前android
            log.info("当前登入渠道ios,踢掉android");
            String sessionKey = String.format(CacheConstants.USER_SESSION, 1, userId);
            redisCache.deleteObject(sessionKey);
        } else {

            log.info("当前登入渠道web,不需要踢掉");

        }

    }

    private void validateLogin(LoginUserDTO dto) {

        log.info("开始校验");
        String phoneNumber = dto.getPhoneNumber();
        //校验手机格式
        PhoneValidator.ValidationResult validationResult = PhoneValidator.validatePhone(phoneNumber);
        if (!validationResult.isValid()) {
            log.info(validationResult.getMessage());
            throw new BizException(validationResult.getMessage());

        }
        //校验短信验证码
        String codeKey = String.format(CacheConstants.LOGIN_CODE_KEY, phoneNumber);
        String code = redisCache.getCacheObject(codeKey);
        if (!dto.getCode().equals(code)) {
            log.info("验证码错误");
            throw new BizException("验证码错误");
        }
        //
        UserDO userDO = this.lambdaQuery().eq(UserDO::getPhoneNumber, phoneNumber).one();
        if (userDO == null) {
            log.info("用户不存在");
            throw new BizException("用户不存在");
        }

        if (userDO.getStatus() == 2) {
            log.info("账号被禁用");
            throw new BizException("账号被禁用");
        }
        log.info("校验通过");


    }


    private UserLoginLog buildUserLoginLog(LoginUserDTO dt, String sessionId, UserDO userDO) {

        String ip = IpUtil.getIpAddress(WebUtil.getRequest());
        String account = dt.getPhoneNumber();

        Integer loginType = 0;
       //
        UserLoginLog userLoginLog = BeanCopyUtils.copyBean(dt, UserLoginLog.class);
        userLoginLog.setUserId(userDO.getId());
        userLoginLog.setAccount(account);
        userLoginLog.setSessionId(sessionId);
        userLoginLog.setIpAddress(ip);
        userLoginLog.setLoginType(loginType);

        return userLoginLog;
    }

}
