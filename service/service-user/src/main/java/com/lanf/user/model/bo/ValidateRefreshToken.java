package com.lanf.user.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ValidateRefreshToken implements Serializable {

    private Boolean refreshTokenExpired;

}
