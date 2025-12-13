package com.lanf.goods.constant.code;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 */
@Getter
public enum CommonResultCodeEnum {

    /**
     * 当次前段收到次code时 阻塞一秒后 重新发起请求，重试两次，
     * 如果还是失败 使用本地缓存商品详细数据进行页面降级
     */
    LOCK_FAIL(500, "获取加载DB商品详细锁失败");







    private Integer code;

    private String message;

     CommonResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }



}
