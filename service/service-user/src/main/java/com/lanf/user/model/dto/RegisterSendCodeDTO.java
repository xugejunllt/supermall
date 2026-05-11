package com.lanf.user.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterSendCodeDTO implements Serializable {
    /**
     * 获取秘钥的随机key
     */
    private String randomKey;
    private String  phoneNumber;

}
