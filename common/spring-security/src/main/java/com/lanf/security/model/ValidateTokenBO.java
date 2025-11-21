package com.lanf.security.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class ValidateTokenBO implements Serializable {

    private Long userId;



    private Integer channel;

    private  Boolean sessionExpired;

    private String  token;
    private String deviceId;

    private String   userName;

    private  Long merchantId;
}
