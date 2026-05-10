package com.lanf.constant.model.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageQuery implements Serializable {

    protected long page = 1;

    protected long pageSize = 20;

}
