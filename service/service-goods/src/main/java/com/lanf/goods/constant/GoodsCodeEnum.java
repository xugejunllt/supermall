package com.lanf.goods.constant;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 */
@Getter
public enum GoodsCodeEnum {

    /**
     * 当次前段收到次code时 阻塞一秒后 重新发起请求，重试两次，
     * 如果还是失败 使用本地缓存商品详细数据进行页面降级
     */
    LOCK_FAIL(4001, "获取加载DB商品详细锁失败"),
    /**
     * 用户地址为空，需要先添加或选择收货地址
     */
    ADDRESS_EMPTY(40002, "请先选择收货地址");







    private Integer code;

    private String message;

     GoodsCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }



}
