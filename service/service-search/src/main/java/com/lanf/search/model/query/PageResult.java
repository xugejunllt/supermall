package com.lanf.search.model.query;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {

    private List<T> records;

    private long total;

    private long size;

    public PageResult(List<T> records, long total, long size) {
        this.records = records;
        this.total = total;
        this.size = size;
    }
}
