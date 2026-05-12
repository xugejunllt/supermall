package com.lanf.constant.exception;
import com.lanf.constant.code.CommonCodeEnum;
import lombok.Data;

/**
 * 自定义全局异常类
 *
 */
@Data
public class BizException extends RuntimeException {

    private Integer code;

    private String message;

    /**
     * 通过状态码和错误消息创建异常对象
     * @param code
     * @param message
     */
    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    public BizException( String message) {
        super(message);
        this.code = CommonCodeEnum.FAIL.getCode();
        this.message = message;
    }
    public BizException(CommonCodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
    }

}
