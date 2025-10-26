package com.lanf.storage.service.purchase;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.dto.CalculatePurchaseOrderMoneyDTO;
import com.lanf.storage.model.dto.PurchaseOrderAddDTO;
import com.lanf.storage.model.entity.PurchaseOrderDO;
import com.lanf.storage.model.query.PurchaseOrderPageQuery;
import com.lanf.storage.model.vo.CalculatePurchaseOrderMoneyVO;
import com.lanf.storage.model.vo.PurchaseOrderDetailVO;
import com.lanf.storage.model.vo.PurchaseOrderPageVO;

/**
 * <p>
 * 采购单 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
public interface IPurchaseOrderService extends IService<PurchaseOrderDO> {


     void  purchaseOrderAdd(PurchaseOrderAddDTO purchaseOrderAdd);
     CalculatePurchaseOrderMoneyVO calculatePurchaseOrderMoney(CalculatePurchaseOrderMoneyDTO calculatePurchaseOrderMoney);

     PageResult<PurchaseOrderPageVO> purchaseOrderPage(PurchaseOrderPageQuery query);

     PurchaseOrderDetailVO purchaseOrderDetail(Long id);

     void review(Long id,Integer status);

}
