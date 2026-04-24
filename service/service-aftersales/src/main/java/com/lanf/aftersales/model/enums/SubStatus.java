package com.lanf.aftersales.model.enums;

import lombok.Getter;

/**
 * 每个状态 对应一种业务场景动作
 *
 * 拆分业务场景
 *
 * 一个大状态 支持多种子状态->动作
 */
@Getter
public enum SubStatus {
    // 待审核下的子状态
    WAIT_MANUAL(0, "待人工审核", MainStatusEnum.WAIT_SELLER_AGREE),
    AUTO_PASS(1, "系统自动通过", MainStatusEnum.WAIT_SELLER_AGREE),
    RISK_CHECK(2, "风控复核中", MainStatusEnum.WAIT_SELLER_AGREE),

    // 待买家退货下的子状态
    WAIT_LOGISTICS(3, "待上传物流单号", MainStatusEnum.WAIT_BUYER_RETURN),

    // 待收货下的子状态
    NO_SIGN(4, "待签收", MainStatusEnum.WAIT_SELLER_RECEIVE),
    SIGNED(5, "已签收待入库", MainStatusEnum.WAIT_SELLER_RECEIVE),
//    QUALITY_CHECK(6, "已入库", MainStatusEnum.WAIT_SELLER_RECEIVE),

    // 待退款/换货下的子状态
    REFUND_PROCESS(7, "退款处理中", MainStatusEnum.WAIT_CONFIRM),
    REFUND_FAILED(8, "退款失败", MainStatusEnum.WAIT_CONFIRM),
    REPLACE_SHIP(9, "换货出库中", MainStatusEnum.WAIT_CONFIRM),
    REPLACE_DELIVERED(10, "换货已发出", MainStatusEnum.WAIT_CONFIRM),

    // 已完成下的子状态
    REFUND_DONE(11, "退款完成", MainStatusEnum.SUCCESS),
    REPLACE_DONE(12, "换货完成", MainStatusEnum.SUCCESS),

    // 已关闭下的子状态
    USER_CANCEL(13, "用户主动取消", MainStatusEnum.CLOSED),
    REJECT(14, "审核拒绝", MainStatusEnum.CLOSED),
    TIME_OUT(15, "超时关闭", MainStatusEnum.CLOSED);

    private final Integer code;
    private final String desc;
    private final MainStatusEnum mainStatus;

    SubStatus(Integer code, String desc, MainStatusEnum mainStatus) {
        this.code = code;
        this.desc = desc;
        this.mainStatus = mainStatus;
    }

    /**
     * 根据code获取子状态枚举
     */
    public static SubStatus fromCode(String code) {
        for (SubStatus sub : values()) {
            if (sub.code.equals(code)) {
                return sub;
            }
        }
        throw new IllegalArgumentException("Unknown SubStatus code: " + code);
    }
}