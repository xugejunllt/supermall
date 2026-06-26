package com.lanf.seckill.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class AddSeckillActivityDTO implements Serializable {


    @NotBlank(message = "活动名称不能为空")
    private String name;



}
