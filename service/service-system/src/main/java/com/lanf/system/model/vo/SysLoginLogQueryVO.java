package com.lanf.system.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SysLoginLogQueryVO {
	
	@ApiModelProperty(value = "用户账号")
	private String username;

	private String createTimeBegin;
	private String createTimeEnd;

}

