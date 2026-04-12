package com.lanf.pay.model.enums;

import lombok.Getter;

@Getter
public enum CompensatePaymentStatusEnum {

    CONTINUE(0, "继续补投"),
    SUCCESS(1, "补投成功"),
    FINISH(2, "结束补投");

    private final Integer code;
    private final String description;

    CompensatePaymentStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}
