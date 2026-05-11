package com.lanf.web.security.sign;

import lombok.Getter;

/**
 * 签名密钥来源枚举
 */
@Getter
public enum SignKeySourceEnum {

    /**
     * 通过随机数从Redis获取签名密钥
     */
    RANDOM_KEY(0, "通过随机数获取"),

    /**
     * 从Token中获取签名密钥
     */
    TOKEN(1, "从Token中获取");

    private final Integer code;
    private final String description;

    SignKeySourceEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     * 
     * @param code 代码
     * @return 枚举值，如果不存在则返回RANDOM_KEY
     */
    public static SignKeySourceEnum getByCode(Integer code) {
        if (code == null) {
            return RANDOM_KEY;
        }
        
        for (SignKeySourceEnum source : values()) {
            if (source.getCode().equals(code)) {
                return source;
            }
        }
        
        return RANDOM_KEY;
    }
}
