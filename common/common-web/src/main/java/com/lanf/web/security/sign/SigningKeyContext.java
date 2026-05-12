package com.lanf.web.security.sign;

import lombok.extern.slf4j.Slf4j;

/**
 * 签名密钥上下文管理器
 * 使用ThreadLocal存储当前请求的AES签名密钥
 */
@Slf4j
public class SigningKeyContext {

    /**
     * ThreadLocal存储当前请求的signingKey（AES密钥字节数组）
     */
    private static final ThreadLocal<byte[]> SIGNING_KEY_HOLDER = new ThreadLocal<>();

    /**
     * ThreadLocal存储签名密钥的来源
     * 0: 通过随机数从Redis获取
     * 1: 从Token中获取
     */
    private static final ThreadLocal<SignKeySourceEnum> SIGN_KEY_SOURCE_HOLDER = ThreadLocal.withInitial(() -> SignKeySourceEnum.RANDOM_KEY);



    /**
     * 获取当前请求的signingKey
     *
     * @return AES密钥字节数组，如果未设置则返回null
     */
    public static byte[] get() {
        return SIGNING_KEY_HOLDER.get();
    }

    /**
     * 设置当前请求的signingKey
     * 设置成功后，自动将来源标记为TOKEN
     *
     * @param signingKey AES密钥字节数组（16字节）
     */
    public static void set(byte[] signingKey) {
        if (signingKey == null) {
            log.warn("尝试设置null的signingKey");
            return;
        }
        SIGNING_KEY_HOLDER.set(signingKey);
        // 设置成功时，标记为从Token获取
        SIGN_KEY_SOURCE_HOLDER.set(SignKeySourceEnum.TOKEN);
        log.debug("signingKey已设置到ThreadLocal，来源: TOKEN，长度: {} 字节", signingKey.length);
    }

    /**
     * 清除当前请求的signingKey
     * 必须在请求结束时调用，防止内存泄漏
     */
    public static void clear() {
        byte[] key = SIGNING_KEY_HOLDER.get();
        if (key != null) {
            // 清空数组内容，增强安全性
            java.util.Arrays.fill(key, (byte) 0);
            log.debug("signingKey已从ThreadLocal清除");
        }
        SIGNING_KEY_HOLDER.remove();
        SIGN_KEY_SOURCE_HOLDER.remove();

        log.debug("所有ThreadLocal变量已清除");
    }

    /**
     * 检查当前请求是否已设置signingKey
     *
     * @return true-已设置，false-未设置
     */
    public static boolean isSet() {
        return SIGNING_KEY_HOLDER.get() != null;
    }

    /**
     * 从Base64字符串解码并设置signingKey
     * 设置成功后，自动将来源标记为TOKEN
     *
     * @param signingKeyBase64 Base64编码的AES密钥
     */
    public static void setFromBase64(String signingKeyBase64) {
        if (signingKeyBase64 == null || signingKeyBase64.isEmpty()) {
           return;
        }

        try {
            byte[] signingKey = java.util.Base64.getDecoder().decode(signingKeyBase64);
            set(signingKey);
        } catch (IllegalArgumentException e) {
            log.error("Base64解码signingKey失败", e);
        }
    }

    /**
     * 获取signingKey的Base64编码字符串（用于调试）
     *
     * @return Base64编码的signingKey，如果未设置则返回null
     */
    public static String getAsBase64() {
        byte[] key = get();
        if (key == null) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(key);
    }

    /**
     * 获取签名密钥的来源
     *
     * @return 签名密钥来源枚举
     */
    public static SignKeySourceEnum getSource() {
        return SIGN_KEY_SOURCE_HOLDER.get();
    }

    /**
     * 设置签名密钥的来源
     *
     * @param source 签名密钥来源
     */
    public static void setSource(SignKeySourceEnum source) {
        if (source == null) {
            log.warn("尝试设置null的签名密钥来源，使用默认值RANDOM_KEY");
            SIGN_KEY_SOURCE_HOLDER.set(SignKeySourceEnum.RANDOM_KEY);
            return;
        }
        SIGN_KEY_SOURCE_HOLDER.set(source);
        log.debug("签名密钥来源已设置为: {}", source.getDescription());
    }

    /**
     * 检查签名密钥是否来自Token
     *
     * @return true-来自Token，false-来自随机数
     */
    public static boolean isFromToken() {
        return SignKeySourceEnum.TOKEN.equals(SIGN_KEY_SOURCE_HOLDER.get());
    }

    /**
     * 检查签名密钥是否来自随机数
     *
     * @return true-来自随机数，false-来自Token
     */
    public static SignKeySourceEnum getSignKeySourceEnum() {

        if (SignKeySourceEnum.TOKEN.equals(SIGN_KEY_SOURCE_HOLDER.get())){
            return SignKeySourceEnum.TOKEN;
        }

        return SignKeySourceEnum.RANDOM_KEY;
    }





}
