package com.lanf.search.model.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class BasePageQuery implements Serializable {

    protected int page = 1;

    protected int pageSize = 20;


}
