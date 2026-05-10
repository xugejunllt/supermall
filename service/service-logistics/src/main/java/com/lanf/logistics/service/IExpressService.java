package com.lanf.logistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.logistics.model.dto.ExpressAddDTO;
import com.lanf.logistics.model.entity.ExpressDO;
import com.lanf.constant.web.PageQuery;
import com.lanf.constant.web.PageResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
public interface IExpressService extends IService<ExpressDO> {

    void  expressAdd(ExpressAddDTO dto);
    PageResult<ExpressDO> expressPage(PageQuery query);


}
