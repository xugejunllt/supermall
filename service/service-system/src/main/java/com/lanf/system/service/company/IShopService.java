package com.lanf.system.service.company;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.system.model.entiry.ShopDO;
import com.lanf.system.model.query.ShopPageQuery;
import com.lanf.system.model.vo.ShopVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-28
 */
public interface IShopService extends IService<ShopDO> {

    PageResult<ShopDO> shopPage(ShopPageQuery query);

    List<ShopVO> shopQuery(List<Long> idList);

    Long getPlatformShopId();

    String getTenantCodeByShopId(Long shopId);
}
