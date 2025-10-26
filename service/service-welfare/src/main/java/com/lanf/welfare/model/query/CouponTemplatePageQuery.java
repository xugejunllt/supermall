package com.lanf.welfare.model.query;

import com.lanf.mybatis.base.PageQuery;
import lombok.Data;

@Data
public class CouponTemplatePageQuery extends PageQuery {

    private Long shopId;
}
