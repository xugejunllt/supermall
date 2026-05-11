package com.lanf.common.utils;


import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64编码解码工具类
 */
@Slf4j
public class Base64Utils {

    /**
     * 将字节数组编码为Base64字符串
     * 
     * @param data 原始字节数组
     * @return Base64编码字符串
     */
    public static String encode(byte[] data) {
        if (data == null || data.length == 0) {
            throw new BizException("Base64编码数据不能为空");
        }
        try {
            return Base64.getEncoder().encodeToString(data);
        } catch (Exception e) {
            log.error("Base64编码失败", e);
            throw new BizException("Base64编码失败");
        }
    }

    /**
     * 将字符串编码为Base64字符串
     * 
     * @param text 原始字符串
     * @return Base64编码字符串
     */
    public static String encode(String text) {
        if (text == null || text.isEmpty()) {
            throw new BizException("Base64编码文本不能为空");
        }
        try {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("Base64编码失败", e);
            throw new BizException("Base64编码失败");
        }
    }

    /**
     * 将Base64字符串解码为字节数组
     * 
     * @param base64String Base64编码字符串
     * @return 解码后的字节数组
     */
    public static byte[] decodeToBytes(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            throw new BizException("Base64解码字符串不能为空");
        }
        try {
            return Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            log.error("Base64解码失败: {}", base64String, e);
            throw new BizException("Base64解码失败，无效的Base64格式");
        }
    }

    /**
     * 将Base64字符串解码为普通字符串
     * 
     * @param base64String Base64编码字符串
     * @return 解码后的字符串
     */
    public static String decodeToString(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            throw new BizException("Base64解码字符串不能为空");
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(base64String);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("Base64解码失败: {}", base64String, e);
            throw new BizException("Base64解码失败，无效的Base64格式");
        }
    }

    /**
     * 判断字符串是否为有效的Base64编码
     * 
     * @param base64String 待验证的字符串
     * @return true-有效，false-无效
     */
    public static boolean isValidBase64(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(base64String);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
