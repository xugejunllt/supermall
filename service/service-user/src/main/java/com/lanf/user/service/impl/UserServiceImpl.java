package com.lanf.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.cache.aop.DistributedLock;
import com.lanf.cache.service.DistributedLocker;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.*;
import com.lanf.constant.code.CommonResultCodeEnum;
import com.lanf.constant.constant.RedisKeyConstants;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.SmsCodeEnum;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.SendSmsMsg;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.user.constant.UserRedisKeyConstants;
import com.lanf.user.mapper.UserMapper;
import com.lanf.user.model.bo.UserLevelBO;
import com.lanf.user.model.dto.LoginSendCodeDTO;
import com.lanf.user.model.dto.LoginUserDTO;
import com.lanf.user.model.dto.RefreshTokenDTO;
import com.lanf.user.model.dto.RegisterUserDTO;
import com.lanf.user.model.entity.UserDO;
import com.lanf.user.model.entity.UserLoginLog;
import com.lanf.user.model.vo.UserDetailVO;
import com.lanf.user.model.vo.UserTokenInfoVO;
import com.lanf.user.model.vo.UserVO;
import com.lanf.user.mq.UserClientTopicName;
import com.lanf.user.mq.message.UserRegisterMessage;
import com.lanf.user.service.IUserLoginLogService;
import com.lanf.user.service.IUserService;
import com.lanf.user.service.benefit.IUserLevelService;
import com.lanf.web.auth.RequestAuthExtractor;
import com.lanf.web.model.bo.AuthRequestInfo;
import com.lanf.web.model.bo.JwtTokenInfo;
import com.lanf.web.security.keygen.RsaEncryptKeyManager;
import com.lanf.web.utils.IpUtil;
import com.lanf.web.utils.JwtUtils;
import com.lanf.web.utils.UserContext;
import com.lanf.web.utils.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
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
    private IUserLoginLogService userLoginLogService;

    @Autowired
    private IUserLevelService userLevelService;

    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private RedissonCacheService redissonCacheService;
    @Autowired
    private RsaEncryptKeyManager rsaEncryptKeyManager;
    @Override
    @DistributedLock(key = "#dto.phoneNumber")
    @Transactional
    public void registerUser(RegisterUserDTO dto) {

        //校验
        validateRegisterUser(dto);
        UserDO userDO = BeanCopyUtils.copyBean(dto, UserDO.class);
        fillUser(userDO);
        this.save(userDO);
        UserRegisterMessage message = new UserRegisterMessage();
        message.setUserId(userDO.getId());
        rocketMqClient.sendMessage(UserClientTopicName.USER_REGISTER_EVENT_TOPIC,
                JsonUtils.toJsonString(message));
        log.info("用户注册成功");
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

        //1.校验短信验证码
        String codeKey = String.format(UserRedisKeyConstants.REGISTER_CODE_KEY, phoneNumber);
        String code = redissonCacheService.get(codeKey);
        if (!dto.getCode().equals(code)) {
            log.info("验证码错误");
            throw new BizException("验证码错误");
        }

        //2.校验是否已经注册过了
        List<UserDO> list = this.lambdaQuery().eq(UserDO::getPhoneNumber, phoneNumber).list();
        if (!list.isEmpty()) {
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
    @DistributedLock(key = "#dto.phoneNumber")
    public void loginSendCode(LoginSendCodeDTO dto) {

        String phoneNumber = dto.getPhoneNumber();
        String randomKey = dto.getRandomKey();
        String phoneNumberEncryptedData = rsaEncryptKeyManager.decryptAndVerify(randomKey, phoneNumber);

        log.info("解密后的数据是[{}]", phoneNumberEncryptedData);

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

        String codeKey = String.format(UserRedisKeyConstants.LOGIN_CODE_KEY, phoneNumber);

        redissonCacheService.set(codeKey, code, 10, TimeUnit.MINUTES);

    }

    private void cacheRegisterCode(String phoneNumber, String code) {

        String codeKey = String.format(UserRedisKeyConstants.REGISTER_CODE_KEY, phoneNumber);

        redissonCacheService.set(codeKey, code, 10, TimeUnit.MINUTES);

    }

    private void sendCode(String code, String phoneNumber, String templateCode) {

        List<String> parameterValueList = Arrays.asList(code);
        SendSmsMsg sendSmsDTO = new SendSmsMsg();
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

    }

    @Override
    @DistributedLock(key = "#dt.phoneNumber")
    public UserTokenInfoVO login(LoginUserDTO dt, HttpServletRequest request) {


        validateLogin(dt);
        UserDO userDO = this.lambdaQuery()
                .eq(UserDO::getPhoneNumber, dt.getPhoneNumber())
                .one();
        UserLoginLog userLoginLog = buildUserLoginLog(dt, userDO);
        userLoginLogService.save(userLoginLog);

        try {
            AuthRequestInfo authRequestInfo = RequestAuthExtractor.extractBasicInfo(request);

            return generateAndCacheTokens(userDO.getId(), authRequestInfo);

        } catch (Exception e) {
            throw new BizException("登录失败");
        }

    }

    private UserTokenInfoVO generateAndCacheTokens(Long userId, AuthRequestInfo authRequestInfo) throws Exception {

        String deviceId = authRequestInfo.getDeviceId();
        String channel = authRequestInfo.getChannel();
        Long accessTokenExpDays = JwtUtils.getAccessTokenExpDays();
        Long refreshTokenExpDays = JwtUtils.getRefreshTokenExpDays();

        String accessToken = JwtUtils.createTokenForUserWithDays(userId, deviceId, accessTokenExpDays);
        String refreshToken = JwtUtils.createTokenForUserWithDays(userId, deviceId, refreshTokenExpDays);

        String accessKey = String.format(RedisKeyConstants.USER_ACCESS_TOKEN, userId, channel);
        String refreshKey = String.format(RedisKeyConstants.USER_REFRESH_TOKEN, userId, channel);

        redissonCacheService.set(accessKey, accessToken, accessTokenExpDays, TimeUnit.DAYS);
        redissonCacheService.set(refreshKey, refreshToken, refreshTokenExpDays, TimeUnit.DAYS);

        log.info("生成并缓存Token成功: userId={}, deviceId={}, accessTokenExpDays={}, refreshTokenExpDays={}",
                userId, deviceId, accessTokenExpDays, refreshTokenExpDays);

        UserTokenInfoVO tokenInfo = new UserTokenInfoVO();
        tokenInfo.setAccessToken(accessToken);
        tokenInfo.setRefreshToken(refreshToken);
        
        // 计算过期时间戳（当前时间 + 有效期）
        tokenInfo.setAccessTokenExp(DateUtils.getExpireTimestampFromDays(accessTokenExpDays));
        tokenInfo.setRefreshTokenExp(DateUtils.getExpireTimestampFromDays(refreshTokenExpDays));

        return tokenInfo;
    }


    private void validateLogin(LoginUserDTO dto) {


        //1.校验短信验证码 抛出自定义异常 上面捕获 然后redis统计
        log.info("开始校验");
        String phoneNumber = dto.getPhoneNumber();
        //2.校验手机格式
        PhoneValidator.ValidationResult validationResult = PhoneValidator.validatePhone(phoneNumber);
        if (!validationResult.isValid()) {
            log.warn(validationResult.getMessage());
            throw new BizException(validationResult.getMessage());

        }

        UserDO userDO = this.lambdaQuery().eq(UserDO::getPhoneNumber, phoneNumber).one();
        if (userDO == null) {
            log.warn("用户不存在");
            throw new BizException("用户不存在");
        }

        if (userDO.getStatus() == 2) {
            log.warn("账号被禁用");
            throw new BizException("账号被禁用");
        }

        //3.从Redis获取验证码并校验
        String codeKey = String.format(UserRedisKeyConstants.LOGIN_CODE_KEY, phoneNumber);
        String code = redissonCacheService.get(codeKey);
        if (!dto.getCode().equals(code)) {
            log.warn("验证码错误");
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
    public UserTokenInfoVO refreshToken(RefreshTokenDTO dto, HttpServletRequest request) {

        try {
            AuthRequestInfo authRequestInfo = RequestAuthExtractor.extractBasicInfo(request);
            
            String refreshToken = dto.getRefreshToken();
            if (IStringUtils.isEmpty(refreshToken)) {
                log.warn("刷新令牌为空");
                throw new BizException(CommonResultCodeEnum.KICKED_OUT.getCode(),
                        CommonResultCodeEnum.KICKED_OUT.getMessage());
            }
            JwtTokenInfo jwtTokenInfo;
            try {
                jwtTokenInfo = JwtUtils.parseUserToken(refreshToken);
            } catch (Exception e) {
                throw new BizException(CommonResultCodeEnum.KICKED_OUT.getCode(),
                        CommonResultCodeEnum.KICKED_OUT.getMessage());
            }
            
            String requestDeviceId = authRequestInfo.getDeviceId();
            String tokenDeviceId = jwtTokenInfo.getDeviceId();

            if (!requestDeviceId.equals(tokenDeviceId)) {
                log.warn("设备ID不一致，请求头: {}, Token中: {}", requestDeviceId, tokenDeviceId);
                throw new BizException(CommonResultCodeEnum.KICKED_OUT.getCode(),
                        CommonResultCodeEnum.KICKED_OUT.getMessage());
            }
            Long userId = jwtTokenInfo.getUserId();
            String channel = authRequestInfo.getChannel();
            String refreshKey = String.format(RedisKeyConstants.USER_REFRESH_TOKEN, userId, channel);
            String cachedRefreshToken = redissonCacheService.get(refreshKey);
            
            if (IStringUtils.isEmpty(cachedRefreshToken)) {
                log.warn("刷新令牌已失效，可能已被使用或过期: userId={}", userId);
                throw new BizException(CommonResultCodeEnum.KICKED_OUT.getCode(),
                        CommonResultCodeEnum.KICKED_OUT.getMessage());
            }
            if (!refreshToken.equals(cachedRefreshToken)) {
                log.warn("刷新令牌与缓存不一致，可能存在安全风险: userId={}", userId);
                throw new BizException(CommonResultCodeEnum.KICKED_OUT.getCode(),
                        CommonResultCodeEnum.KICKED_OUT.getMessage());
            }
            
            UserTokenInfoVO tokenInfo = generateAndCacheTokens(userId, authRequestInfo);
            
            log.info("刷新令牌成功: userId={}, deviceId={}, channel={}", userId, requestDeviceId, channel);
            
            return tokenInfo;
            
        } catch (Exception e) {
            log.error("刷新令牌异常",e);
            throw new BizException(CommonResultCodeEnum.KICKED_OUT.getCode(),
                    CommonResultCodeEnum.KICKED_OUT.getMessage());
        }
    }


    private UserVO getUserById() {


        Long userId = UserContext.getUserId();
        UserDO userDO = this.getById(userId);
        if (userDO == null) {
            log.info("用户不存在");
            throw new BizException("用户不存在");

        }

        return BeanCopyUtils.copyBean(userDO, UserVO.class);
    }

    /**
     * 用户详细信息由 基本信息+会员等级信息
     *
     */
    @Override
    public UserDetailVO userDetailQuery() {
        Long userId = UserContext.getUserId();

        UserVO userVO = getUserById();
        UserLevelBO userLevel = userLevelService.getUserLevel(userId);

        UserDetailVO userDetailVO = BeanCopyUtils.copyBean(userVO, UserDetailVO.class);
        userDetailVO.setLevel(userLevel.getLevel());
        userDetailVO.setLevelName(userLevel.getName());
        userDetailVO.setLevelIcon(userLevel.getIcon());

        return userDetailVO;
    }


}

