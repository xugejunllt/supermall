package com.lanf.web.utils;

import com.lanf.common.utils.DateUtils;
import com.lanf.web.exception.IExpiredJwtException;
import com.lanf.web.exception.TokenParseException;
import com.lanf.web.model.bo.JwtTokenInfo;
import com.lanf.web.security.keygen.SignKeyManager;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    private static final String TOKEN_SIGN_KEY = "dGVzdC1rZXktMTIzNDU2Nz";

    @Value("${jwt.tokenSignKey:asass121}")
    private String tokenSignKey;
    
    @Value("${jwt.accessTokenExpDays:1000}")
    private Long accessTokenExpDays;
    
    @Value("${jwt.refreshTokenExpDays:10000}")
    private Long refreshTokenExpDays;
    
    @Autowired
    private SignKeyManager signKeyManager;
    
    private static JwtBuilder JWT_BUILDER;
    private static JwtParser JWT_PARSER;
    private static SignKeyManager STATIC_SIGN_KEY_MANAGER;
    
    private static Long DEFAULT_ACCESS_TOKEN_EXP_DAYS;
    private static Long DEFAULT_REFRESH_TOKEN_EXP_DAYS;
    
    private static final String SUBJECT_AUTH_USER = "AUTH-USER";
    private static final String SUBJECT_AUTH_ADMIN = "AUTH-ADMIN";
    private static final String SUBJECT_SEC_KILL = "SEC-KILL";

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_DEVICE_ID = "deviceId";
    public static final String CLAIM_TENANT_ID = "tenantId";
    public static final String CLAIM_MERCHANT_ID = "merchantId";
    public static final String CLAIM_USER_NAME = "userName";
    public static final String CLAIM_SEC_KILL_ITEM_ID = "secKillItemId";
    public static final String CLAIM_SEC_KILL_MODE = "secKillMode";
    public static final String CLAIM_SIGNING_KEY = "signingKey";

    @PostConstruct
    public void init(){
        tokenSignKey = TOKEN_SIGN_KEY;
        JWT_BUILDER = Jwts.builder();
        JWT_PARSER = Jwts.parser();
        DEFAULT_ACCESS_TOKEN_EXP_DAYS = accessTokenExpDays;
        DEFAULT_REFRESH_TOKEN_EXP_DAYS = refreshTokenExpDays;
        STATIC_SIGN_KEY_MANAGER = signKeyManager;
    }



    /**
     * 创建用户Token（使用动态AES密钥签名）
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param expDays 过期天数
     * @return JWT Token
     */
    public static String createTokenForUserWithDays(Long userId, String deviceId, long expDays) {

        //1.生成AES密钥作为signing key
        String signingKey = STATIC_SIGN_KEY_MANAGER.generateAesKeyBase64Only();

        //2.计算过期时间
        long time = DateUtils.getExpireTimestampFromDays(expDays);
        
        //3.构建JWT Token
        return JWT_BUILDER
                .setSubject(SUBJECT_AUTH_USER)
                .setExpiration(new Date(time))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_DEVICE_ID, deviceId)
                .claim(CLAIM_SIGNING_KEY, signingKey)
                .signWith(SignatureAlgorithm.HS512, TOKEN_SIGN_KEY)
                .compressWith(CompressionCodecs.GZIP)
                .compact();

    }


    /**
     * 创建秒杀Token（使用固定密钥签名）
     */
    public static String createSecKillToken(Long userId, Long secKillItemId, Integer secKillMode, long expTime) {

        long time = expTime * 60 * 1000;
        return JWT_BUILDER
                .setSubject(SUBJECT_SEC_KILL)
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_SEC_KILL_ITEM_ID, secKillItemId)
                .claim(CLAIM_SEC_KILL_MODE, secKillMode)
                .signWith(SignatureAlgorithm.HS512, TOKEN_SIGN_KEY)
                .compressWith(CompressionCodecs.GZIP)
                .compact();

    }

    /**
     * 解析用户Token
     */
    public static JwtTokenInfo parseUserToken(String token) throws IExpiredJwtException, TokenParseException {
        try {
            //1.先不验证签名，解析出signingKey
            Claims claims = Jwts.parser()
                    .setSigningKey(TOKEN_SIGN_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            String subject = claims.getSubject();
            if (!SUBJECT_AUTH_USER.equals(subject)) {
                log.warn("Token类型不匹配，期望: {}, 实际: {}", SUBJECT_AUTH_USER, subject);
                throw new TokenParseException();
            }
            Object userIdObj = claims.get(JwtUtils.CLAIM_USER_ID);
            Long userId = null;
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).longValue();  // ← 统一转换为 Long
            }
            JwtTokenInfo tokenInfo = new JwtTokenInfo();
            tokenInfo.setUserId(userId);
            tokenInfo.setDeviceId(claims.get(CLAIM_DEVICE_ID, String.class));
            tokenInfo.setSigningKey(claims.get(CLAIM_SIGNING_KEY, String.class));
            tokenInfo.setExpTime(claims.getExpiration().getTime());
            return tokenInfo;
        } catch (ExpiredJwtException e) {

            log.warn("Token已过期 ",e);
            throw new IExpiredJwtException();
        } catch (Exception e) {

            log.warn("Token解析失败", e);
            throw new TokenParseException();
        }
    }

    /**
     * 解析管理员Token
     */
    public static JwtTokenInfo parseAdminToken(String token) throws IExpiredJwtException, TokenParseException {
        try {
            Claims claims = JWT_PARSER
                    .setSigningKey(TOKEN_SIGN_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            String subject = claims.getSubject();
            if (!SUBJECT_AUTH_ADMIN.equals(subject)) {
                log.warn("Token类型不匹配，期望: {}, 实际: {}", SUBJECT_AUTH_ADMIN, subject);
                throw new TokenParseException();
            }

            JwtTokenInfo tokenInfo = new JwtTokenInfo();
            tokenInfo.setUserId(Long.parseLong(claims.get(CLAIM_USER_ID, String.class)));
            tokenInfo.setDeviceId(claims.get(CLAIM_DEVICE_ID, String.class));
            tokenInfo.setTenantId(Long.parseLong(claims.get(CLAIM_TENANT_ID, String.class)));

            return tokenInfo;
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期 token=[{}]", token);
            throw new IExpiredJwtException();
        } catch (Exception e) {

            log.warn("Token解析失败 token=[{}]", token, e);
            throw new TokenParseException();
        }
    }



    public static Long getAccessTokenExpDays() {
        return DEFAULT_ACCESS_TOKEN_EXP_DAYS != null ? DEFAULT_ACCESS_TOKEN_EXP_DAYS : 7L;
    }

    public static Long getRefreshTokenExpDays() {
        return DEFAULT_REFRESH_TOKEN_EXP_DAYS != null ? DEFAULT_REFRESH_TOKEN_EXP_DAYS : 30L;
    }

}
