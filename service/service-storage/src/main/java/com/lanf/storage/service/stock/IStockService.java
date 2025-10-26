package com.lanf.storage.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.entity.StockDO;
import com.lanf.storage.model.query.StockPageQuery;
import com.lanf.storage.model.vo.StockPageQueryVO;
import com.lanf.storage.model.vo.StockVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-07
 */
public interface IStockService extends IService<StockDO> {

    PageResult<StockPageQueryVO> stockPage(StockPageQuery query);


    List<StockVO> querySkuCodeList(List<String> skuCodeList);

}
