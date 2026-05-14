package com.lanf.storage.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.storage.model.entity.StockDO;
import com.lanf.api.storage.model.query.StockPageQuery;
import com.lanf.api.storage.model.vo.StockPageQueryVO;
import com.lanf.api.storage.model.vo.StockVO;

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

    PageResult<StockPageQueryVO> stockPageQuery(StockPageQuery query);


    List<StockVO> querySkuCodeList(List<String> skuCodeList);

}
