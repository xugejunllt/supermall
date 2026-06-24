package com.lanf.dynamicsrrefresh.service.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {

    private List<T> records;

    private long total;

    private long size;

    public PageResult() {
    }

    public PageResult(List<T> records) {
        this.records = records;
    }


}
