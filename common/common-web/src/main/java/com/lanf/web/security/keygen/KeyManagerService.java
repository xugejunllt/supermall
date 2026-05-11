package com.lanf.web.security.keygen;


import com.lanf.web.security.keygen.model.IKeyPairInfo;

/**
 * @author: Jarven
 * @date: 2026-02-26 14:41
 * @description:
 */

public interface KeyManagerService {

    /**
     * 查找密钥对
     * @return
     */
    IKeyPairInfo findKeyPairInfo();
}
