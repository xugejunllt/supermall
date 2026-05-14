package com.lanf.storage.service.purchase.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.storage.mapper.PurchaseOrderMapper;
import com.lanf.storage.model.bo.CalculatePurchaseOrderMoneyBO;
import com.lanf.storage.model.bo.PurchaseOrderItemAddBO;
import com.lanf.storage.model.dto.AddPurchaseOrderDTO;
import com.lanf.storage.model.dto.CalculatePurchaseOrderItemMoneyDTO;
import com.lanf.storage.model.dto.CalculatePurchaseOrderMoneyDTO;
import com.lanf.storage.model.dto.PurchaseOrderItemAddDTO;
import com.lanf.storage.model.entity.*;
import com.lanf.storage.model.query.PurchaseOrderPageQuery;
import com.lanf.storage.model.vo.PurchaseOrderDetailVO;
import com.lanf.storage.model.vo.PurchaseOrderPageVO;
import com.lanf.storage.service.manager.StorageManagerService;
import com.lanf.storage.service.purchase.IPurchaseOrderItemService;
import com.lanf.storage.service.purchase.IPurchaseOrderService;
import com.lanf.storage.service.storage.IInOutStockOrderItemService;
import com.lanf.storage.service.storage.IPurchaseInStockOrderService;
import com.lanf.storage.service.storage.impl.manager.StorageAdapter;
import com.lanf.storage.service.supplier.ISupplierService;
import com.lanf.storage.service.warehous.IWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 采购单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrderDO> implements IPurchaseOrderService {

    @Autowired
    private IWarehouseService warehouseService;

    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private IPurchaseOrderItemService purchaseOrderItemService;
    @Autowired
    private StorageAdapter storageAdapter;
    @Autowired
    private IPurchaseInStockOrderService purchaseStorageOrderService;
    @Autowired
    private IInOutStockOrderItemService storageOrderItemDetailsService;
    @Autowired
    private StorageManagerService storageManagerService;

    @Transactional
    @Override
    public void addPurchaseOrder(AddPurchaseOrderDTO purchaseOrderAdd) {

        //进行校验
        purchaseOrderAddCheck(purchaseOrderAdd);

        //构建PurchaseOrderItemAddBO
        List<PurchaseOrderItemAddBO> purchaseOrderItemAddBOList = buildPurchaseOrderItemAddBO(purchaseOrderAdd.getPurchaseOrderItemAdd(), purchaseOrderAdd);

        //构建PurchaseOrderDO
        PurchaseOrderDO purchaseOrder = buildPurchaseOrderDO(purchaseOrderAdd);

        /**
         * 进行保存
         */
        this.save(purchaseOrder);
        List<PurchaseOrderItemDO> purchaseOrderItemList = BeanCopyUtils.copyBeanList(purchaseOrderItemAddBOList, PurchaseOrderItemDO.class);
        //添加 purchaseOrderId
        purchaseOrderItemList.forEach(p -> p.setPurchaseOrderId(purchaseOrder.getId()));
        purchaseOrderItemService.saveBatch(purchaseOrderItemList);
    }


    private void purchaseOrderAddCheck(AddPurchaseOrderDTO purchaseOrderAdd) {

        SupplierDO supplier = supplierService.getById(purchaseOrderAdd.getSupplierId());
        if (supplier == null) {
            throw new BizException("供应商不存在");
        }
    }

    private PurchaseOrderDO buildPurchaseOrderDO(AddPurchaseOrderDTO purchaseOrderAdd) {

        PurchaseOrderDO purchaseOrder = new PurchaseOrderDO();

        BeanCopyUtils.copy(purchaseOrderAdd, purchaseOrder);
        //设置单号
        purchaseOrder.setCode(CodeGenerateUtils.generaCode());
        //设置状态
        purchaseOrder.setStatus(0);
        //总计金额
        CalculatePurchaseOrderMoneyDTO calculatePurchaseOrderMoney = new CalculatePurchaseOrderMoneyDTO();
        BeanCopyUtils.copy(purchaseOrderAdd, calculatePurchaseOrderMoney);
        List<CalculatePurchaseOrderItemMoneyDTO> calculatePurchaseOrderItemMoneyList =
                BeanCopyUtils.copyBeanList(purchaseOrderAdd.getPurchaseOrderItemAdd(), CalculatePurchaseOrderItemMoneyDTO.class);
        calculatePurchaseOrderMoney.setPurchaseOrderItemMoneyList(calculatePurchaseOrderItemMoneyList);
        CalculatePurchaseOrderMoneyBO calculatePurchaseOrderMoneyBO = storageAdapter.calculatePurchaseOrderMoney(calculatePurchaseOrderMoney);
        BigDecimal totalMoney = calculatePurchaseOrderMoneyBO.getPurchaseOrderTotalMoney();
        purchaseOrder.setTotalMoney(totalMoney);

        return purchaseOrder;

    }

    private List<PurchaseOrderItemAddBO> buildPurchaseOrderItemAddBO(List<PurchaseOrderItemAddDTO> purchaseOrderItemAddList, AddPurchaseOrderDTO purchaseOrderAdd) {

        List<PurchaseOrderItemAddBO> purchaseOrderItemAddBOList =
                BeanCopyUtils.copyBeanList(purchaseOrderItemAddList, PurchaseOrderItemAddBO.class);

        for (PurchaseOrderItemAddBO purchaseOrderItemAdd : purchaseOrderItemAddBOList) {

            /**
             * 计算个项商品总金额
             */
            CalculatePurchaseOrderItemMoneyDTO purchaseOrderItemMoney = new CalculatePurchaseOrderItemMoneyDTO();
            BeanCopyUtils.copy(purchaseOrderItemAdd, purchaseOrderItemMoney);
            BigDecimal totalMoney = storageAdapter.calculateItemTotalMoney(purchaseOrderItemMoney);
            purchaseOrderItemAdd.setTotalMoney(totalMoney);

        }

        return purchaseOrderItemAddBOList;

    }

    /**
     * 根据sku编码编从商品基础数据服务查询出商品信息
     *
     * @param goodsCodes
     * @return
     */

    private String queryByGoodsSkuCode(List<String> goodsCodes) {

        //这里是远程调用 后续实现远程调用接口

        return "";
    }



    @Override
    public PageResult<PurchaseOrderPageVO> purchaseOrderPageQuery(PurchaseOrderPageQuery query) {

        IPage<PurchaseOrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<PurchaseOrderDO> purchaseOrderPage = this.lambdaQuery().
                eq(query.getStatus() != null, PurchaseOrderDO::getStatus, query.getStatus()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

//        if (purchaseOrderPage.getRecords().isEmpty()) {
//
//            return PageResult.emptyResult(PurchaseOrderPageVO.class);
//        }
//
//        PageResult<PurchaseOrderPageVO> pageResult = PageResult.toPageResult(page, PurchaseOrderPageVO.class);
//
//        List<PurchaseOrderDO> records = purchaseOrderPage.getRecords();
//        /**
//         * 填充关联属性
//         */
//        //用set接收 去重
//        Set<Long> supplierIdList = records.stream().map(PurchaseOrderDO::getSupplierId).collect(Collectors.toSet());
//        Map<Long, SupplierDO> supplierMap = supplierService.lambdaQuery().in(SupplierDO::getId, supplierIdList).list().stream().
//                collect(Collectors.toMap(SupplierDO::getId, Function.identity()));
//
//
//        pageResult.getRecords().forEach(vo -> {
//            vo.setSupplierName(supplierMap.get(vo.getSupplierId()).getName());
//        });

        return null;
    }

    @Override
    public PurchaseOrderDetailVO purchaseOrderDetailQuery(Long id) {

        PurchaseOrderDO purchaseOrder = this.getById(id);

        if (purchaseOrder == null) {
            throw new BizException("数据不存在");
        }
        PurchaseOrderDetailVO purchaseOrderDetailVO = new PurchaseOrderDetailVO();
        BeanCopyUtils.copy(purchaseOrder, purchaseOrderDetailVO);
        /**
         * 填充其他属性
         */
        SupplierDO supplier = supplierService.getById(purchaseOrderDetailVO.getSupplierId());
        if (supplier == null) {
            throw new BizException("供应商不存在");
        }
        Integer totalQuantity = findItemTotalQuantity(id);
        purchaseOrderDetailVO.setTotalQuantity(totalQuantity);
        purchaseOrderDetailVO.setSupplierName(supplier.getName());
        //填充商品item
        List<PurchaseOrderItemDO> list = purchaseOrderItemService.lambdaQuery().eq(PurchaseOrderItemDO::getPurchaseOrderId, id).list();
        purchaseOrderDetailVO.setPurchaseOrderItemList(list);

        return purchaseOrderDetailVO;
    }

    /**
     * 审核通过
     */
    @Transactional
    @Override
    public void auditApprove(Long id) {

        validateAuditApprove(id);
        /**
         * 1.更新采购单状态
         * 2.生成采购入库单
         * 3.生成入库单商品明细
         */

        PurchaseInStockOrderDO purchaseStorageOrderSave = buildPurchaseInStockOrderDO(id);
        List<InOutStockOrderItemDO> storageOrderItemDetailsList = buildStorageOrderItemDetailsDO(id);
        PurchaseOrderDO purchaseOrderDO = buildPurchaseOrderUpdate(id);

        //更新采购单状态 这里加version乐观锁更新
        this.updateById(purchaseOrderDO);
        //保存采购入库单
        purchaseStorageOrderService.save(purchaseStorageOrderSave);
        storageOrderItemDetailsList.forEach(a -> {
            a.setInOutStockOrderId(purchaseStorageOrderSave.getId());
        });
        storageOrderItemDetailsService.saveBatch(storageOrderItemDetailsList);

    }

    private PurchaseInStockOrderDO buildPurchaseInStockOrderDO(Long id){

        //预计入库数量
        Integer expectStorageQuantity = findItemTotalQuantity(id);

        PurchaseInStockOrderDO purchaseStorageOrderSave = new PurchaseInStockOrderDO();
        purchaseStorageOrderSave.setCode(CodeGenerateUtils.generaCode());
        purchaseStorageOrderSave.setPurchaseOrderId(id);
        purchaseStorageOrderSave.setExpectStorageQuantity(expectStorageQuantity);
        purchaseStorageOrderSave.setStorageStatus(0);

        return  purchaseStorageOrderSave;
    }
    private void validateAuditApprove(Long id) {

        PurchaseOrderDO purchaseOrderDO = this.getById(id);
        if (purchaseOrderDO == null) {
            throw new BizException("采购单不存在");
        }
        if (purchaseOrderDO.getStatus() != 0) {
            throw new BizException("重复审核");
        }


    }

    private PurchaseOrderDO buildPurchaseOrderUpdate(Long id) {

        PurchaseOrderDO purchaseOrderUpdate = new PurchaseOrderDO();
        purchaseOrderUpdate.setId(id);
        purchaseOrderUpdate.setStatus(1);
        purchaseOrderUpdate.setReviewTime(new Date());
        return purchaseOrderUpdate;
    }


    /**
     * 查找商品总数量
     *
     * @param id
     */
    private Integer findItemTotalQuantity(Long id) {

        List<PurchaseOrderItemDO> purchaseOrderItemList = purchaseOrderItemService.lambdaQuery().
                eq(PurchaseOrderItemDO::getPurchaseOrderId, id).list();

        Integer expectStorageQuantity = 0;
        for (PurchaseOrderItemDO it : purchaseOrderItemList) {
            expectStorageQuantity += it.getQuantity();
        }
        return expectStorageQuantity;

    }

    private List<InOutStockOrderItemDO> buildStorageOrderItemDetailsDO(Long id) {

        List<PurchaseOrderItemDO> purchaseOrderItemList = purchaseOrderItemService.lambdaQuery().
                eq(PurchaseOrderItemDO::getPurchaseOrderId, id).list();

        List<InOutStockOrderItemDO> storageOrderItemDetailsDOList = new ArrayList<>(purchaseOrderItemList.size());
        purchaseOrderItemList.forEach(a -> {

            InOutStockOrderItemDO storageOrderItemDetailsDO = new InOutStockOrderItemDO();
            storageOrderItemDetailsDO.setGoodsName(a.getGoodsName());
            storageOrderItemDetailsDO.setSkuCode(a.getSkuCode());
            storageOrderItemDetailsDO.setTotalQuantity(a.getQuantity());
            storageOrderItemDetailsDO.setSurplusQuantity(a.getQuantity());
            storageOrderItemDetailsDO.setUnit(a.getUnit());
            storageOrderItemDetailsDOList.add(storageOrderItemDetailsDO);
        });

        return storageOrderItemDetailsDOList;

    }


}
