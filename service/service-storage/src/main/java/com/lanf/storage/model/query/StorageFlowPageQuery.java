package com.lanf.storage.model.query;

import com.lanf.mybatis.base.PageQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StorageFlowPageQuery extends PageQuery {

    private String bizNumber;

}
