package com.lanf.user.model.vo;


import lombok.Data;

import java.io.Serializable;

/**
 * 公钥信息VO
 */
@Data
public class PublicKeyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 随机数（Redis key前缀）
     */
    private String randomKey;

    /**
     * 公钥
     */
    private String publicKey;
}
