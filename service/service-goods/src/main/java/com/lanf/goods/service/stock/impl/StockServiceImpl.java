package com.lanf.goods.service.stock.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.goods.model.bo.GoodsSku;
import com.lanf.api.goods.model.dto.DeductStockDTO;
import com.lanf.api.goods.model.dto.SeckillStockPreoccupationDTO;
import com.lanf.api.goods.model.query.UserStockPageQuery;
import com.lanf.api.goods.model.vo.DeductStockVO;
import com.lanf.api.goods.model.vo.StockPageVO;
import com.lanf.api.user.api.UserCacheService;
import com.lanf.api.user.model.vo.AddressListVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.goods.UserStockFlowEventTypeEnum;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.goods.constant.GoodsCodeEnum;
import com.lanf.goods.mapper.StockMapper;
import com.lanf.goods.model.bo.DeductStockParameterBO;
import com.lanf.goods.model.dto.StockEnoughDTO;
import com.lanf.goods.model.dto.SubmitCartStockEnoughDTO;
import com.lanf.goods.model.entity.*;
import com.lanf.goods.model.enums.WarehouseSelectionStrategyEnum;
import com.lanf.goods.model.query.StockQueryByGoodsIdQuery;
import com.lanf.goods.model.vo.StockEnoughVO;
import com.lanf.goods.model.vo.StockWithDistanceVO;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.service.goods.IShopService;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.service.stock.IUserStockFlowService;
import com.lanf.goods.utils.GoodsServiceUtils;
import com.lanf.tcc.service.ITccOperationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
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
    private IUserStockFlowService userStockFlowService;
    @Autowired
    private ITccOperationService tccOperationService;
    @Autowired
    private IGoodsSkuService goodsSkuService;
    @Lazy
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private IShopService shopService;

    /**
     * 查找所有库存
     * 多仓库
     */

    @Override
    public Map<String, StockDO> findBySkuCode(List<String> skuCode) {

        List<StockDO> stockDOList = this.lambdaQuery().in(StockDO::getSkuCode, skuCode).list();

        Map<String, StockDO> stockCount = new HashMap<>();

        for (StockDO stockDO : stockDOList) {

            String skuCode1 = stockDO.getSkuCode();
            stockCount.put(skuCode1, stockDO);

        }

        return stockCount;
    }

    @Transactional
    @HmilyTCC(confirmMethod = "confirmDeductStock", cancelMethod = "cancelDeductStock")
    @Override
    public DeductStockVO deductStock(DeductStockDTO deductStockDTO) {

        String skuCode = deductStockDTO.getSkuCode();
        Long warehouseId = deductStockDTO.getWarehouseId();
        Long goodsId1 = deductStockDTO.getGoodsId();

        /**
         *
         * 可能多个仓库 skucode 暂时取其中一个
         *
         */
        StockDO stockDO = this.lambdaQuery()
                .eq(StockDO::getSkuCode, skuCode)
                .eq(StockDO::getWarehouseId, warehouseId)
                .eq(StockDO::getGoodsId, goodsId1)
                .one();

        String bizKey = generateDeductStockBizKey(deductStockDTO.getBizKeyPrx());
        if (stockDO == null) {

            log.warn("库存不存在");
            tccOperationService.addInterruptedFlag(bizKey, "库存不存在");
            throw new BizException("库存不存在");

        }
        Integer totalStock = stockDO.getUsableStock();
        if (totalStock < deductStockDTO.getQuantity()) {

            log.warn("库存不足");
            tccOperationService.addInterruptedFlag(bizKey, "库存不足");
            throw new BizException("库存不足");
        }
        Long tenantId = stockDO.getTenantId();
        DeductStockParameterBO deductStockParameterBO = new DeductStockParameterBO();
        deductStockParameterBO.setTenantId(tenantId);
        deductStockParameterBO.setOrderNumber(deductStockDTO.getOrderNumber());
        deductStockParameterBO.setSkuCode(skuCode);
        deductStockParameterBO.setStockId(stockDO.getId());

        Long updateVersion = stockDO.getVersion() + 1;
        //扣减后的剩余总库存
        Integer updateTotalStock = totalStock - deductStockDTO.getQuantity();
        //冻结库存
        Integer updateLockStock = stockDO.getLockStock() + deductStockDTO.getQuantity();


        /**
         * 查询返回需要的数据
         */
        GoodsSkuDO goodsSkuDO = goodsSkuService
                .lambdaQuery()
                .eq(GoodsSkuDO::getSkuCode, skuCode)
                .eq(GoodsSkuDO::getGoodsId, goodsId1)
                .one();

        Long goodsId = goodsSkuDO.getGoodsId();
        GoodsDO goodsDO = goodsService.getById(goodsId);
        /**
         * 提前准备返回的数据
         *
         */
        //订单总金额
        BigDecimal totalAmount = GoodsServiceUtils.calculateTotalAmount(goodsSkuDO.getPrice(),
                deductStockDTO.getQuantity());

        GoodsSku goodsSkuBO = buildGoodsSkuBO(goodsSkuDO, goodsDO, stockDO);
        DeductStockVO deductStockVO = new DeductStockVO();
        deductStockVO.setTotalAmount(totalAmount);
        deductStockVO.setGoodsSkuBO(goodsSkuBO);

        /**
         * DB操作
         */
        tccOperationService.tryOperation(bizKey, JsonUtils.toJsonString(deductStockParameterBO));
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

        return deductStockVO;
    }



    private GoodsSku buildGoodsSkuBO(GoodsSkuDO goodsSkuDO, GoodsDO goodsDO, StockDO stockDO) {

        ShopDO shopDO = shopService.getById(goodsDO.getShopId());

        GoodsSku goodsSkuBO = new GoodsSku();
        goodsSkuBO.setSkuId(goodsSkuDO.getId());
        goodsSkuBO.setGoodsId(goodsSkuDO.getGoodsId());
        goodsSkuBO.setGoodsName(goodsDO.getName());
        goodsSkuBO.setSkuCode(goodsSkuDO.getSkuCode());
        goodsSkuBO.setSkuPictureAddress(goodsSkuDO.getSkuPictureAddress());
        goodsSkuBO.setPrice(goodsSkuDO.getPrice());
        goodsSkuBO.setSkuVersion(goodsSkuDO.getVersion());
        goodsSkuBO.setGoodsVersion(goodsDO.getVersion());
        goodsSkuBO.setGoodsTitle(goodsDO.getTitle());
        goodsSkuBO.setWarehouseId(stockDO.getWarehouseId());
        goodsSkuBO.setTenantId(goodsSkuDO.getTenantId());
        goodsSkuBO.setShopName(shopDO.getName());

        return goodsSkuBO;
    }

    @Transactional
    public void confirmDeductStock(DeductStockDTO deductStockDTO) {

        log.info("confirmDeductStock[{}]", deductStockDTO);


        try {
            String bizKey = generateDeductStockBizKey(deductStockDTO.getBizKeyPrx());

            String parameter = tccOperationService.getParameter(bizKey);

            DeductStockParameterBO parameterBO = JsonUtils.toObject(parameter, DeductStockParameterBO.class);
            Long stockId = parameterBO.getStockId();
            StockDO stockDO = this.getById(stockId);

            Long updateVersion = stockDO.getVersion() + 1;
            //扣减冻结库存
            Integer lockStock = stockDO.getLockStock() - deductStockDTO.getQuantity();


            UserStockFlowDO userStockFlowDO = buildUserStockFlowDO(deductStockDTO, stockDO, parameterBO);

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
        } catch (Exception e) {
            log.error("confirmDeductStock异常", e);
            throw  e;
        }
    }

    /**
     * 生成库存流水业务key
     */
    private String generateDeductStockBizKey(String bizKeyPrx) {

        return bizKeyPrx + ":" + "deductStockBizKey";
    }

    private UserStockFlowDO buildUserStockFlowDO(DeductStockDTO deductStockDTO,StockDO stockDO,
                                                 DeductStockParameterBO parameterBO) {


        String flowNo = parameterBO.getSkuCode() + ":" + deductStockDTO.getOrderNumber()+":" +
                UserStockFlowEventTypeEnum.ORDER_OUTBOUND.getCode();

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
        userStockFlowDO.setTenantId(parameterBO.getTenantId());
        userStockFlowDO.setFlowNo(flowNo);
        userStockFlowDO.setWarehouseId(stockDO.getWarehouseId());
        return userStockFlowDO;
    }

    @Transactional
    public void cancelDeductStock(DeductStockDTO deductStockDTO) {

        log.info("cancelDeductStock[{}]", deductStockDTO);
        try {
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
        } catch (Exception e) {
            log.error("cancelDeductStock失败", e);
            throw e;
        }


    }


    @Override
    public StockEnoughVO isStockEnough(StockEnoughDTO dto) {

        //1.校验是否有地址
        List<AddressListVO> addressListVOS = RpcResultParser.parseResult(userCacheService.addressListQuery());
        AddressListVO addressListVO = null;
        for (AddressListVO add : addressListVOS) {
            if (add.getId().equals(dto.getAddressId())) {
                addressListVO = add;
            }

        }
        if (addressListVO == null) {
            log.warn("请先选择收货地址");
            throw new BizException("请先选择收货地址");
        }
        //2.查询库存
        StockQueryByGoodsIdQuery stockEnoughVO = new StockQueryByGoodsIdQuery();
        stockEnoughVO.setAreaCode(addressListVO.getAreaCode());
        stockEnoughVO.setGoodsId(dto.getGoodsId());
        stockEnoughVO.setSkuCode(dto.getSkuCode());
        stockEnoughVO.setLatitude(addressListVO.getLatitude());
        stockEnoughVO.setLongitude(addressListVO.getLongitude());

        List<StockWithDistanceVO> distanceVOS = this.stockQueryByGoodsId(stockEnoughVO);
        String alertKey = dto.getGoodsId()+"_"+dto.getSkuCode();
        if (distanceVOS.isEmpty()) {
            log.warn(alertKey+"商品无库存");
            throw new BizException(alertKey+"商品无库存");
        }
        //3.校验库存
        StockWithDistanceVO stock = distanceVOS.get(0);
        if (dto.getQuantity() > stock.getUsableStock()) {
            log.warn(alertKey+"库存不足");
            throw new BizException(alertKey+"库存不足");
        }
        //4.封装返回
        StockEnoughVO vco = new StockEnoughVO();
        vco.setSkuCode(stock.getSkuCode());
        vco.setGoodsId(dto.getGoodsId());
        vco.setWarehouseId(stock.getWarehouseId());


        return vco;
    }

    /**
     * 待优化 成多线程 并行join
     *
     */
    @Override
    public List<StockEnoughVO> submitCartStockEnough(SubmitCartStockEnoughDTO dto) {

        List<StockEnoughDTO> stockEnoughDTOS = dto.getStockEnoughDTOS();
        List<StockEnoughVO> result = new ArrayList<>(stockEnoughDTOS.size());
        for (StockEnoughDTO stockEnoughDTO : stockEnoughDTOS) {
            StockEnoughVO stockEnough = this.isStockEnough(stockEnoughDTO);
            result.add(stockEnough);
        }

        return result;
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

    @Override
    public List<StockWithDistanceVO> stockQueryByGoodsId(StockQueryByGoodsIdQuery dto) {


        String areaCode = dto.getAreaCode();
        Long goodsId = dto.getGoodsId();
        BigDecimal latitude = dto.getLatitude();
        BigDecimal longitude = dto.getLongitude();
        WarehouseSelectionStrategyEnum strategy = dto.getWarehouseSelectionStrategy();
        /**
         * 查询该商品下的 skuCode 所有库存
         */
        List<StockDO> stockList = this.lambdaQuery()
                .eq(dto.getSkuCode() != null, StockDO::getSkuCode, dto.getSkuCode())
                .eq(StockDO::getGoodsId, goodsId)
                .list();
        if (stockList.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取用户地址的经纬度
        AddressListVO userAddress = null;

        /**
         * 1.优先取用户定位中的地理位置 的areaCode  latitude  longitude
         * 2.取默认地址
         * 3.如果没有默认地址，则抛出错误提示用户
         *
         * 对于客户端 如果没有默认地址 传定位
         * 如果有默认地址 则不传定位
         */
        if (!StringUtils.isEmpty(areaCode) &&
                latitude != null
                && longitude != null) {

            userAddress = new AddressListVO();
            userAddress.setAreaCode(areaCode);
            userAddress.setLatitude(latitude);
            userAddress.setLongitude(longitude);
        } else {
            /**
             * 没有定位信息  取用户默认地址
             */
            userAddress = getUserDefaultAddress();
        }
        // 根据仓库选择策略匹配库存
        List<StockDO> matchedStockList = matchStockByStrategy(stockList, userAddress, strategy);

        // 转换为 VO 对象
        return convertToStockWithDistanceVO(matchedStockList);
    }

    /**
     * 根据仓库选择策略匹配库存
     *
     * @param stockList   所有库存列表
     * @param userAddress 用户地址信息（包含经纬度）
     * @param strategy    仓库选择策略
     * @return 匹配后的库存列表
     */
    private List<StockDO> matchStockByStrategy(List<StockDO> stockList, AddressListVO userAddress, WarehouseSelectionStrategyEnum strategy) {
        if (strategy == null) {
            // 默认使用同区域附近仓库策略
            strategy = WarehouseSelectionStrategyEnum.SAME_REGION_NEARBY;
        }

        switch (strategy) {
            case SAME_REGION_NEARBY:
                // 同区域附近仓库：优先选择相同 areaCode 的仓库，然后按距离排序
                List<StockDO> sameAreaStocks = stockList.stream()
                        .filter(stock -> userAddress.getAreaCode().equals(stock.getAreaCode()))
                        .collect(Collectors.toList());

                // 如果同区域有仓库，按距离排序；否则返回所有仓库按距离排序
                if (!sameAreaStocks.isEmpty()) {
                    return sortStockByDistance(sameAreaStocks, userAddress);
                } else {
                    return new ArrayList<>();
                }

            case NATIONAL_MOST_STOCK:
                // 全国范围内附近仓库：按距离排序，不依赖库存数量
                return sortStockByDistance(stockList, userAddress);

            default:
                throw new BizException("不支持的库存选择策略");
        }
    }

    /**
     * 根据距离对库存列表进行排序（从近到远），并对相同 skuCode 去重，只保留最近的
     *
     * @param stockList   库存列表
     * @param userAddress 用户地址
     * @return 按距离排序并去重后的库存列表
     */
    private List<StockDO> sortStockByDistance(List<StockDO> stockList, AddressListVO userAddress) {
        // 先计算每个库存的距离
        Map<StockDO, Double> stockDistanceMap = new HashMap<>();
        for (StockDO stock : stockList) {
            if (stock.getLatitude() != null && stock.getLongitude() != null) {
                double distance = calculateDistance(
                        userAddress.getLatitude().doubleValue(), userAddress.getLongitude().doubleValue(),
                        stock.getLatitude().doubleValue(), stock.getLongitude().doubleValue()
                );
                stockDistanceMap.put(stock, distance);
            }
        }

        // 按 skuCode 分组，每组保留距离最近的
        Map<String, StockDO> bestStockBySkuCode = new HashMap<>();
        for (Map.Entry<StockDO, Double> entry : stockDistanceMap.entrySet()) {
            StockDO stock = entry.getKey();
            Double distance = entry.getValue();
            String skuCode = stock.getSkuCode();

            if (!bestStockBySkuCode.containsKey(skuCode)) {
                bestStockBySkuCode.put(skuCode, stock);
            } else {
                // 比较距离，保留更近的
                StockDO existingStock = bestStockBySkuCode.get(skuCode);
                Double existingDistance = stockDistanceMap.get(existingStock);
                if (distance < existingDistance) {
                    bestStockBySkuCode.put(skuCode, stock);
                }
            }
        }

        // 将去重后的库存按距离排序
        return bestStockBySkuCode.values().stream()
                .sorted((s1, s2) -> {
                    Double distance1 = stockDistanceMap.get(s1);
                    Double distance2 = stockDistanceMap.get(s2);
                    return Double.compare(distance1, distance2); // 升序排列，近的在前
                })
                .collect(Collectors.toList());
    }

    /**
     * 使用 Haversine 公式计算两点之间的距离
     *
     * @param lat1 起点纬度
     * @param lon1 起点经度
     * @param lat2 终点纬度
     * @param lon2 终点经度
     * @return 距离（公里）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（公里）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * 将 StockDO 列表转换为 StockWithDistanceVO 列表
     *
     * @param stockList 库存列表
     * @return VO 列表
     */
    private List<StockWithDistanceVO> convertToStockWithDistanceVO(List<StockDO> stockList) {

        return stockList.stream().map(stock -> {
            StockWithDistanceVO vo = new StockWithDistanceVO();
            vo.setSkuCode(stock.getSkuCode());
            vo.setWarehouseId(stock.getWarehouseId());
            vo.setUsableStock(stock.getUsableStock());
            vo.setHasStock(stock.getUsableStock() != null && stock.getUsableStock() > 0);

            return vo;
        }).collect(Collectors.toList());
    }

    private String buildStockDOMapKey(String skuCode, String finalAreaCode) {

        return skuCode + "_" + finalAreaCode;
    }

    private StockDO selectBetterStock(StockDO stock1, StockDO stock2) {


        /**
         * 1.取有货的
         * 2.都有货 取库存更多的
         */
        boolean hasStock1 = stock1.getUsableStock() != null && stock1.getUsableStock() > 0;
        boolean hasStock2 = stock2.getUsableStock() != null && stock2.getUsableStock() > 0;

        if (hasStock1 && !hasStock2) {
            return stock1;
        }

        if (!hasStock1 && hasStock2) {
            return stock2;
        }

        int usableStock1 = stock1.getUsableStock() != null ? stock1.getUsableStock() : 0;
        int usableStock2 = stock2.getUsableStock() != null ? stock2.getUsableStock() : 0;

        if (usableStock1 >= usableStock2) {
            return stock1;
        } else {
            return stock2;
        }
    }

    private AddressListVO getUserDefaultAddress() {

        Result<List<AddressListVO>> result = userCacheService.addressListQuery();

        List<AddressListVO> addresses = RpcResultParser.parseResult(result);
        /**
         * 选取默认地址
         */
        Optional<AddressListVO> defaultAddress = addresses.stream()
                .filter(addr -> addr.getDefaultAddress() != null && addr.getDefaultAddress() == 0)
                .findFirst();
        if (defaultAddress.isPresent()) {
            return defaultAddress.get();
        }
        throw new BizException(GoodsCodeEnum.ADDRESS_EMPTY.getCode(), GoodsCodeEnum.ADDRESS_EMPTY.getMessage());
    }


    @Override
    public PageResult<StockPageVO> stockPageQuery(UserStockPageQuery query) {
        IPage<StockDO> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<StockDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(query.getSkuCode()), StockDO::getSkuCode, query.getSkuCode())
                .eq(query.getWarehouseId() != null, StockDO::getWarehouseId, query.getWarehouseId())
                .orderByDesc(StockDO::getUpdateTime);

        IPage<StockDO> result = this.page(page, wrapper);

        PageResult<StockPageVO> pageResult = new PageResult<>();
        pageResult.setRecords(BeanCopyUtils.copyBeanList(result.getRecords(), StockPageVO.class));
        pageResult.setTotal(result.getTotal());
        pageResult.setSize(result.getSize());

        return pageResult;
    }

}
