package com.lanf.user.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RefreshTokenDTO implements Serializable {

    private Long userId;

    private Integer channel;


    private String refreshToken;

     private String deviceId;
}
