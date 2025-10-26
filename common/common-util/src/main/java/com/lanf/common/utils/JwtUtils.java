package com.lanf.common.utils;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * 生成JSON Web令牌的工具类
 */
public class JwtUtils {
    //30分钟过期
    private static long tokenExpiration = 24*60 * 60 * 1000;
    //刷新token过期时间 10分钟
    private static long refreshTokenExpiration = 10 * 60 * 1000;
    //一天刷新一次token
   private static long refreshUserTokenExpiration = 24* 60 * 60 * 1000;
   // private static long refreshUserTokenExpiration = 5 * 1000;
    private static long userTokenExpiration = 7 * 24 * 60 * 60 * 1000;
    private static String tokenSignKey = "mf5240.com";

    public static String createAdminToken(Long userId, String username, String tenantCode) {

        Date expireTime = new Date(System.currentTimeMillis() + tokenExpiration);
        String token = Jwts.builder()
                .setSubject("AUTH-USER")
                .setExpiration(expireTime)
                .claim("userId", userId + "")
                .claim("username", username)
                .claim("tenantCode", tenantCode)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    public static String createRefreshToken(Long userId, String username, String tenantCode) {

        Date expireTime = new Date(System.currentTimeMillis() + refreshTokenExpiration);
        String token = Jwts.builder()
                .setSubject("AUTH-USER")
                .setExpiration(expireTime)
                .claim("expireTime", expireTime)
                .claim("userId", userId + "")
                .claim("username", username)
                .claim("tenantCode", tenantCode)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    public static String createUserToken(Long userId) {
        String token = Jwts.builder()
                .setSubject("AUTH-USER")
                .setExpiration(new Date(System.currentTimeMillis() + userTokenExpiration))
                .claim("userId", userId + "")
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }
    public static String createUserRefreshToken(Long userId) {
        Date expireTime = new Date(System.currentTimeMillis() + refreshUserTokenExpiration);
        String token = Jwts.builder()
                .setSubject("AUTH-USER")
                .setExpiration(new Date(System.currentTimeMillis() + userTokenExpiration))
                .claim("expireTime", expireTime)
                .claim("userId", userId + "")
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }
    public static boolean refresh(String token) {
        try {

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            Long object = (Long) claims.get("expireTime");
            if (System.currentTimeMillis() > object) {
                return true;
            }
        } catch (ExpiredJwtException e) {
            return true;
        }
        return false;
    }

    public static Long getUserId(String token) {
        try {
            if (StringUtils.isEmpty(token)) return null;

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            String userId = (String) claims.get("userId");
            return Long.parseLong(userId);
        } catch (ExpiredJwtException e) {
            throw e;

        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public static String getUserIds(String token) {
        try {
            if (StringUtils.isEmpty(token)) return null;

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            String userId = (String) claims.get("userId");
            return userId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getUsername(String token) {
        try {
            if (StringUtils.isEmpty(token)) return "";

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            return (String) claims.get("username");
        } catch (ExpiredJwtException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getTenantCode(String token) {
        try {
            if (StringUtils.isEmpty(token)) return "";

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            return (String) claims.get("tenantCode");
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void removeToken(String token) {
        //jwttoken无需删除，客户端扔掉即可。
    }


}
