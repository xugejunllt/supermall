package com.lanf.order.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class OnePlaceAnOrderBO implements Serializable {

   private Long mainOrderId;
   private Long bizOrderId;
   private  Long userId;
}
