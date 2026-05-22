package com.lanf.order.service.shipping;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.order.model.bo.AddExpressDTO;
import com.lanf.order.model.entity.ExpressDO;
import com.lanf.order.model.vo.ExpressPageVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
public interface IExpressService extends IService<ExpressDO> {

    void  addExpress(AddExpressDTO dto);

    PageResult<ExpressPageVO> expressPageQuery(PageQuery query);


}
