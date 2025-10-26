package com.lanf.storage.service.purchase.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.security.utils.UserUtil;
import com.lanf.storage.mapper.PurchaseOrderMapper;
import com.lanf.storage.model.bo.CalculatePurchaseOrderMoneyBO;
import com.lanf.storage.model.bo.PurchaseOrderItemAddBO;
import com.lanf.storage.model.dto.CalculatePurchaseOrderItemMoneyDTO;
import com.lanf.storage.model.dto.CalculatePurchaseOrderMoneyDTO;
import com.lanf.storage.model.dto.PurchaseOrderAddDTO;
import com.lanf.storage.model.dto.PurchaseOrderItemAddDTO;
import com.lanf.storage.model.entity.*;
import com.lanf.storage.model.query.PurchaseOrderPageQuery;
import com.lanf.storage.model.vo.CalculatePurchaseOrderMoneyVO;
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
import com.lanf.web.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public void purchaseOrderAdd(PurchaseOrderAddDTO purchaseOrderAdd) {

        //进行校验
        purchaseOrderAddCheck(purchaseOrderAdd);

        //构建PurchaseOrderItemAddBO
        List<PurchaseOrderItemAddBO> purchaseOrderItemAddBOList = buildPurchaseOrderItemAddBO(purchaseOrderAdd.getPurchaseOrderItemAdd(),purchaseOrderAdd);

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


    private void purchaseOrderAddCheck(PurchaseOrderAddDTO purchaseOrderAdd) {

        SupplierDO supplier = supplierService.getById(purchaseOrderAdd.getSupplierId());
        if (supplier == null) {
            throw new BizException("供应商不存在");
        }
    }

    private PurchaseOrderDO buildPurchaseOrderDO(PurchaseOrderAddDTO purchaseOrderAdd) {

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

    private List<PurchaseOrderItemAddBO> buildPurchaseOrderItemAddBO(List<PurchaseOrderItemAddDTO> purchaseOrderItemAddList,PurchaseOrderAddDTO purchaseOrderAdd) {

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

    /**
     * 计算采购单各项金额
     *
     * @
     */
    @Override
    public CalculatePurchaseOrderMoneyVO calculatePurchaseOrderMoney(CalculatePurchaseOrderMoneyDTO calculatePurchaseOrderMoney) {

        CalculatePurchaseOrderMoneyBO calculatePurchaseOrderMoneyBO = storageAdapter.calculatePurchaseOrderMoney(calculatePurchaseOrderMoney);
        CalculatePurchaseOrderMoneyVO calculatePurchaseOrderMoneyVO = new CalculatePurchaseOrderMoneyVO();
        calculatePurchaseOrderMoneyVO.setCalculatePurchaseOrderMoney(calculatePurchaseOrderMoneyBO);

        return calculatePurchaseOrderMoneyVO;
    }

    @Override
    public PageResult<PurchaseOrderPageVO> purchaseOrderPage(PurchaseOrderPageQuery query) {

        IPage<PurchaseOrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<PurchaseOrderDO> purchaseOrderPage = this.lambdaQuery().
                eq(query.getStatus() != null, PurchaseOrderDO::getStatus, query.getStatus()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (purchaseOrderPage.getRecords().isEmpty()) {

            return PageResult.emptyResult(PurchaseOrderPageVO.class);
        }

        PageResult<PurchaseOrderPageVO> pageResult = PageResult.toPageResult(page, PurchaseOrderPageVO.class);

        List<PurchaseOrderDO> records = purchaseOrderPage.getRecords();
        /**
         * 填充关联属性
         */
        //用set接收 去重
        Set<Long> supplierIdList = records.stream().map(PurchaseOrderDO::getSupplierId).collect(Collectors.toSet());
        Map<Long, SupplierDO> supplierMap = supplierService.lambdaQuery().in(SupplierDO::getId, supplierIdList).list().stream().
                collect(Collectors.toMap(SupplierDO::getId, Function.identity()));


        pageResult.getRecords().forEach(vo -> {
            vo.setSupplierName(supplierMap.get(vo.getSupplierId()).getName());
        });

        return pageResult;
    }

    @Override
    public PurchaseOrderDetailVO purchaseOrderDetail(Long id) {

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
     * 审核
     */
    @Transactional
    @Override
    public void review(Long id, Integer status) {

        PurchaseOrderDO purchaseOrderDO = this.getById(id);
        if (purchaseOrderDO == null) {
            throw new BizException("采购单不存在");
        }
        if (purchaseOrderDO.getStatus()!=0){
            throw new BizException("重复审核");
        }

        if (!(status == 1 || status == 2)) {
            throw new BizException("不支持的状态");
        }
        if (status == 1) {
            //审核通过

            /**
             * 1.更新采购单状态
             * 2.生成采购入库单
             * 3.生成入库单商品明细
             */
            WarehouseDO defaultWarehouse = storageManagerService.getDefaultWarehouse();
            //预计入库数量
            Integer expectStorageQuantity = findItemTotalQuantity(id);

            PurchaseInStockOrderDO purchaseStorageOrderSave = new PurchaseInStockOrderDO();
            purchaseStorageOrderSave.setCode(CodeGenerateUtils.generaCode());
            purchaseStorageOrderSave.setPurchaseOrderId(id);
            purchaseStorageOrderSave.setExpectStorageQuantity(expectStorageQuantity);
            purchaseStorageOrderSave.setStorageStatus(0);
            purchaseStorageOrderSave.setSupplierId(purchaseOrderDO.getSupplierId());
            purchaseStorageOrderSave.setWarehouseId(defaultWarehouse.getId());

            //
            List<InOutStockOrderItemDO> storageOrderItemDetailsList = buildStorageOrderItemDetailsDO(id);
            //保存
            purchaseStorageOrderService.save(purchaseStorageOrderSave);
            storageOrderItemDetailsList.forEach(a->{
                a.setInOutStockOrderId(purchaseStorageOrderSave.getId());
            });
            storageOrderItemDetailsService.saveBatch(storageOrderItemDetailsList);
            //更新采购单状态
            purchaseOrderUpdate(id, status);
        }
        if (status == 2) {
            //审核不通过
            purchaseOrderUpdate(id, status);
        }

    }

    private void purchaseOrderUpdate(Long id, Integer status) {

        PurchaseOrderDO purchaseOrderUpdate = new PurchaseOrderDO();
        purchaseOrderUpdate.setId(id);
        purchaseOrderUpdate.setStatus(status);
        purchaseOrderUpdate.setReviewTime(new Date());
        purchaseOrderUpdate.setReviewer(UserUtil.getUserInfo().getName());
        this.updateById(purchaseOrderUpdate);
    }

    /**
     * 查找商品总数量
     *
     * @param id
     */
    private Integer findItemTotalQuantity(Long id) {

        List<PurchaseOrderItemDO> purchaseOrderItemList = purchaseOrderItemService.lambdaQuery().
                eq(PurchaseOrderItemDO::getPurchaseOrderId, id).list();
        if (purchaseOrderItemList.isEmpty()) {
            throw new BizException("采购单商品不存在");
        }
        Integer expectStorageQuantity = 0;
        for (PurchaseOrderItemDO it : purchaseOrderItemList) {
            expectStorageQuantity += it.getQuantity();
        }
        return expectStorageQuantity;

    }

    private List<InOutStockOrderItemDO> buildStorageOrderItemDetailsDO(Long id) {

        List<PurchaseOrderItemDO> purchaseOrderItemList = purchaseOrderItemService.lambdaQuery().
                eq(PurchaseOrderItemDO::getPurchaseOrderId, id).list();
        if (purchaseOrderItemList.isEmpty()) {
            throw new BizException("采购单商品不存在");
        }
        List<InOutStockOrderItemDO> storageOrderItemDetailsDOList = new ArrayList<>(purchaseOrderItemList.size());
        purchaseOrderItemList.forEach(a -> {

            InOutStockOrderItemDO storageOrderItemDetailsDO = new InOutStockOrderItemDO();
            //待修改 远程调用查询商品名称
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
