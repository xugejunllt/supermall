package com.lanf.storage.service.purchase;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.api.storage.model.dto.AddPurchaseOrderDTO;
import com.lanf.storage.model.entity.PurchaseOrderDO;
import com.lanf.api.storage.model.query.PurchaseOrderPageQuery;
import com.lanf.api.storage.model.vo.PurchaseOrderDetailVO;
import com.lanf.api.storage.model.vo.PurchaseOrderPageVO;

/**
 * <p>
 * 采购单 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
public interface IPurchaseOrderService extends IService<PurchaseOrderDO> {


     void  addPurchaseOrder(AddPurchaseOrderDTO purchaseOrderAdd);

     PageResult<PurchaseOrderPageVO> purchaseOrderPageQuery(PurchaseOrderPageQuery query);

     PurchaseOrderDetailVO purchaseOrderDetailQuery(Long id);

     /**
      * 审核通过
      * @param id
      */
     void auditApprove(Long id);

}
