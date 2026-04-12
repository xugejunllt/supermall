package com.lanf.pay.model.dto;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
public class CancelTradeOrderDTO implements Serializable {

    @NonNull
    private Long orderId;
}
