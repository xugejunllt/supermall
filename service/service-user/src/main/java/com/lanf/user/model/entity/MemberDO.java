package com.lanf.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-10
 */
@Data
@TableName("member")
public class MemberDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "密码")
    private String userPassword;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "手机号码")
    private String phoneNumber;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "0.待审核 1.正常 2.禁用")
    private Integer userStatus;

    @ApiModelProperty(value = "注册来源 0:app 1:web")
    private Integer registerSource;

    private String headImageUrl;

}
