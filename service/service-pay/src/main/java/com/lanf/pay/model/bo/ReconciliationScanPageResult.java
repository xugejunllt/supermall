package com.lanf.pay.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ReconciliationScanPageResult<T> implements Serializable {

   private List<String> outTradeNoList;


    private  List<T>  dataList;

    private long  pages;
}
