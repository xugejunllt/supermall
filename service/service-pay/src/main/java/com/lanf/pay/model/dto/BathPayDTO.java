package com.lanf.pay.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BathPayDTO implements Serializable {


   private Integer payType;
   private Long mainOrderId;


}
