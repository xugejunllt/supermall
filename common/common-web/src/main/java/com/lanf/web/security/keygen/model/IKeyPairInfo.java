package com.lanf.web.security.keygen.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Jarven
 * @date: 2026-02-25 14:38
 * @description:
 */

@Data
public class IKeyPairInfo implements Serializable {


    private String privateKey;

    private String publicKey;



}
