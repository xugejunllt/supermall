package com.lanf.order.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CommentGoodsItemVO implements Serializable {


    private Long orderId;

    private List<CommentGoodsItemDetailVO> commentGoodsItemDetailVOList;

}
