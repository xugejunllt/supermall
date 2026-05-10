package com.lanf.storage.service.storage;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.web.PageResult;
import com.lanf.storage.model.dto.InStockDTO;
import com.lanf.storage.model.entity.PurchaseInStockOrderDO;
import com.lanf.storage.model.query.PurchaseInStockOrderPageQuery;
import com.lanf.storage.model.vo.PurchaseInStockOrderDetailVO;
import com.lanf.storage.model.vo.PurchaseInStockOrderPageVO;

/**
 * <p>
 * 采购入库单 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
public interface IPurchaseInStockOrderService extends IService<PurchaseInStockOrderDO> {

        void inStock(InStockDTO inStorageDTO);
        PageResult<PurchaseInStockOrderPageVO> purchaseInStockOrderPage(PurchaseInStockOrderPageQuery query);
        PurchaseInStockOrderDetailVO purchaseInStockOrderDetail(Long id);
}
