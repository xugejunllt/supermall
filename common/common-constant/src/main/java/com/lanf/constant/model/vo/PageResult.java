package com.lanf.constant.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {

    private List<T> records;

    private long total;

    private long size;

    public PageResult() {
    }

    public PageResult(List<T> records,  long size,long total) {
        this.records = records;
        this.total = total;
        this.size = size;
    }

    public PageResult(List<T> records) {
        this.records = records;
    }


    public static <T>  PageResult<T> emptyResult(){
        return new PageResult<>(new ArrayList<>());
    }


}
