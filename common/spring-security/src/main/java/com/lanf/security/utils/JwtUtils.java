package com.lanf.security.utils;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class JwtUtils {

    private static String tokenSignKey = "asass121";

    /**
     * Token Subject 常量
     */
    private static final String SUBJECT_AUTH_USER = "AUTH-USER";
    private static final String SUBJECT_SEC_KILL = "SEC-KILL";

    /**
     * Token Claim Key 常量
     */
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_DEVICE_ID = "deviceId";
    public static final String CLAIM_MERCHANT_ID = "merchantId";
    public static final String CLAIM_USER_NAME = "userName";
    public static final String CLAIM_SEC_KILL_ITEM_ID = "secKillItemId";
    public static final String CLAIM_SEC_KILL_MODE = "secKillMode";

    /**
     * 铭感信息存在token中 不通过请求头传输
     *
     * @param userId
     * @param deviceId
     * @param expTime 单位分钟
     * @return
     */

    public static String createUserToken(Long userId, String deviceId, int expTime) {

        long time = expTime*60*1000;
        String token = Jwts.builder()
                .setSubject(SUBJECT_AUTH_USER)
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .claim(CLAIM_USER_ID, userId + "")
                .claim(CLAIM_DEVICE_ID, deviceId)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    /**
     * 创建秒杀Token
     *
     * @param userId 用户ID
     * @param secKillItemId 秒杀商品ID
     * @param secKillMode 秒杀模式 0：实时秒杀，1：MQ排队秒杀
     * @param expTime 过期时间，单位分钟
     * @return 秒杀Token
     */
    public static String createSecKillToken(Long userId, Long secKillItemId, Integer secKillMode, long expTime) {

        long time = expTime * 60 * 1000;
        return Jwts.builder()
                .setSubject(SUBJECT_SEC_KILL)
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .claim(CLAIM_USER_ID, userId )
                .claim(CLAIM_SEC_KILL_ITEM_ID, secKillItemId )
                .claim(CLAIM_SEC_KILL_MODE, secKillMode)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();

    }
    public static Claims getClaims(String token) throws ExpiredJwtException,Exception{

        return Jwts.parser()
                .setSigningKey(tokenSignKey)
                .parseClaimsJws(token).getBody();
    }
    public static String parseDeviceId(String token) throws ExpiredJwtException,Exception {


        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();

        return (String) claims.get(CLAIM_DEVICE_ID);
    }

    public static String parseMerchantId(String token) throws ExpiredJwtException,Exception {


        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();

        return (String) claims.get(CLAIM_MERCHANT_ID);
    }
    public static String parseUserName(String token) throws ExpiredJwtException,Exception {


        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();

        return (String) claims.get(CLAIM_USER_NAME);
    }

    public static Long parseUserId(String token) throws ExpiredJwtException,Exception{


        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();

        return Long.parseLong((String) claims.get(CLAIM_USER_ID));
    }

    /**
     * 解析秒杀Token中的用户ID
     *
     * @param token 秒杀Token
     * @return 用户ID
     */
    public static Long parseSecKillUserId(String token) throws ExpiredJwtException, Exception {
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return Long.parseLong((String) claims.get(CLAIM_USER_ID));
    }

    /**
     * 解析秒杀Token中的秒杀商品ID
     *
     * @param token 秒杀Token
     * @return 秒杀商品ID
     */
    public static Long parseSecKillItemId(String token) throws ExpiredJwtException, Exception {
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return Long.parseLong((String) claims.get(CLAIM_SEC_KILL_ITEM_ID));
    }

    /**
     * 解析秒杀Token中的秒杀模式
     *
     * @param token 秒杀Token
     * @return 秒杀模式 0：实时秒杀，1：MQ排队秒杀
     */
    public static Integer parseSecKillMode(String token) throws ExpiredJwtException, Exception {
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return Integer.parseInt((String) claims.get(CLAIM_SEC_KILL_MODE));
    }

}
