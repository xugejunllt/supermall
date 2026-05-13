package com.lanf.goods.service.stock.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.goods.UserStockFlowEventTypeEnum;
import com.lanf.goods.mapper.StockMapper;
import com.lanf.goods.model.bo.GoodsSku;
import com.lanf.goods.model.bo.SkuCodeStockBO;
import com.lanf.goods.model.dto.DeductStockDTO;
import com.lanf.goods.model.dto.SeckillStockPreoccupationDTO;
import com.lanf.goods.model.dto.StockEnoughDTO;
import com.lanf.goods.model.entity.GoodsDO;
import com.lanf.goods.model.entity.GoodsSkuDO;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.entity.UserStockFlowDO;
import com.lanf.goods.model.vo.DeductStockVO;
import com.lanf.goods.model.vo.StockEnoughVO;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.service.goods.IUserStockSyncRecordService;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.service.stock.IUserStockFlowService;
import com.lanf.goods.utils.GoodsServiceUtils;
import com.lanf.tcc.service.ITccOperationService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 库存 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2025-11-29
 */
@Slf4j
@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, StockDO> implements IStockService {

    @Autowired
    private IUserStockSyncRecordService userStockSyncRecordService;

    @Autowired
    private IUserStockFlowService userStockFlowService;
    @Autowired
    private ITccOperationService tccOperationService;
    @Autowired
    private IGoodsSkuService goodsSkuService;
    @Lazy
    @Autowired
    private IGoodsService goodsService;


    @Override
    public Map<String, SkuCodeStockBO> findBySkuCode(List<String> skuCode) {

        List<StockDO> stockDOList = this.lambdaQuery().in(StockDO::getSkuCode, skuCode).list();

        Map<String, SkuCodeStockBO> stockCount = new HashMap<>();

        for (StockDO stockDO : stockDOList) {

            String skuCode1 = stockDO.getSkuCode();
            SkuCodeStockBO skuCodeStockBO = stockCount.get(skuCode1);

            if (skuCodeStockBO == null) {
                SkuCodeStockBO stockBO = new SkuCodeStockBO();
                stockBO.setTotalStock(stockDO.getUsableStock());
                stockCount.put(skuCode1, stockBO);
            } else {
                Integer count2 = skuCodeStockBO.getTotalStock() + stockDO.getUsableStock();
                skuCodeStockBO.setTotalStock(count2);
            }
        }

        return stockCount;
    }

    @Transactional
    @HmilyTCC(confirmMethod = "confirmDeductStock", cancelMethod = "cancelDeductStock")
    @Override
    public DeductStockVO deductStock(DeductStockDTO deductStockDTO) {

        String skuCode = deductStockDTO.getSkuCode();

        List<StockDO> stockDOList = this.lambdaQuery().eq(StockDO::getSkuCode, skuCode).list();
        if (stockDOList.isEmpty()) {
            log.info("库存不存在");
            throw new BizException("库存不存在");
        }

        /**
         *
         * 可能多个仓库 skucode 暂时取其中一个
         *
         */
        StockDO stockDO = GoodsServiceUtils.findStockDO(skuCode);
        Integer totalStock = stockDO.getUsableStock();
        if (totalStock < deductStockDTO.getQuantity()) {
            log.info("库存不足");
            throw new BizException("库存不足");
        }

        Long updateVersion = stockDO.getVersion() + 1;
        //扣减后的剩余总库存
        Integer updateTotalStock = totalStock - deductStockDTO.getQuantity();
        //冻结库存
        Integer updateLockStock = stockDO.getLockStock() + deductStockDTO.getQuantity();

        String bizKey = generateDeductStockBizKey(deductStockDTO.getBizKeyPrx());
        /**
         * 查询返回需要的数据
         */
        GoodsSkuDO goodsSkuDO = goodsSkuService.lambdaQuery().eq(GoodsSkuDO::getSkuCode, skuCode)
                .one();

        Long goodsId = goodsSkuDO.getGoodsId();
        GoodsDO goodsDO = goodsService.getById(goodsId);

        /**
         * DB操作
         */
        tccOperationService.tryOperation(bizKey, null);
        boolean update = this.lambdaUpdate().
                eq(StockDO::getId, stockDO.getId()).
                eq(StockDO::getVersion, stockDO.getVersion()).
                set(StockDO::getUsableStock, updateTotalStock).
                set(StockDO::getLockStock, updateLockStock).
                set(StockDO::getVersion, updateVersion).
                update();
        if (!update) {
            log.info("扣减库存失败");
            throw new BizException("扣减库存失败");
        }
        return buildDeductStockVO(deductStockDTO.getQuantity(), goodsSkuDO, goodsDO, stockDO);
    }


    private DeductStockVO buildDeductStockVO(Integer quantity,
                                             GoodsSkuDO goodsSkuDO, GoodsDO goodsDO, StockDO stockDO) {


        //订单总金额
        BigDecimal totalAmount = GoodsServiceUtils.calculateTotalAmount(goodsSkuDO.getPrice(), quantity);
        GoodsSku goodsSkuBO = buildGoodsSkuBO(goodsSkuDO, goodsDO, stockDO);
        DeductStockVO deductStockVO = new DeductStockVO();
        deductStockVO.setTotalAmount(totalAmount);
        deductStockVO.setGoodsSkuBO(goodsSkuBO);
        return deductStockVO;
    }

    private GoodsSku buildGoodsSkuBO(GoodsSkuDO goodsSkuDO, GoodsDO goodsDO, StockDO stockDO) {
        GoodsSku goodsSkuBO = new GoodsSku();
        goodsSkuBO.setSkuId(goodsSkuDO.getId());
        goodsSkuBO.setGoodsId(goodsSkuDO.getGoodsId());
        goodsSkuBO.setGoodsName(goodsDO.getName());
        goodsSkuBO.setSkuCode(goodsSkuDO.getSkuCode());
        goodsSkuBO.setSkuName(goodsSkuDO.getSkuName());
        goodsSkuBO.setSkuPictureAddress(goodsSkuDO.getSkuPictureAddress());
        goodsSkuBO.setPrice(goodsSkuDO.getPrice());
        goodsSkuBO.setSkuVersion(goodsSkuDO.getVersion());
        goodsSkuBO.setGoodsVersion(goodsDO.getVersion());
        goodsSkuBO.setGoodsTitle(goodsDO.getTitle());
        goodsSkuBO.setWarehouseId(stockDO.getWarehouseId());
        return goodsSkuBO;
    }

    @Transactional
    public void confirmDeductStock(DeductStockDTO deductStockDTO) {

        log.info("confirmDeductStock[{}]", deductStockDTO);

        String skuCode = deductStockDTO.getSkuCode();
        StockDO stockDO = GoodsServiceUtils.findStockDO(skuCode);
        Long updateVersion = stockDO.getVersion() + 1;
        //扣减冻结库存
        Integer lockStock = stockDO.getLockStock() - deductStockDTO.getQuantity();
        UserStockFlowDO userStockFlowDO = buildUserStockFlowDO(deductStockDTO, stockDO);
        String bizKey = generateDeductStockBizKey(deductStockDTO.getBizKeyPrx());
        boolean operation = tccOperationService.confirmOperation(bizKey);
        if (!operation) {
            log.info("confirm已执行");
            return;
        }
        userStockFlowService.save(userStockFlowDO);
        boolean update = this.lambdaUpdate().
                eq(StockDO::getId, stockDO.getId()).
                eq(StockDO::getVersion, stockDO.getVersion()).
                set(StockDO::getLockStock, lockStock).
                set(StockDO::getVersion, updateVersion).
                update();
        if (!update) {
            log.info("解冻失败");
            throw new BizException("解冻失败");
        }
    }

    /**
     * 生成库存流水业务key
     */
    private String generateDeductStockBizKey(String bizKeyPrx) {

        return bizKeyPrx + ":" + "deductStockBizKey";
    }

    private UserStockFlowDO buildUserStockFlowDO(DeductStockDTO deductStockDTO, StockDO stockDO) {

        //总库存 = 可使用+已冻结
        Integer totalStock = stockDO.getUsableStock() + deductStockDTO.getQuantity();
        UserStockFlowDO userStockFlowDO = new UserStockFlowDO();
        userStockFlowDO.setUserStockId(stockDO.getId());
        userStockFlowDO.setOrderId(deductStockDTO.getOrderId());
        userStockFlowDO.setEventType(UserStockFlowEventTypeEnum.ORDER_OUTBOUND);
        // 总库存 = 可使用+已冻结
        userStockFlowDO.setBeforeQuantity(totalStock);
        userStockFlowDO.setAfterQuantity(totalStock - deductStockDTO.getQuantity());
        userStockFlowDO.setChangeQuantity(deductStockDTO.getQuantity());
        return userStockFlowDO;
    }

    @Transactional
    public void cancelDeductStock(DeductStockDTO deductStockDTO) {

        log.info("cancelDeductStock[{}]", deductStockDTO);
        String skuCode = deductStockDTO.getSkuCode();
        StockDO stockDO = GoodsServiceUtils.findStockDO(skuCode);
        Integer usableStock = stockDO.getUsableStock();
        Long updateVersion = stockDO.getVersion() + 1;
        //扣减后的剩余总库存
        Integer updateUsableStock = usableStock + deductStockDTO.getQuantity();
        //冻结库存
        Integer updateLockStock = stockDO.getLockStock() - deductStockDTO.getQuantity();
        String bizKey = generateDeductStockBizKey(deductStockDTO.getBizKeyPrx());
        /**
         * DB操作
         */
        boolean operation = tccOperationService.cancelOperation(bizKey);
        if (!operation) {

            return;
        }
        boolean update = this.lambdaUpdate().
                eq(StockDO::getId, stockDO.getId()).
                eq(StockDO::getVersion, stockDO.getVersion()).
                set(StockDO::getUsableStock, updateUsableStock).
                set(StockDO::getLockStock, updateLockStock).
                set(StockDO::getVersion, updateVersion).
                update();
        if (!update) {
            log.info("cancelDeductStock失败");
            throw new BizException("cancelDeductStock失败");
        }


    }


    @Override
    public StockEnoughVO isStockEnough(StockEnoughDTO dto) {

        StockDO stockDO = GoodsServiceUtils.findStockDO(dto.getSkuCode());
        boolean enough = true;
        if (stockDO.getUsableStock() < dto.getQuantity()) {
            log.info("库存不足");
            enough = false;
        }
        StockEnoughVO stockEnoughVO = new StockEnoughVO();
        stockEnoughVO.setSkuId(stockDO.getId());
        stockEnoughVO.setEnough(enough);

        return stockEnoughVO;
    }

    /**
     * 待实现tcc事务
     */
    @Transactional
    @HmilyTCC(confirmMethod = "confirmSeckillStockPreoccupation", cancelMethod = "cancelSeckillStockPreoccupation")
    @Override
    public void seckillStockPreoccupation(SeckillStockPreoccupationDTO dto) {

        StockDO one = this.lambdaQuery().eq(StockDO::getSkuCode, dto.getSkuCode())
                .eq(StockDO::getWarehouseId, dto.getWarehouseId())
                .one();

        String bizKey = generateSeckillStockPreoccupation(dto.getBizKeyPrx());

        if (one == null) {
            log.error("库存不存在");
            tccOperationService.addInterruptedFlag(bizKey, "库存不存在");

            throw new BizException("库存不存在");
        }
        Integer usableStock = one.getUsableStock();
        Integer preQuantity = dto.getPreQuantity();

        if (usableStock < preQuantity) {
            log.warn("库存不足");
            tccOperationService.addInterruptedFlag(bizKey, "库存不足");
            throw new BizException("库存不足");
        }

        /**
         * DB操作
         */
        tccOperationService.tryOperation(bizKey, null);
        boolean update = this.lambdaUpdate().eq(StockDO::getId, one.getId())
                .eq(StockDO::getVersion, one.getVersion())
                .set(StockDO::getUsableStock, usableStock - preQuantity)
                .set(StockDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("预占库存失败");
            throw new BizException("预占库存失败");
        }

    }

    private String generateSeckillStockPreoccupation(String bizKeyPrx) {

        return bizKeyPrx + ":" + "seckillStockPreoccupation";
    }

    public void confirmSeckillStockPreoccupation(SeckillStockPreoccupationDTO dto) {
        /**
         * 空执行 什么也不作
         */
        String bizKey = generateSeckillStockPreoccupation(dto.getBizKeyPrx());
        boolean operation = tccOperationService.confirmOperation(bizKey);
        if (!operation) {
            log.info("confirm已执行");
            return;
        }

    }
    @Transactional
    public void cancelSeckillStockPreoccupation(SeckillStockPreoccupationDTO dto) {


        String bizKey = generateSeckillStockPreoccupation(dto.getBizKeyPrx());
        StockDO one = this.lambdaQuery().eq(StockDO::getSkuCode, dto.getSkuCode())
                .eq(StockDO::getWarehouseId, dto.getWarehouseId())
                .one();
        if (one == null) {
            log.error("库存不存在");
            tccOperationService.addInterruptedFlag(bizKey, "库存不存在");
            throw new BizException("库存不存在");
        }

        boolean operation = tccOperationService.cancelOperation(bizKey);
        if (!operation) {

            return;
        }
        Integer usableStock = one.getUsableStock();
        Integer preQuantity = dto.getPreQuantity();
        boolean update = this.lambdaUpdate().eq(StockDO::getId, one.getId())
                .eq(StockDO::getVersion, one.getVersion())
                .set(StockDO::getUsableStock, usableStock + preQuantity)
                .set(StockDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("预占库存失败");
            throw new BizException("预占库存失败");
        }
    }
}
