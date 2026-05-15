package com.lanf.search.model.query;


import lombok.Data;
import java.io.Serializable;

@Data
public class SuggestQuery implements Serializable {
    
    /**
     * 用户输入的搜索前缀/关键词
     */
    private String prefix;
    
    /**
     * 建议数量，默认 10
     */
    private Integer size = 10;
    
    /**
     * 分类ID（可选），用于限定某个分类下的建议词
     */
    private Long categoryId;
}
