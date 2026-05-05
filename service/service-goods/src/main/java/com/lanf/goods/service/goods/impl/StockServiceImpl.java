package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.goods.mapper.StockMapper;
import com.lanf.goods.model.bo.GoodsSkuBO;
import com.lanf.goods.model.bo.SkuCodeStockBO;
import com.lanf.goods.model.bo.StockSaveOrUpdateBO;
import com.lanf.goods.model.dto.DeductStockDTO;
import com.lanf.goods.model.dto.StockEnoughDTO;
import com.lanf.goods.model.entity.*;
import com.lanf.goods.model.enums.StockFlowEventTypeEnum;
import com.lanf.goods.model.vo.DeductStockVO;
import com.lanf.goods.model.vo.StockEnoughVO;
import com.lanf.goods.service.goods.*;
import com.lanf.goods.utils.GoodsServiceUtils;
import com.lanf.rocketmq.model.message.UserStockAddMsg;
import com.lanf.rocketmq.model.message.UserStockMsg;
import com.lanf.tcc.service.ITccOperationService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        Long updateVersion = stockDO.getVersion()+1;
        //扣减后的剩余总库存
        Integer  updateTotalStock = totalStock - deductStockDTO.getQuantity();
        //冻结库存
        Integer updateLockStock = stockDO.getLockStock() + deductStockDTO.getQuantity();

        String bizKey = generateDeductStockBizKey(deductStockDTO.getBizKeyPrx());
        /**
         * DB操作
         */
        tccOperationService.tryOperation(bizKey,null);
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
        return buildDeductStockVO( skuCode, deductStockDTO.getQuantity());
    }


    private DeductStockVO buildDeductStockVO(String skuCode,Integer  quantity) {

        GoodsSkuDO goodsSkuDO = goodsSkuService.lambdaQuery().eq(GoodsSkuDO::getSkuCode, skuCode)
                .one();

        Long goodsId = goodsSkuDO.getGoodsId();
        GoodsDO goodsDO = goodsService.getById(goodsId);

        //订单总金额
        BigDecimal totalAmount =  GoodsServiceUtils.calculateTotalAmount(goodsSkuDO.getPrice(), quantity);
        GoodsSkuBO goodsSkuBO = buildGoodsSkuBO(goodsSkuDO, goodsDO);
        DeductStockVO deductStockVO = new DeductStockVO();
        deductStockVO.setTotalAmount(totalAmount);
        deductStockVO.setGoodsSkuBO(goodsSkuBO);
        return deductStockVO;
    }

    private  GoodsSkuBO buildGoodsSkuBO(GoodsSkuDO goodsSkuDO, GoodsDO goodsDO) {
        GoodsSkuBO goodsSkuBO = new GoodsSkuBO();
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
        return goodsSkuBO;
    }

    @Transactional
    public void confirmDeductStock(DeductStockDTO deductStockDTO) {

        log.info("confirmDeductStock[{}]", deductStockDTO);

        String skuCode = deductStockDTO.getSkuCode();
        StockDO stockDO = GoodsServiceUtils.findStockDO(skuCode);
        Long updateVersion = stockDO.getVersion()+1;
        //扣减冻结库存
        Integer lockStock = stockDO.getLockStock() - deductStockDTO.getQuantity();
        UserStockFlowDO userStockFlowDO = buildUserStockFlowDO(deductStockDTO, stockDO);
        String bizKey = generateDeductStockBizKey(deductStockDTO.getBizKeyPrx());
        boolean operation = tccOperationService.confirmOperation(bizKey);
        if ( !operation){
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
     *
     * 生成库存流水业务key
     *
     *
     */
    private String generateDeductStockBizKey(String bizKeyPrx) {

        return bizKeyPrx+":"+"deductStockBizKey";
    }
    private UserStockFlowDO buildUserStockFlowDO(DeductStockDTO deductStockDTO,StockDO stockDO) {

        //总库存 = 可使用+已冻结
        Integer totalStock = stockDO.getUsableStock()+ deductStockDTO.getQuantity();
        Integer eventType = StockFlowEventTypeEnum.ORDER_OUTBOUND.getCode();
        UserStockFlowDO userStockFlowDO = new UserStockFlowDO();
        userStockFlowDO.setUserStockId(stockDO.getId());
        userStockFlowDO.setOrderId(deductStockDTO.getOrderId());
        userStockFlowDO.setEventType(eventType);
        // 总库存 = 可使用+已冻结
        userStockFlowDO.setBeforeQuantity(totalStock);
        userStockFlowDO.setAfterQuantity(totalStock - deductStockDTO.getQuantity());
        userStockFlowDO.setOutQuantity(deductStockDTO.getQuantity());
        return userStockFlowDO;
    }
    @Transactional
    public void cancelDeductStock(DeductStockDTO deductStockDTO) {

        log.info("cancelDeductStock[{}]", deductStockDTO);
        String skuCode = deductStockDTO.getSkuCode();
        StockDO stockDO = GoodsServiceUtils.findStockDO(skuCode);
        Integer usableStock = stockDO.getUsableStock();
        Long updateVersion = stockDO.getVersion()+1;
        //扣减后的剩余总库存
        Integer  updateUsableStock = usableStock + deductStockDTO.getQuantity();
        //冻结库存
        Integer updateLockStock = stockDO.getLockStock() - deductStockDTO.getQuantity();
        String bizKey = generateDeductStockBizKey(deductStockDTO.getBizKeyPrx());
        /**
         * DB操作
         */
        boolean operation = tccOperationService.cancelOperation(bizKey);
        if ( !operation){

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
}
