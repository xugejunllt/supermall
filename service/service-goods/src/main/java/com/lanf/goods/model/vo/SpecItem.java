package com.lanf.goods.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class SpecItem {
    private String name;      // 属性名，如“颜色”
    private List<String> values; // 属性值集合，如["白色", "黑色"]
}
