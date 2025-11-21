package com.lanf.security.utils;

import com.lanf.common.utils.StackTraceUtil;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Date;

@Slf4j
public class JwtUtils {

    private static String tokenSignKey = "asass121";

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
                .setSubject("AUTH-USER")
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .claim("userId", userId + "")
                .claim("deviceId", deviceId)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }
    public static String createUserToken(Long userId, String deviceId,String userName,Long merchantId, int expTime) {

        long time = expTime*60*1000;
        String token = Jwts.builder()
                .setSubject("AUTH-USER")
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .claim("userId", userId + "")
                .claim("deviceId", deviceId)
                .claim("userName", userName)
                .claim("merchantId", merchantId+"")
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    public static String parseDeviceId(String token) throws ExpiredJwtException,Exception {


        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();

        return (String) claims.get("deviceId");
    }

    public static String parseMerchantId(String token) throws ExpiredJwtException,Exception {


        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();

        return (String) claims.get("merchantId");
    }
    public static String parseUserName(String token) throws ExpiredJwtException,Exception {


        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();

        return (String) claims.get("userName");
    }

    public static Long parseUserId(String token) throws ExpiredJwtException,Exception{


        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();

        return Long.parseLong((String) claims.get("userId"));
    }


}
