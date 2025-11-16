package com.lanf.common.utils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class TokenUtils {
    private static final SecureRandom secureRandom = new SecureRandom();

    
    public static String generateToken() {


        return generateSecureToken(32);
    }
    
    // 生成安全的随机token
    private static String generateSecureToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    

}