package com.lanf.api.user.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class UserRegisterMessage extends BaseMessage {

    private Long userId;
}
