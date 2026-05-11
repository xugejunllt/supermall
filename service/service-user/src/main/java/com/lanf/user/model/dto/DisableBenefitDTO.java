package com.lanf.user.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DisableBenefitDTO implements Serializable {

    @NotNull(message = "id不能为空")
    private Long id;


}
