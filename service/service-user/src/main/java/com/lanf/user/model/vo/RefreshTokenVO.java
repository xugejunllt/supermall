package com.lanf.user.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RefreshTokenVO implements Serializable {

    private String token;

    private String refreshToken;


}
