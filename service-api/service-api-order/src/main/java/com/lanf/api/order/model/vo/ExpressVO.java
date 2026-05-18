package com.lanf.api.order.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExpressVO implements Serializable {

    /**
     * 快递编码
     */
    private String code;

    /**
     * 快递名称
     */
    private String expressName;

    /**
     * 快递公司
     */
    private String expressCompany;

    /**
     * 省
     */
    private String province;


}
