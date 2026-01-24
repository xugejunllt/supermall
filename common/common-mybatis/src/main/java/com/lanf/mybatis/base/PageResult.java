package com.lanf.mybatis.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.common.utils.BeanCopyUtils;
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

    public static <T> PageResult<T> toPageResult(IPage<T> result) {

        PageResult<T> result1 = new PageResult<>();
        result1.setRecords(result.getRecords());
        result1.setTotal(result.getTotal());
        result1.setSize(result.getSize());

        return result1;
    }
    public static <T,S> PageResult<S> toPageResult(IPage<T> result,Class<S> tClass) {
        PageResult<S> result1 = new PageResult<>();
        List<S> s = BeanCopyUtils.copyBeanList(result.getRecords(), tClass);
        result1.setRecords(s);
        result1.setTotal(result.getTotal());
        result1.setSize(result.getSize());

        return result1;
    }
    public static <T,S> PageResult<S> toPageResult(IPage<T> result,List<S> records) {
        PageResult<S> result1 = new PageResult<>();
        result1.setRecords(records);
        result1.setTotal(result.getTotal());
        result1.setSize(result.getSize());

        return result1;
    }


    public static <T,S> PageResult<S> toPageResultNotCopy(IPage<T> result,Class<S> tClass) {
        PageResult<S> result1 = new PageResult<>();
        List<S> s = new ArrayList<>();
        result1.setRecords(s);
        result1.setTotal(result.getTotal());
        result1.setSize(result.getSize());

        return result1;
    }

    @Deprecated
    public static <T>  PageResult<T> emptyResult(Class<T> tClass){
        return new PageResult<>(new ArrayList<>());
    }
    public static <T>  PageResult<T> emptyResult(){
        return new PageResult<>(new ArrayList<>());
    }
    public static <T> IPage<T> buildIPage(PageQuery pageQuery,Class<T> tClass) {



        return  new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
    }

}
