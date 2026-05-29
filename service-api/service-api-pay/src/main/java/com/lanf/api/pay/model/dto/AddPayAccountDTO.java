package com.lanf.api.pay.model.dto;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class AddPayAccountDTO implements Serializable {

    @NotNull(message = "账户类型不能为空")
    private PayChannelEnum accountType;

    @NotBlank(message = "账户不能为空")
    private String account;


}
