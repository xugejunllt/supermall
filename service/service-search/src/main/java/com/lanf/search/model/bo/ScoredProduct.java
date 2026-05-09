package com.lanf.search.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScoredProduct implements Serializable {
    private Long goodsId;
    private double totalScore; // 最终综合得分
    private double textScore;  // fx1: 文本匹配得分
    private double qualityScore; // fx2: 商品质量得分
    private double comboScore;   // fx3: 组合/行为得分
    
    // 重排标记
    private boolean isAd;        // 是否广告
    private boolean isNew;       // 是否新品
}
