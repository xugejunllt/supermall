package com.lanf.search.model.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestVO implements Serializable {
    
    /**
     * 建议词文本
     */
    private String text;
    
    /**
     * 建议词类型：keyword-关键词, prompt-提示词标签, category-分类名称
     */
    private String type;
    
    /**
     * 相关商品数量（热度指标）
     */
    private Long count;
    
    /**
     * 排序权重/得分
     */
    private Double score;
}
