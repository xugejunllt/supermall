package com.lanf.storage.service.storage.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.security.utils.UserUtils;
import com.lanf.storage.mapper.PurchaseInStockOrderMapper;
import com.lanf.storage.model.bo.StockSaveOrUpdateBO;
import com.lanf.storage.model.bo.StockUpdateBO;
import com.lanf.storage.model.dto.InStockDTO;
import com.lanf.storage.model.dto.InStockItemDTO;
import com.lanf.storage.model.entity.*;
import com.lanf.storage.model.query.PurchaseInStockOrderPageQuery;
import com.lanf.storage.model.vo.PurchaseInStockOrderDetailVO;
import com.lanf.storage.model.vo.PurchaseInStockOrderItemDetailVO;
import com.lanf.storage.model.vo.PurchaseInStockOrderPageVO;
import com.lanf.storage.service.purchase.IPurchaseOrderService;
import com.lanf.storage.service.stock.IStockFlowService;
import com.lanf.storage.service.stock.IStockService;
import com.lanf.storage.service.storage.IInOutStockOrderItemService;
import com.lanf.storage.service.storage.IPurchaseInStockOrderService;
import com.lanf.storage.service.storage.IStorageFlowService;
import com.lanf.storage.service.supplier.ISupplierService;
import com.lanf.storage.service.warehous.IWarehouseService;
import com.lanf.web.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购入库单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Service
public class PurchaseInStockOrderServiceImpl extends ServiceImpl<PurchaseInStockOrderMapper, PurchaseInStockOrderDO> implements IPurchaseInStockOrderService {

    @Autowired
    private IInOutStockOrderItemService storageOrderItemDetailsService;
    @Autowired
    private IWarehouseService warehouseService;
    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private IStorageFlowService storageDetailsService;
    @Autowired
    private IStockFlowService stockFlowService;
    @Autowired
    private IStockService stockService;

    @Lazy
    @Autowired
    private IPurchaseOrderService purchaseOrderService;

    /**
     * 入库
     * 用分布式锁 避免库存更新并非
     */
    @Transactional
    @Override
    public void inStock(InStockDTO inStorageDTO) {

        Long purchaseStorageOrderId = inStorageDTO.getPurchaseInStockOrderId();
        List<InStockItemDTO> inStorageItemList = inStorageDTO.getInStorageItemList();
        PurchaseInStockOrderDO storageOrderDO = this.getById(purchaseStorageOrderId);

        List<InOutStockOrderItemDO> storageOrderItemDetailList = storageOrderItemDetailsService.lambdaQuery().
                eq(InOutStockOrderItemDO::getInOutStockOrderId, inStorageDTO.getPurchaseInStockOrderId()).list();
        Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap = storageOrderItemDetailList.stream()
                .collect(Collectors.toMap(InOutStockOrderItemDO::getId, Function.identity()));
        //校验
        inStorageCheck(storageOrderDO, inStorageItemList, inStorageDTO);
        //构建更新StorageOrderItemDetailsDO
        List<InOutStockOrderItemDO> storageOrderItemDetailsDOUpdate = buildStorageOrderItemDetailsDO(inStorageItemList,
                purchaseOrderItemDOMap);
        //入库总数量
        Integer enterQuantity = 0;
        for (InStockItemDTO is : inStorageItemList) {
            enterQuantity += is.getActualQuantity();
        }
        WarehouseDO warehouseDO = warehouseService.getById(storageOrderDO.getWarehouseId());
        SupplierDO supplierDO = supplierService.getById(storageOrderDO.getSupplierId());
        //生成入库明细
        StorageFlowDO storageDetailsDO = buildStorageDetailsDO(warehouseDO, storageOrderDO, enterQuantity);
        //生成库存流水
        List<StockFlowDO> stockFlowList = buildStockFlowDO(inStorageItemList, purchaseOrderItemDOMap, storageOrderDO, warehouseDO);
        //更新采购单状态
        PurchaseOrderDO purchaseOrderDOUpdate = buildPurchaseOrderDOUpdate(storageOrderDO, enterQuantity);
        //更新入库单实际库存和状态
        PurchaseInStockOrderDO purchaseStorageOrderDOUpdate = buildPurchaseStorageOrderDO(storageOrderDO, enterQuantity);
        //更新商品库存
        StockSaveOrUpdateBO stockSaveOrUpdateBO = buildStockSaveOrUpdate(inStorageItemList);
        List<StockDO> stockSave = stockSaveOrUpdateBO.getStockSave();
        List<StockUpdateBO> stockUpdate = stockSaveOrUpdateBO.getStockUpdate();
        //数据库操作
        //更新入库单
        this.updateById(purchaseStorageOrderDOUpdate);
        //更新入库单item数量
        storageOrderItemDetailsService.updateBatchById(storageOrderItemDetailsDOUpdate);
        //保存入库详细
        storageDetailsService.save(storageDetailsDO);
        //更新采购单状态
        purchaseOrderService.updateById(purchaseOrderDOUpdate);
        if (!stockSave.isEmpty()) {
            //保存库存
            stockService.saveBatch(stockSave);
        }
        if (!stockUpdate.isEmpty()) {
            //乐观锁更新 更新商品库存
            stockUpdate.forEach(a -> {
                stockService.lambdaUpdate().
                        eq(StockDO::getId, a.getId()).
                        set(StockDO::getTotalStock, a.getTotalStock()).
                        set(StockDO::getUsableStock, a.getUsableStock());

            });
        }
        //保存库存流水
        stockFlowService.saveBatch(stockFlowList);
    }


    private PurchaseOrderDO buildPurchaseOrderDOUpdate(PurchaseInStockOrderDO storageOrderDO, Integer enterQuantity) {

        Integer inStorageStatus = getInStorageStatus(storageOrderDO, enterQuantity);
        Integer status = null;
        if (inStorageStatus == 1) {
            status = 3;
        } else {
            status = 4;
        }
        PurchaseOrderDO purchaseOrderDOUpdate = new PurchaseOrderDO();
        purchaseOrderDOUpdate.setId(storageOrderDO.getPurchaseOrderId());
        purchaseOrderDOUpdate.setStatus(status);

        return purchaseOrderDOUpdate;
    }

    private PurchaseInStockOrderDO buildPurchaseStorageOrderDO(PurchaseInStockOrderDO storageOrderDO, Integer enterQuantity) {

        Integer actualStorageQuantity = getActualStorageQuantity(storageOrderDO, enterQuantity);
        Integer status = getInStorageStatus(storageOrderDO, enterQuantity);
        PurchaseInStockOrderDO purchaseStorageOrderDOUpdate = new PurchaseInStockOrderDO();
        purchaseStorageOrderDOUpdate.setId(storageOrderDO.getId());
        purchaseStorageOrderDOUpdate.setStorageStatus(status);
        purchaseStorageOrderDOUpdate.setActualStorageQuantity(actualStorageQuantity);

        return purchaseStorageOrderDOUpdate;
    }

    private Integer getActualStorageQuantity(PurchaseInStockOrderDO storageOrderDO, Integer enterQuantity) {

        return storageOrderDO.getActualStorageQuantity() + enterQuantity;
    }

    /**
     * 获取入库状态
     * 1:部分入库
     * 2:全部入库
     */
    private Integer getInStorageStatus(PurchaseInStockOrderDO storageOrderDO, Integer enterQuantity) {

        Integer actualStorageQuantity = getActualStorageQuantity(storageOrderDO, enterQuantity);
        Integer status = null;
        if (actualStorageQuantity.equals(storageOrderDO.getExpectStorageQuantity())) {
            status = 2;
        } else {
            status = 1;
        }
        return status;
    }

    private void inStorageCheck(PurchaseInStockOrderDO storageOrderDO, List<InStockItemDTO> inStorageItemList, InStockDTO inStorageDTO) {

        if (storageOrderDO == null) {
            throw new BizException("入库单不存在");
        }
        Set<Long> purchaseOrderItemIdSet = inStorageItemList.stream().map(InStockItemDTO::getId).collect(Collectors.toSet());
        List<InOutStockOrderItemDO> storageOrderItemDetailList = storageOrderItemDetailsService.lambdaQuery().
                eq(InOutStockOrderItemDO::getInOutStockOrderId, inStorageDTO.getPurchaseInStockOrderId()).list();

        if (purchaseOrderItemIdSet.size() != storageOrderItemDetailList.size()) {
            throw new BizException("入库单部分商品数据不存在");
        }
        Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap = storageOrderItemDetailList.stream()
                .collect(Collectors.toMap(InOutStockOrderItemDO::getId, Function.identity()));

        //校验实际入库数量是否合法
        inStorageItemList.forEach(a -> {
            Integer actualQuantity = a.getActualQuantity();
            Integer quantity = purchaseOrderItemDOMap.get(a.getId()).getSurplusQuantity();
            if (actualQuantity > quantity) {
                throw new BizException("入库商品" + a.getId() + "实际入库数量大于最大数量");
            }
        });
    }

    private List<InOutStockOrderItemDO> buildStorageOrderItemDetailsDO(List<InStockItemDTO> inStorageItemList,
                                                                       Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap) {

        List<InOutStockOrderItemDO> storageOrderItemDetailsDOUpdate = new ArrayList<>();
        for (InStockItemDTO s : inStorageItemList) {

            InOutStockOrderItemDO storageOrderItemDetailsDO1 = purchaseOrderItemDOMap.get(s.getId());
            InOutStockOrderItemDO storageOrderItemDetailsDO = new InOutStockOrderItemDO();
            storageOrderItemDetailsDO.setId(s.getId());
            //剩余数量
            Integer surplusQuantity = storageOrderItemDetailsDO1.getSurplusQuantity() - s.getActualQuantity();
            storageOrderItemDetailsDO.setSurplusQuantity(surplusQuantity);
            storageOrderItemDetailsDOUpdate.add(storageOrderItemDetailsDO);
        }
        return storageOrderItemDetailsDOUpdate;
    }

    private StorageFlowDO buildStorageDetailsDO(WarehouseDO warehouseDO, PurchaseInStockOrderDO
            storageOrderDO, Integer enterQuantity) {


        StorageFlowDO storageDetailsDO = new StorageFlowDO();
        storageDetailsDO.setOrderType(4);
        storageDetailsDO.setBizNumber(storageOrderDO.getCode());
        storageDetailsDO.setWarehousName(warehouseDO.getName());
        storageDetailsDO.setInQuantity(enterQuantity);
        storageDetailsDO.setShopId(UserUtils.getShopId());
        return storageDetailsDO;
    }

    private List<StockFlowDO> buildStockFlowDO(List<InStockItemDTO> inStorageItemList, Map<Long,
            InOutStockOrderItemDO> purchaseOrderItemDOMap, PurchaseInStockOrderDO storageOrderDO, WarehouseDO warehouseDO) {

        List<StockFlowDO> stockFlowList = new ArrayList<>();
        for (InStockItemDTO is : inStorageItemList) {
            InOutStockOrderItemDO storageOrderItemDetailsDO = purchaseOrderItemDOMap.get(is.getId());
            StockFlowDO stockFlowDO = new StockFlowDO();
            stockFlowDO.setBizOrderId(storageOrderDO.getId());
            stockFlowDO.setOrderType(4);
            stockFlowDO.setSkuCode(storageOrderItemDetailsDO.getSkuCode());
            stockFlowDO.setGoodsName(storageOrderItemDetailsDO.getGoodsName());
            stockFlowDO.setBizNumber(storageOrderDO.getCode());
            stockFlowDO.setWarehousName(warehouseDO.getName());
            stockFlowDO.setInQuantity(is.getActualQuantity());
            stockFlowDO.setShopId(UserUtils.getShopId());
            stockFlowList.add(stockFlowDO);
        }
        return stockFlowList;
    }

    private StockSaveOrUpdateBO buildStockSaveOrUpdate(List<InStockItemDTO> inStorageItemList) {

        List<String> skuCodeList = inStorageItemList.stream().map(InStockItemDTO::getSkuCode).collect(Collectors.toList());
        List<StockDO> stockDOlist = stockService.lambdaQuery().in(StockDO::getSkuCode, skuCodeList).list();
        Map<String, StockDO> stockDOMap = stockDOlist.stream()
                .collect(Collectors.toMap(StockDO::getSkuCode, Function.identity()));
        List<StockDO> stockDOSave = new ArrayList<>();
        List<StockUpdateBO> stockDOUpdate = new ArrayList<>();

        for (InStockItemDTO st : inStorageItemList) {
            String skuCode = st.getSkuCode();
            StockDO stockDO = stockDOMap.get(skuCode);
            if (stockDO == null) {
                //新增
                StockDO stock = new StockDO();
                stock.setSkuCode(skuCode);
                stock.setTotalStock(st.getActualQuantity());
                stock.setLockStock(0);
                stock.setUsableStock(st.getActualQuantity());
                stockDOSave.add(stock);
            } else {
                //更新
                Integer totalStock = stockDO.getTotalStock() + st.getActualQuantity();
                Integer usableStock = stockDO.getUsableStock() + st.getActualQuantity();
                StockUpdateBO stockUpdateBO = new StockUpdateBO();
                stockUpdateBO.setTotalStock(totalStock);
                stockUpdateBO.setUsableStock(usableStock);
                stockUpdateBO.setId(stockDO.getId());
                stockDOUpdate.add(stockUpdateBO);
            }

        }
        return new StockSaveOrUpdateBO(stockDOSave, stockDOUpdate);

    }

    @Override
    public PageResult<PurchaseInStockOrderPageVO> purchaseInStockOrderPage(PurchaseInStockOrderPageQuery query) {

        IPage<PurchaseInStockOrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<PurchaseInStockOrderDO> purchaseStorageOrderPage = this.lambdaQuery().
                eq(query.getInStockStatus() != null, PurchaseInStockOrderDO::getStorageStatus, query.getInStockStatus()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (purchaseStorageOrderPage.getRecords().isEmpty()) {

            return PageResult.emptyResult(PurchaseInStockOrderPageVO.class);
        }

        PageResult<PurchaseInStockOrderPageVO> pageResult = PageResult.toPageResult(page, PurchaseInStockOrderPageVO.class);

        List<PurchaseInStockOrderDO> records = purchaseStorageOrderPage.getRecords();
        /**
         * 填充关联属性
         */
        //用set接收 去重
        Set<Long> supplierIdList = records.stream().map(PurchaseInStockOrderDO::getSupplierId).collect(Collectors.toSet());
        Set<Long> warehouseIdList = records.stream().map(PurchaseInStockOrderDO::getWarehouseId).collect(Collectors.toSet());
        Map<Long, SupplierDO> supplierMap = supplierService.lambdaQuery().in(SupplierDO::getId, supplierIdList).list().stream().
                collect(Collectors.toMap(SupplierDO::getId, Function.identity()));
        Map<Long, WarehouseDO> warehouseMap = warehouseService.lambdaQuery().in(WarehouseDO::getId, warehouseIdList).list().stream().
                collect(Collectors.toMap(WarehouseDO::getId, Function.identity()));

        pageResult.getRecords().forEach(vo -> {
            vo.setSupplierName(supplierMap.get(vo.getSupplierId()).getName());
            vo.setWarehouseName(warehouseMap.get(vo.getWarehouseId()).getName());
        });

        return pageResult;
    }

    @Override
    public PurchaseInStockOrderDetailVO purchaseInStockOrderDetail(Long id) {

        PurchaseInStockOrderDO storageOrderDO = this.getById(id);
        if (storageOrderDO == null){
            throw new BizException("采购入库单不存在");
        }
        List<InOutStockOrderItemDO> storageOrderItemDetailsList = storageOrderItemDetailsService.
                lambdaQuery().eq(InOutStockOrderItemDO::getInOutStockOrderId, id).list();
        if (storageOrderItemDetailsList.isEmpty()){
            throw new BizException("采购入库单商品不存在");
        }

        SupplierDO supplierDO = supplierService.getById(storageOrderDO.getSupplierId());
        WarehouseDO warehouseDO = warehouseService.getById(storageOrderDO.getWarehouseId());


        Integer totalExpectStorageQuantity = storageOrderDO.getExpectStorageQuantity();
        Integer totalActualStorageQuantity = storageOrderDO.getActualStorageQuantity();
        Integer totalActualSurplusQuantity = totalExpectStorageQuantity-totalActualStorageQuantity;
        List<PurchaseInStockOrderItemDetailVO> purchaseStorageOrderItemDetailVOList =
                BeanCopyUtils.copyBeanList(storageOrderItemDetailsList, PurchaseInStockOrderItemDetailVO.class);
        purchaseStorageOrderItemDetailVOList.forEach(a->{
            //实际入库数量 = 总数量-剩余数量
            a.setActualQuantity(a.getTotalQuantity()-a.getSurplusQuantity());
        });
        PurchaseInStockOrderDetailVO purchaseStorageOrderDetailVO = new PurchaseInStockOrderDetailVO();
        purchaseStorageOrderDetailVO.setTotalExpectStorageQuantity(totalExpectStorageQuantity);
        purchaseStorageOrderDetailVO.setTotalActualStorageQuantity(totalActualStorageQuantity);
        purchaseStorageOrderDetailVO.setTotalActualSurplusQuantity(totalActualSurplusQuantity);
        purchaseStorageOrderDetailVO.setCode(storageOrderDO.getCode());
        purchaseStorageOrderDetailVO.setPurchaseStorageOrderItemDetailVOList(purchaseStorageOrderItemDetailVOList);
        purchaseStorageOrderDetailVO.setSupplierName(supplierDO.getName());
        purchaseStorageOrderDetailVO.setWarehouseName(warehouseDO.getName());
        purchaseStorageOrderDetailVO.setId(storageOrderDO.getId());
        return purchaseStorageOrderDetailVO;
    }

}
