package com.lanf.web.utils;

import com.lanf.web.exception.IExpiredJwtException;
import com.lanf.web.exception.TokenParseException;
import com.lanf.web.model.bo.JwtTokenInfo;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    //TOKEN_SIGN_KEY
    private static final String TOKEN_SIGN_KEY = "asass121";

    @Value("${jwt.tokenSignKey}")
    private String tokenSignKey;
    private static JwtBuilder JWT_BUILDER;
    private static JwtParser JWT_PARSER;
    /**
     * Token Subject 常量
     */
    private static final String SUBJECT_AUTH_USER = "AUTH-USER";
    private static final String SUBJECT_AUTH_ADMIN = "AUTH-ADMIN";
    private static final String SUBJECT_SEC_KILL = "SEC-KILL";

    /**
     * Token Claim Key 常量
     */
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_DEVICE_ID = "deviceId";
    public static final String CLAIM_TENANT_ID = "tenantId";
    public static final String CLAIM_MERCHANT_ID = "merchantId";
    public static final String CLAIM_USER_NAME = "userName";
    public static final String CLAIM_SEC_KILL_ITEM_ID = "secKillItemId";
    public static final String CLAIM_SEC_KILL_MODE = "secKillMode";

    @PostConstruct
    public void init(){
        tokenSignKey = TOKEN_SIGN_KEY;
        JWT_BUILDER = Jwts.builder();
        JWT_PARSER = Jwts.parser();
    }

    public static String createTokenForUser(Long userId, String deviceId, long expTime) {

        long time = expTime * 60 * 1000;
        return JWT_BUILDER
                .setSubject(SUBJECT_AUTH_USER)
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_DEVICE_ID, deviceId)
                .signWith(SignatureAlgorithm.HS512, TOKEN_SIGN_KEY)
                .compressWith(CompressionCodecs.GZIP)
                .compact();

    }

    /**
     * 创建管理员Token
     *
     * @param userId   管理员ID
     * @param deviceId 设备ID
     * @param tenantId 租户ID
     * @param expTime  过期时间，单位分钟
     * @return 管理员Token
     */
    public static String createTokenForAdmin(Long userId, String deviceId, Long tenantId, long expTime) {
        long time = expTime * 60 * 1000;
        return JWT_BUILDER
                .setSubject(SUBJECT_AUTH_ADMIN)
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .claim(CLAIM_USER_ID, userId )
                .claim(CLAIM_DEVICE_ID, deviceId)
                .claim(CLAIM_TENANT_ID, tenantId)
                .signWith(SignatureAlgorithm.HS512, TOKEN_SIGN_KEY)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
    }

    /**
     * 创建秒杀Token
     *
     * @param userId        用户ID
     * @param secKillItemId 秒杀商品ID
     * @param secKillMode   秒杀模式 0：实时秒杀，1：MQ排队秒杀
     * @param expTime       过期时间，单位分钟
     * @return 秒杀Token
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
     *
     * @param token 用户Token
     * @return Token信息对象
     */
    public static JwtTokenInfo parseUserToken(String token) throws IExpiredJwtException, TokenParseException {
        try {
            Claims claims = JWT_PARSER
                    .setSigningKey(TOKEN_SIGN_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            String subject = claims.getSubject();
            if (!SUBJECT_AUTH_USER.equals(subject)) {
                log.warn("Token类型不匹配，期望: {}, 实际: {}", SUBJECT_AUTH_USER, subject);
                throw new TokenParseException();
            }

            JwtTokenInfo tokenInfo = new JwtTokenInfo();
            tokenInfo.setUserId(claims.get(CLAIM_USER_ID, Long.class));
            tokenInfo.setDeviceId(claims.get(CLAIM_DEVICE_ID, String.class));

            return tokenInfo;
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期 token=[{}]", token);
            throw new IExpiredJwtException();
        } catch (Exception e) {

            log.warn("Token解析失败 token=[{}]", token, e);
            throw new TokenParseException();
        }
    }

    /**
     * 解析管理员Token
     *
     * @param token 管理员Token
     * @return Token信息对象
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


}
