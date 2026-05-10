package com.lanf.user.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterMessage implements Serializable {

    private Long userId;
}
