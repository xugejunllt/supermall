package com.lanf.search.model.query;

import lombok.Data;
import org.springframework.data.elasticsearch.core.query.BaseQuery;

@Data
public class GoodsPageQuery extends BasePageQuery {

    //搜索词
    private String searchWords;

}
