package com.lanf.security.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class RefreshTokenTokenBO implements Serializable {

    private  String token;

    private  String refreshToken;

}
