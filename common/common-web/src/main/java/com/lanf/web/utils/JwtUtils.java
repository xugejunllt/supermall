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

    private static final String TOKEN_SIGN_KEY = "asass121";

    @Value("${jwt.tokenSignKey:asass121}")
    private String tokenSignKey;
    
    @Value("${jwt.accessTokenExpDays:7}")
    private Long accessTokenExpDays;
    
    @Value("${jwt.refreshTokenExpDays:30}")
    private Long refreshTokenExpDays;
    
    private static JwtBuilder JWT_BUILDER;
    private static JwtParser JWT_PARSER;
    
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

    @PostConstruct
    public void init(){
        tokenSignKey = TOKEN_SIGN_KEY;
        JWT_BUILDER = Jwts.builder();
        JWT_PARSER = Jwts.parser();
        DEFAULT_ACCESS_TOKEN_EXP_DAYS = accessTokenExpDays;
        DEFAULT_REFRESH_TOKEN_EXP_DAYS = refreshTokenExpDays;
    }



    public static String createTokenForUserWithDays(Long userId, String deviceId, long expDays) {

        long time = expDays * 24 * 60 * 60 * 1000;
        return JWT_BUILDER
                .setSubject(SUBJECT_AUTH_USER)
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_DEVICE_ID, deviceId)
                .signWith(SignatureAlgorithm.HS512, TOKEN_SIGN_KEY)
                .compressWith(CompressionCodecs.GZIP)
                .compact();

    }

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

    public static String parseDeviceId(String token) throws ExpiredJwtException {
        try {
            Claims claims = JWT_PARSER
                    .setSigningKey(TOKEN_SIGN_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.get(CLAIM_DEVICE_ID, String.class);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            log.warn("解析deviceId失败 token=[{}]", token, e);
            throw new RuntimeException("解析deviceId失败");
        }
    }

    public static Long getAccessTokenExpDays() {
        return DEFAULT_ACCESS_TOKEN_EXP_DAYS != null ? DEFAULT_ACCESS_TOKEN_EXP_DAYS : 7L;
    }

    public static Long getRefreshTokenExpDays() {
        return DEFAULT_REFRESH_TOKEN_EXP_DAYS != null ? DEFAULT_REFRESH_TOKEN_EXP_DAYS : 30L;
    }

}
