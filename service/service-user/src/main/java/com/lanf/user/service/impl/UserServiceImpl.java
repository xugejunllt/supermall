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
import com.lanf.security.model.CacheSessionBO;
import com.lanf.security.utils.JwtUtils;
import com.lanf.security.utils.UserContext;
import com.lanf.security.utils.UserSessionCache;
import com.lanf.user.mapper.UserMapper;
import com.lanf.user.model.bo.ValidateRefreshTokenBO;
import com.lanf.user.model.dto.LoginUserDTO;
import com.lanf.user.model.dto.RefreshTokenDTO;
import com.lanf.user.model.dto.RegisterUserDTO;
import com.lanf.user.model.entity.UserDO;
import com.lanf.user.model.entity.UserLoginLog;
import com.lanf.user.model.vo.LoginUserVO;
import com.lanf.user.model.vo.RefreshTokenVO;
import com.lanf.user.model.vo.UserVO;
import com.lanf.user.service.IUserLoginLogService;
import com.lanf.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.web.exception.BizException;
import com.lanf.web.utils.WebUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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

    @Autowired
    private LoginSecurityService loginSecurityService;


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

        redisCache.setCacheObject(codeKey, code, 1000000000);

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

        if (loginSecurityService.isLocked(dt.getPhoneNumber())){

            throw new BizException("登入失败超过最大登入次数");
        }
        try {
            validateLogin(dt);
        } catch (BizException e) {
            loginSecurityService.handleFailedLogin(dt.getPhoneNumber());
            throw e;
        }

        UserDO userDO = this.lambdaQuery().eq(UserDO::getPhoneNumber, dt.getPhoneNumber()).one();

        //加入缓存中
        CacheSessionBO sessionBO = userSessionCache.cacheSession(dt.getLoginChannel(), userDO.getId(), dt.getDeviceId());

        //踢人
        kick(dt.getLoginChannel(), userDO.getId());

        //保存登入日志
        UserLoginLog userLoginLog = buildUserLoginLog(dt, userDO);
        userLoginLogService.save(userLoginLog);

        //登入成功 构建返回信息
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setUserId(userDO.getId());
        loginUserVO.setRefreshToken(sessionBO.getRefreshToken());
        loginUserVO.setToken(sessionBO.getToken());
        return loginUserVO;
    }

    private void kick(Integer loginChannel, Long userId) {

        if (loginChannel.equals(1)) {
            //当前android登入 踢掉 ios端
            log.info("当前登入渠道android,踢掉ios");
            userSessionCache.cleanSession(2, userId);

        } else if (loginChannel.equals(2)) {
            //ios端 踢掉 当前android
            log.info("当前登入渠道ios,踢掉android");
            userSessionCache.cleanSession(1, userId);
        } else {

            log.info("当前登入渠道web,不需要踢掉");

        }

    }

    private void validateLogin(LoginUserDTO dto) {


        //校验短信验证码 抛出自定义异常 上面捕获 然后redis统计
        log.info("开始校验");
        String phoneNumber = dto.getPhoneNumber();
        //校验手机格式
        PhoneValidator.ValidationResult validationResult = PhoneValidator.validatePhone(phoneNumber);
        if (!validationResult.isValid()) {
            log.info(validationResult.getMessage());
            throw new BizException(validationResult.getMessage());

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

        String codeKey = String.format(CacheConstants.LOGIN_CODE_KEY, phoneNumber);
        String code = redisCache.getCacheObject(codeKey);
        if (!dto.getCode().equals(code)) {
            log.info("验证码错误");
            throw new BizException("验证码错误");
        }
        log.info("校验通过");


    }


    private UserLoginLog buildUserLoginLog(LoginUserDTO dt, UserDO userDO) {

        String ip = IpUtil.getIpAddress(WebUtil.getRequest());
        String account = dt.getPhoneNumber();

        Integer loginType = 0;
        //
        UserLoginLog userLoginLog = BeanCopyUtils.copyBean(dt, UserLoginLog.class);
        userLoginLog.setUserId(userDO.getId());
        userLoginLog.setAccount(account);
        userLoginLog.setIpAddress(ip);
        userLoginLog.setLoginType(loginType);

        return userLoginLog;
    }

    @Override
    public RefreshTokenVO refreshToken(RefreshTokenDTO dto) {

        //校验
        ValidateRefreshTokenBO refreshTokenBO = validateRefreshToken(dto);
        RefreshTokenVO refreshTokenVO = null;
        if (refreshTokenBO.getRefreshTokenExpired()) {
            //刷新token已过期了
            log.info("刷新令牌已过期");

            boolean hasRefreshToken = hasRefreshToken(dto);
            if (hasRefreshToken) {

                log.info("符合重新刷新令牌条件");
                refreshTokenVO = againRefreshToken(dto);

            } else {
                log.info("不符合重新刷新令牌条件");
                throw new BizException("不符合重新刷新令牌条件");
            }

        } else {
            log.info("刷新令牌没有过期");
            refreshTokenVO = againRefreshToken(dto);
        }

        return refreshTokenVO;
    }

    /**
     * 刷新token 已经过期 满足重新刷新token而不需要退出登入的条件
     *
     * @param dto
     * @return
     */
    private boolean hasRefreshToken(RefreshTokenDTO dto) {


        String refreshToken = dto.getRefreshToken();
        String deviceId = parseDeviceId(refreshToken);


        //从风控系统获取用户特征 判断是否能够重新刷新令牌

        return true;
    }

    private String parseDeviceId(String refreshToken) {

        String deviceId = null;
        try {
            deviceId = JwtUtils.parseDeviceId(refreshToken);

        } catch (ExpiredJwtException e) {
            log.info("token已过期");
            return null;

        } catch (Exception e) {
            log.info("JWT 解析异常 [{}]", StackTraceUtil.getStackTrace(e));
            throw new BizException("jwt解析异常");
        }

        return deviceId;
    }

    private RefreshTokenVO againRefreshToken(RefreshTokenDTO dto) {

        log.info("重新颁发令牌开始");
        CacheSessionBO cacheSessionBO = userSessionCache.cacheSession(dto.getChannel(), dto.getUserId(), dto.getDeviceId());
        RefreshTokenVO vo = new RefreshTokenVO();
        vo.setRefreshToken(cacheSessionBO.getRefreshToken());
        vo.setToken(cacheSessionBO.getToken());

        log.info("重新颁发令牌完成");
        return vo;
    }


    private ValidateRefreshTokenBO validateRefreshToken(RefreshTokenDTO dto) {

        String refreshToken = dto.getRefreshToken();
        Integer channel = dto.getChannel();
        Long userId = dto.getUserId();

        Boolean refreshTokenExpired = false;
        String deviceId = null;

        try {
            deviceId = JwtUtils.parseDeviceId(dto.getRefreshToken());

        } catch (Exception e) {

            if (e instanceof ExpiredJwtException) {

                log.info(" JWT token已过期");
                refreshTokenExpired = true;
            } else {
                log.info("JWT解析 deviceId失败[{}]", StackTraceUtil.getStackTrace(e));
                throw new BizException("JWT解析 deviceId失败");

            }

        }
        if (deviceId != null) {
            log.info("JWT refreshToken没有过期");
            if (!deviceId.equals(dto.getDeviceId())) {
                log.info("设备id不一致");
                throw new BizException("设备id不一致");


            }
        }
        /**
         * 避免一个token 被多次使用
         */
        String cacheRefreshToken = userSessionCache.getRefreshToken(channel, userId);
        if (IStringUtils.isEmpty(cacheRefreshToken)) {
            //过期处理
            log.info(" 缓存 刷新token已过期");
            refreshTokenExpired = true;
        } else {

            if (!cacheRefreshToken.equals(refreshToken)) {
                throw new BizException("请求refreshToken与缓存refreshToken不一致");

            }
        }
        //
        String token = userSessionCache.getToken(channel, userId);
        if (!IStringUtils.isEmpty(token)) {
            throw new BizException("token未过期,不允许刷新");

        }
        /**
         * JWT过期 或者缓存过期 都认为已过期
         */
        ValidateRefreshTokenBO bo = new ValidateRefreshTokenBO();
        bo.setRefreshTokenExpired(refreshTokenExpired);

        return bo;
    }

    @Override
    public UserVO getUserById() {


        Long userId = UserContext.getUserId();
        UserDO userDO = this.getById(userId);
        if ( userDO == null){
            log.info("用户不存在");
            throw new BizException("用户不存在");

        }

        return BeanCopyUtils.copyBean(userDO,UserVO.class);
    }


}

