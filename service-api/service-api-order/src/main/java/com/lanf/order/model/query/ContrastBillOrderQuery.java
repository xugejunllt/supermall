package com.lanf.order.model.query;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ContrastBillOrderQuery  {

    private String createTimeFormat;

    protected long page = 1;

    protected long pageSize = 2000;

}
