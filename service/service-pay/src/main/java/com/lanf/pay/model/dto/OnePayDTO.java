package com.lanf.pay.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OnePayDTO implements Serializable {


   private Integer payType;

   private Long orderId;


}
