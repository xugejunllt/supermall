package com.lanf.system.model.query;

import com.lanf.constant.web.PageQuery;
import lombok.Data;

@Data
public class CompanyPageQuery extends PageQuery {

    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 手机号码
     */
    private String phoneNumber;

}
