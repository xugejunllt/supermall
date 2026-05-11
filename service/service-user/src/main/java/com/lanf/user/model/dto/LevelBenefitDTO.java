package com.lanf.user.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 等级权益
 */
@Data
public class LevelBenefitDTO implements Serializable {

    /**
     * 权益code
     */
    @NotBlank( message = "权益code不能为空")
    private String code;

}
