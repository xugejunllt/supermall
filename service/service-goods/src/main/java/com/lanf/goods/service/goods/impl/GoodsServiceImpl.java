package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.goods.model.bo.*;
import com.lanf.api.goods.model.dto.AddGoodsDTO;
import com.lanf.api.goods.model.dto.CheckAndQueryGoodsDTO;
import com.lanf.api.goods.model.dto.UpGoodsDTO;
import com.lanf.api.goods.model.query.GoodsPageQuery;
import com.lanf.api.goods.model.vo.ApiGoodsSkuVO;
import com.lanf.api.goods.model.vo.GoodsDetailVO;
import com.lanf.api.goods.model.vo.GoodsPageVO;
import com.lanf.api.goods.model.vo.SkuNameVO;
import com.lanf.api.user.api.UserCacheService;
import com.lanf.cache.aop.DistributedLock;
import com.lanf.cache.service.DistributedLocker;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.IdUtils;
import com.lanf.goods.constant.GoodsCodeEnum;
import com.lanf.goods.constant.GoodsRedisKeyConstants;
import com.lanf.goods.mapper.GoodsMapper;
import com.lanf.goods.model.entity.*;
import com.lanf.goods.model.vo.GoodsDetailForUserVO;
import com.lanf.goods.model.vo.SkuDetailVO;
import com.lanf.goods.model.vo.SkuInfo;
import com.lanf.goods.model.vo.SpecItem;
import com.lanf.goods.service.goods.*;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.SyncGoodsInfoToEsMsg;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.web.utils.CndUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 基础商品 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Slf4j
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, GoodsDO> implements IGoodsService {

    @Autowired
    private IGoodsCategoryService goodsCategoryService;
    @Autowired
    private IGoodsBrandService goodsBrandService;
    @Autowired
    private IGoodsSkuService goodsSkuService;
    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private IStockService stockService;
    @Autowired
    private CndUtils cndUtils;
    @Autowired
    private IGoodsHistoryVersionService goodsHistoryVersionService;
    @Autowired
    private IGoodsSkuHistoryVersionService goodsSkuHistoryVersionService;

    @Autowired
    private IGoodsSyncEsRecordService goodsSyncEsRecordService;
    @Autowired
    private IShopService shopService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Autowired
    private DistributedLocker distributedLocker;
    
    @Autowired
    private RedissonCacheService redissonCacheService;

    @DistributedLock(key = "#dto.code")
    @Transactional
    @Override
    public void addGoods(AddGoodsDTO dto) {

        validateAddGoods(dto);
        Long goodsId = IdUtils.generateId();
        Long version = 1L;
        GoodsDO goodsDO = buildGoodsDO(dto, goodsId, version);
        List<GoodsSkuDO> goodsSkuList = buildGoodsSkuList(dto.getGoodsSkuAddDTOList(), goodsId, version);
        saveGoodsWithHistory(goodsDO, goodsSkuList);
    }

    /**
     * 构建商品主对象
     */
    private GoodsDO buildGoodsDO(AddGoodsDTO dto, Long goodsId, Long version) {
        GoodsDO goodsDO = BeanCopyUtils.copyBean(dto, GoodsDO.class);
        
        goodsDO.setId(goodsId);
        goodsDO.setVersion(version);
        goodsDO.setUpDownStatus(0);
        goodsDO.setPictureAddress(JsonUtils.toJsonString(dto.getPictureAddressList()));
        goodsDO.setPromptWordLabel(convertListToJson(dto.getPromptWordLabel()));
        goodsDO.setExtendedTags(convertListToJson(dto.getExtendedTags()));
        
        return goodsDO;
    }

    /**
     * 将逗号分隔的字符串转换为 JSON 字符串
     */
    private String convertListToJson(String commaSeparatedStr) {
        if (commaSeparatedStr == null || commaSeparatedStr.trim().isEmpty()) {
            return JsonUtils.toJsonString(new ArrayList<>());
        }
        List<String> list = IStringUtils.toList(commaSeparatedStr, ",");
        return JsonUtils.toJsonString(list);
    }

    /**
     * 构建商品SKU列表
     */
    private List<GoodsSkuDO> buildGoodsSkuList(List<GoodsSkuAdd> skuAddList, Long goodsId, Long version) {
        List<GoodsSkuDO> goodsSkuList = BeanCopyUtils.copyBeanList(skuAddList, GoodsSkuDO.class);
        
        for (GoodsSkuDO sku : goodsSkuList) {
            initializeSkuAttributes(sku, goodsId, version);
        }
        
        return goodsSkuList;
    }

    /**
     * 初始化SKU属性
     */
    private void initializeSkuAttributes(GoodsSkuDO sku, Long goodsId, Long version) {
        sku.setGoodsId(goodsId);
        sku.setVersion(version);
        
        String attributeDetail = sku.getAttributeDetail();
        List<AttributesJson> attributes = parseAttributeDetail(attributeDetail);
        sku.setAttributes(JsonUtils.toJsonString(attributes));
    }

    /**
     * 保存商品及历史记录
     */
    private void saveGoodsWithHistory(GoodsDO goodsDO, List<GoodsSkuDO> goodsSkuList) {

        GoodsHistoryVersionDO goodsHistory = BeanCopyUtils.copyBean(goodsDO, GoodsHistoryVersionDO.class);
        List<GoodsSkuHistoryVersionDO> skuHistoryList = BeanCopyUtils.copyBeanList(goodsSkuList, GoodsSkuHistoryVersionDO.class);
        this.save(goodsDO);
        goodsSkuService.saveBatch(goodsSkuList);
        goodsHistoryVersionService.save(goodsHistory);
        goodsSkuHistoryVersionService.saveBatch(skuHistoryList);
    }

    /**
     * 校验添加商品参数
     */
    private void validateAddGoods(AddGoodsDTO dto) {
        validateCategory(dto.getCategoryId());
        validateBrand(dto.getBrandId());
        validateSkuList(dto.getGoodsSkuAddDTOList());
    }

    /**
     * 校验商品分类
     */
    private void validateCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BizException("商品分类不能为空");
        }
        
        GoodsCategoryDO category = goodsCategoryService.lambdaQuery()
                .eq(BaseEntity::getId, categoryId)
                .eq(GoodsCategoryDO::getLevel, 3)
                .one();
        
        if (category == null) {
            throw new BizException("商品分类不存在或不是三级分类");
        }
    }

    /**
     * 校验商品品牌
     */
    private void validateBrand(Long brandId) {
        if (brandId == null) {
            throw new BizException("商品品牌不能为空");
        }
        
        GoodsBrandDO brand = goodsBrandService.getById(brandId);
        if (brand == null) {
            throw new BizException("商品品牌不存在");
        }
    }

    /**
     * 校验SKU列表
     */
    private void validateSkuList(List<GoodsSkuAdd> skuList) {
        if (skuList == null || skuList.isEmpty()) {
            throw new BizException("商品SKU不能为空");
        }
        
        for (GoodsSkuAdd sku : skuList) {
            if (sku.getAttributeDetail() == null || sku.getAttributeDetail().trim().isEmpty()) {
                throw new BizException("SKU属性详情不能为空");
            }
        }
    }

    /**
     * 将属性详情字符串转换为 Attributes 列表
     * 格式：颜色,白色;内存,16g;
     */
    private List<AttributesJson> parseAttributeDetail(String attributeDetail) {
        if (attributeDetail == null || attributeDetail.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<AttributesJson> attributes = new ArrayList<>();

        // 按分号分割
        String[] pairs = attributeDetail.split(";");

        for (String pair : pairs) {
            if (pair.trim().isEmpty()) {
                continue;
            }

            // 按逗号分割
            String[] keyValue = pair.split(",", 2);

            if (keyValue.length == 2) {
                AttributesJson attribute = new AttributesJson();
                attribute.setAttribute(keyValue[0].trim());
                attribute.setAttributeValue(keyValue[1].trim());
                attributes.add(attribute);
            }
        }

        return attributes;
    }

    @Override
    public PageResult<GoodsPageVO> goodsPageQuery(GoodsPageQuery query) {
        IPage<GoodsDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<GoodsDO> result = this.lambdaQuery().
                eq(!org.apache.commons.lang3.StringUtils.isEmpty(query.getCode()), GoodsDO::getCode, query.getCode()).
                eq(!org.apache.commons.lang3.StringUtils.isEmpty(query.getName()), GoodsDO::getName, query.getName()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);
        if (result.getRecords().isEmpty()){
            return PageResult.emptyResult();
        }
        PageResult<GoodsPageVO> resultVo = new PageResult<>();
        resultVo.setTotal(result.getTotal());
        resultVo.setSize(result.getSize());
        resultVo.setRecords(BeanCopyUtils.copyBeanList(result.getRecords(), GoodsPageVO.class));

        return resultVo;
    }

    @Override
    public GoodsDetailVO goodsDetailQuery(Long id) {

        GoodsDO goodsDO = this.getById(id);
        if (goodsDO == null) {
            throw new BizException("商品信息不存在");
        }

        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().eq(GoodsSkuDO::getGoodsId, id).list();
        GoodsCategoryDO categoryDO = goodsCategoryService.lambdaQuery().eq(BaseEntity::getId, goodsDO.getCategoryId()).eq(GoodsCategoryDO::getLevel, 3).one();
        GoodsBrandDO brandDO = goodsBrandService.getById(goodsDO.getBrandId());
        List<String> skuCodes = goodsSkuDOList.stream().map(GoodsSkuDO::getSkuCode).collect(Collectors.toList());
        List<StockDO> stockDOList = stockService.lambdaQuery()
                .in(StockDO::getSkuCode, skuCodes)
                .list();

        List<GoodsSkuDetail> goodsSkuDetailVOList = BeanCopyUtils.copyBeanList(goodsSkuDOList, GoodsSkuDetail.class);

        Map<String, List<StockDetail>> stockDetailMap = buildStockDetailMap(stockDOList);

        attachStockDetailsToSkus(goodsSkuDetailVOList, stockDetailMap);

        GoodsDetailVO goodsDetailVO = new GoodsDetailVO();
        BeanCopyUtils.copy(goodsDO, goodsDetailVO);
        goodsDetailVO.setCategoryName(categoryDO.getName());
        goodsDetailVO.setBrandName(brandDO.getName());
        goodsDetailVO.setGoodsSkuDetailVOList(goodsSkuDetailVOList);

        goodsDetailVO.setExtendedTags(JsonUtils.toList(goodsDO.getExtendedTags(), String.class));
        goodsDetailVO.setPromptWordLabel(JsonUtils.toList(goodsDO.getPromptWordLabel(), String.class));
        return goodsDetailVO;
    }

    private Map<String, List<StockDetail>> buildStockDetailMap(List<StockDO> stockDOList) {
        if (stockDOList == null || stockDOList.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, List<StockDetail>> stockDetailMap = new HashMap<>();

        for (StockDO stockDO : stockDOList) {
            StockDetail stockDetail = convertToStockDetail(stockDO);

            String skuCode = stockDO.getSkuCode();
            List<StockDetail> stockDetails = stockDetailMap.computeIfAbsent(skuCode, k -> new ArrayList<>());
            stockDetails.add(stockDetail);
        }

        return stockDetailMap;
    }
    /**
     * 将 StockDO 转换为 StockDetail
     */
    private StockDetail convertToStockDetail(StockDO stockDO) {
        StockDetail stockDetail = new StockDetail();
        stockDetail.setSkuCode(stockDO.getSkuCode());
        stockDetail.setWarehouseId(stockDO.getWarehouseId());
        stockDetail.setWarehouseName(stockDO.getWarehouseName());
        stockDetail.setUnit(stockDO.getUnit());
        stockDetail.setUsableStock(stockDO.getUsableStock());
        stockDetail.setLockStock(stockDO.getLockStock());
        return stockDetail;
    }
    /**
     * 将库存详情挂载到 SKU 上
     */
    private void attachStockDetailsToSkus(List<GoodsSkuDetail> skuDetailList,
                                          Map<String, List<StockDetail>> stockDetailMap) {
        if (skuDetailList == null || skuDetailList.isEmpty()) {
            return;
        }

        for (GoodsSkuDetail skuDetail : skuDetailList) {
            String skuCode = skuDetail.getSkuCode();
            List<StockDetail> stockDetails = stockDetailMap.get(skuCode);

            if (stockDetails != null && !stockDetails.isEmpty()) {
                skuDetail.setStockDetailList(stockDetails);
            } else {
                skuDetail.setStockDetailList(new ArrayList<>());
            }
        }
    }
    /**
     *
     *
     *
     *
     */
    @Override
    public GoodsDetailForUserVO goodsDetailForUserQuery(Long id) {
        String cacheKey = GoodsRedisKeyConstants.getGoodsDetailUserKey(id);
        
        String cachedJson = redissonCacheService.get(cacheKey);
        
        if (RedissonCacheService.isRedisErrorValue(cachedJson)) {
            log.warn("Redis 服务异常，直接查询数据库, goodsId={}", id);
            return buildFallbackData(id);
        }
        if (RedissonCacheService.isCacheNullValue(cachedJson)) {
            log.info("商品详情缓存为空值，返回降级数据, goodsId={}", id);
            return buildFallbackData(id);
        }
        
        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                GoodsDetailForUserVO cachedResult = JsonUtils.toObject(cachedJson, GoodsDetailForUserVO.class);
                if (cachedResult != null) {
                    return cachedResult;
                }
            } catch (Exception e) {
                log.error("解析商品详情缓存失败, goodsId={}", id, e);
            }
        }
        
        String lockKey = GoodsRedisKeyConstants.getGoodsDetailUserLockKey(id);
        boolean locked = distributedLocker.getLock(lockKey);
        
        try {
            if (!locked) {
                log.warn("获取商品详情分布式锁失败, goodsId={}", id);
                throw new BizException(GoodsCodeEnum.LOCK_FAIL.getCode(), GoodsCodeEnum.LOCK_FAIL.getMessage());
            }

            GoodsDetailForUserVO result = loadGoodsDetailFromDB(id);
            if (result == null) {
                redissonCacheService.set(cacheKey, RedissonCacheService.CACHE_NULL_VALUE, GoodsRedisKeyConstants.GOODS_DETAIL_USER_NULL_EXP_TIME, TimeUnit.SECONDS);
                log.info("商品不存在，缓存空值并返回降级数据, goodsId={}", id);
                return buildFallbackData(id);
            }
            redissonCacheService.set(cacheKey, JsonUtils.toJsonString(result), GoodsRedisKeyConstants.GOODS_DETAIL_USER_EXP_TIME, TimeUnit.SECONDS);
            
            return result;
        } finally {
            if (locked) {
                distributedLocker.unlock(lockKey);
            }
        }
    }
    /**
     * 构建降级数据
     * 当商品不存在或查询失败时返回固定的降级数据
     * @param goodsId 商品ID
     * @return 降级数据
     */
    private GoodsDetailForUserVO buildFallbackData(Long goodsId) {
        GoodsDetailForUserVO fallback = new GoodsDetailForUserVO();
        fallback.setGoodsId(goodsId);
        fallback.setGoodsName("苹果14");
        fallback.setSubTitle("苹果14");
        fallback.setPictureAddress(Collections.emptyList());
        fallback.setSpecList(Collections.emptyList());
        fallback.setSkuList(Collections.emptyList());
        return fallback;
    }
    private GoodsDetailForUserVO loadGoodsDetailFromDB(Long id) {

        log.info("从DB加载商品详情, goodsId={}", id);
        GoodsDO goodsDO = this.getById(id);
        if (goodsDO == null) {
            return null;
        }

        List<GoodsSkuDO> skuList = goodsSkuService.lambdaQuery().eq(GoodsSkuDO::getGoodsId, id).list();

        if (skuList == null || skuList.isEmpty()) {
            return null;
        }

        GoodsDetailForUserVO vo = new GoodsDetailForUserVO();
        vo.setGoodsId(goodsDO.getId());
        vo.setGoodsName(goodsDO.getName());
        vo.setPictureAddress(JsonUtils.toList(goodsDO.getPictureAddress(), String.class));
        vo.setSubTitle(goodsDO.getTitle());

        processSkuAndSpec(skuList, vo);

        return vo;
    }

    /**
     * 核心逻辑：从 SKU 列表中提取规格项，并组装 SKU 信息
     */
    private void processSkuAndSpec(List<GoodsSkuDO> skuList, GoodsDetailForUserVO vo) {
        // 用于收集所有出现过的属性名和属性值
        Map<String, LinkedHashSet<String>> specMap = new LinkedHashMap<>();
        
        List<SkuInfo> skuInfoList = new ArrayList<>();

        for (GoodsSkuDO sku : skuList) {
            // 4.1 组装单个 SKU 信息
            SkuInfo skuInfo = new SkuInfo();
            skuInfo.setSkuId(sku.getId());
            skuInfo.setSkuCode(sku.getSkuCode());
            skuInfo.setPrice(sku.getPrice());
            skuInfo.setImage(sku.getSkuPictureAddress());
            
            // 解析 attributes JSON 字符串为 Map
            Map<String, String> attrMap = parseAttributesToMap(sku.getAttributes());
            skuInfo.setAttributes(attrMap);

            skuInfoList.add(skuInfo);

            // 4.2 提取规格数据
            for (Map.Entry<String, String> entry : attrMap.entrySet()) {
                String attrName = entry.getKey();
                String attrValue = entry.getValue();

                specMap.computeIfAbsent(attrName, k -> new LinkedHashSet<>());
                specMap.get(attrName).add(attrValue);
            }
        }

        // 5. 转换规格 Map 为 VO 列表
        List<SpecItem> specItemList = new ArrayList<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : specMap.entrySet()) {
            SpecItem specItem = new SpecItem();
            specItem.setName(entry.getKey());
            specItem.setValues(new ArrayList<>(entry.getValue()));
            specItemList.add(specItem);
        }

        vo.setSpecList(specItemList);
        vo.setSkuList(skuInfoList);
    }

    /**
     * 辅助方法：将 JSON 字符串或特定格式的属性数据转换为 Map
     * 输入示例: [{"attribute": "颜色", "attributeValue": "白色"}, ...]
     * 输出示例: {"颜色": "白色", ...}
     */
    private Map<String, String> parseAttributesToMap(String attributesJson) {

        
        try {
            // ✅ 使用新添加的工具方法
            List<Map<String, String>> attrList = JsonUtils.toMapList(attributesJson);
            
            Map<String, String> result = new HashMap<>();
            for (Map<String, String> item : attrList) {
                // 根据你提供的格式：key 是 "attribute", value 是 "attributeValue"
                String key = item.get("attribute");
                String value = item.get("attributeValue");
                if (key != null && value != null) {
                    result.put(key, value);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("解析商品属性失败: {}", attributesJson, e);
            return Collections.emptyMap();
        }
    }




    public Map<String, List<SkuName>> groupByAttribute(List<SkuName> skuList) {


        // 使用 groupingBy 进行分组
        return skuList.stream()
                .collect(Collectors.groupingBy(
                        SkuName::getAttribute,
                        Collectors.toList()
                ));
    }


    private GoodsSkuDO getDefaultGoodsSkuDO(List<GoodsSkuDO> goodsSkuDOList) {
        for (GoodsSkuDO goodsSkuDO : goodsSkuDOList) {
            if (goodsSkuDO.getDefaultSelect() == 1) {
                return goodsSkuDO;
            }
        }
        //如果没有默认选中配置 默认返回第一个
        return goodsSkuDOList.get(0);
    }

    private String buildDetailName(List<SkuNameVO> skuNameVOList, String title) {
        StringBuffer skuDescJointBuffer = new StringBuffer();
        skuNameVOList.forEach(a -> {
            skuDescJointBuffer.append(a.getDesc()).
                    append(" ");

        });
        return title + " " + skuDescJointBuffer;
    }


    @Override
    public List<ApiGoodsSkuVO> queryBySkuCode(List<String> skuCode) {


        ThreadLocalUtils.addIgnoreTableName(true);
        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().in(GoodsSkuDO::getSkuCode, skuCode).list();

        List<Long> goodsIdList = goodsSkuDOList.stream().map(GoodsSkuDO::getGoodsId).collect(Collectors.toList());
        ThreadLocalUtils.addIgnoreTableName(true);

        List<GoodsDO> goodsDOList = this.lambdaQuery().in(BaseEntity::getId, goodsIdList).list();
        Map<Long, GoodsDO> goodsMap = goodsDOList.stream()
                .collect(Collectors.toMap(GoodsDO::getId, Function.identity()));
        List<ApiGoodsSkuVO> goodsSkuVOS = BeanCopyUtils.copyBeanList(goodsSkuDOList, ApiGoodsSkuVO.class);
        goodsSkuVOS.forEach(a -> {
            Long goodsId = a.getGoodsId();
            GoodsDO goodsDO = goodsMap.get(goodsId);
            String name = goodsDO.getName();
            a.setGoodsName(name);
            a.setUpDownStatus(goodsDO.getUpDownStatus());
            a.setShopId(goodsDO.getShopId());
            a.setGoodsTitle(goodsDO.getTitle());

        });

        return goodsSkuVOS;
    }



    @Override
    public ApiGoodsSkuVO checkAndQueryGoods(CheckAndQueryGoodsDTO dto) {


        Long skuId = dto.getSkuId();
        ThreadLocalUtils.addIgnoreTableName(true);
        GoodsSkuDO goodsSkuDO = goodsSkuService.getById(skuId);
        Integer quantity = dto.getQuantity();
        List<ApiGoodsSkuVO> skuVOList = queryBySkuCode(Arrays.asList(goodsSkuDO.getSkuCode()));
        if (skuVOList.isEmpty()) {

            throw new BizException("商品不存在");
        }
        ApiGoodsSkuVO apiGoodsSkuVO = skuVOList.get(0);
        if (apiGoodsSkuVO.getUpDownStatus().equals(1)) {
            throw new BizException("商品已下架");
        }
        if (quantity > apiGoodsSkuVO.getStock()) {
            throw new BizException("商品库存不足");
        }

        return apiGoodsSkuVO;
    }


    @Override
    public SkuDetailVO queryBySkuId(Long skuId) {

        ThreadLocalUtils.addIgnoreTableName(true);
        GoodsSkuDO goodsSkuDO = goodsSkuService.getById(skuId);
        ThreadLocalUtils.addIgnoreTableName(true);
        GoodsDO goodsDO = this.getById(goodsSkuDO.getGoodsId());
//        Long shopId = goodsDO.getShopId();
//        List<ShopVO> shopVOList = systemService.shopQuery(Arrays.asList(shopId)).getData();
//        if (shopVOList.isEmpty()) {
//            return null;
//        }
//        Map<Long, ShopVO> shopVOMap = shopVOList.stream()
//                .collect(Collectors.toMap(ShopVO::getId, Function.identity()));
//
//        SkuDetailVO skuDetailVO = new SkuDetailVO();
//        skuDetailVO.setShopId(shopId);
//        skuDetailVO.setId(skuId);
//        skuDetailVO.setShopName(shopVOMap.get(shopId).getName());
//        skuDetailVO.setSkuPictureAddress(goodsSkuDO.getSkuPictureAddress());
//        skuDetailVO.setGoodsName(goodsDO.getName());
//        skuDetailVO.setSkuName(goodsSkuDO.getSkuName());
//        skuDetailVO.setPrice(goodsSkuDO.getPrice());

        return null;
    }

    @DistributedLock(key = "#dto.goodsId")
    @Transactional
    @Override
    public void upGoods(UpGoodsDTO dto) {

        Long goodsId = dto.getGoodsId();
        //validateUpGoods(goodsId);
        GoodsDO goodsDO = this.getById(goodsId);
        Long updateVersion = goodsDO.getVersion() + 1;
        Long goodsDOId = goodsDO.getId();
        boolean saveGoodsSyncEsRecord = true;
        Integer getUpDownStatus = 1;
        GoodsSyncEsRecordDO one = goodsSyncEsRecordService.lambdaQuery()
                .eq(GoodsSyncEsRecordDO::getGoodsId, goodsDOId)
                .one();
        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().eq(GoodsSkuDO::getGoodsId, goodsDOId).list();

        if (one != null) {
            saveGoodsSyncEsRecord = false;
        }

        //构建GoodsHistoryVersionDO
        GoodsHistoryVersionDO goodsHistoryVersionDO = BeanCopyUtils.copyBean(goodsDO, GoodsHistoryVersionDO.class);
        goodsHistoryVersionDO.setVersion(updateVersion);
        goodsHistoryVersionDO.setUpDownStatus(getUpDownStatus);
        //复制过来有值 赋值为null 每次自动生成
        goodsHistoryVersionDO.setId(null);
        List<GoodsSkuHistoryVersionDO> goodsSkuHistoryVersionDOS = BeanCopyUtils.copyBeanList(goodsSkuDOList, GoodsSkuHistoryVersionDO.class);
        goodsSkuHistoryVersionDOS.forEach(a -> {
            a.setVersion(updateVersion);
            a.setId(null);
        });
        //商品数据同步到ES中
        SyncGoodsInfoToEsMsg goodsInfoToEsMsg = buildSyncGoodsInfoToEsMsg(goodsId);
        /**
         * 更新DB
         */
        boolean update = this.lambdaUpdate().set(GoodsDO::getUpDownStatus, getUpDownStatus)
                .set(GoodsDO::getVersion, updateVersion)
                .eq(BaseEntity::getId, goodsDO.getId())
                .eq(GoodsDO::getVersion, goodsDO.getVersion()).update();
        if (!update) {
            throw new BizException("更新失败!");
        }
        boolean update2 = goodsSkuService.lambdaUpdate()
                .eq(GoodsSkuDO::getVersion, goodsDO.getVersion())
                .set(GoodsSkuDO::getVersion, updateVersion)
                .eq(GoodsSkuDO::getGoodsId, goodsDOId).update();
        if (!update2) {
            throw new BizException("更新失败!");
        }

        if (saveGoodsSyncEsRecord) {

            log.info("新增同步ES记录");
            GoodsSyncEsRecordDO goodsSyncEsRecordDO = buildGoodsSyncEsRecordDO(goodsDOId, updateVersion);
            goodsSyncEsRecordService.save(goodsSyncEsRecordDO);
        } else {
            log.info("更新同步ES记录");
            boolean update1 = goodsSyncEsRecordService.lambdaUpdate().
                    set(GoodsSyncEsRecordDO::getMaxVersion, updateVersion).
                    eq(GoodsSyncEsRecordDO::getGoodsId, goodsDOId).update();
            if (!update1) {
                throw new BizException("更新失败!");
            }
        }
        goodsHistoryVersionService.save(goodsHistoryVersionDO);
        goodsSkuHistoryVersionService.saveBatch(goodsSkuHistoryVersionDOS);
        /**
         * 每次商品修改 都需要同步到ES 这里方便测试
         * 上架触发同步ES操作
         *
         */

        String key = goodsDOId.toString() ;
        rocketMqClient.syncSendOrderly(TopicName.SAVE_GOODS_ES_TOPIC,
                JsonUtils.toJsonString(goodsInfoToEsMsg), key);

    }


    private SyncGoodsInfoToEsMsg buildSyncGoodsInfoToEsMsg(Long goodsId) {

        /**
         * 准备数据
         */
        GoodsDO goodsDO = this.getById(goodsId);
        Long shopId = goodsDO.getShopId();
        ShopDO shopDO = shopService.getById(shopId);
        String pictureAddress = goodsDO.getPictureAddress();
        //商品主图
        String mainImage = getMainImage(pictureAddress);
        //三级分类id
        Long categoryId3 = goodsDO.getCategoryId();
        GoodsCategoryDO category3 = goodsCategoryService.getById(categoryId3);
        //2级分类id
        Long categoryId2 = category3.getParentId();
        GoodsCategoryDO category2 = goodsCategoryService.getById(categoryId2);
        Long categoryId1 = category2.getParentId();
        GoodsCategoryDO category1 = goodsCategoryService.getById(categoryId1);

        GoodsBrandDO brandDO = goodsBrandService.getById(goodsDO.getBrandId());

        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().eq(GoodsSkuDO::getGoodsId, goodsId).list();
        //列表页展示的sku 默认取第一个 当然可以新增字段标记展示
        GoodsSkuDO firstGoodsSku = goodsSkuDOList.stream()
                .filter(sku -> sku.getDefaultSelect() != null && sku.getDefaultSelect() == 1)
                .findFirst()
                .orElse(goodsSkuDOList.get(0));
        List<SyncGoodsInfoToEsMsg.Attribute> attributes = buildAttribute(goodsSkuDOList);
        List<String> promptWordLabel = JsonUtils.toList(goodsDO.getPromptWordLabel(), String.class);

        List<String> extendedTags = JsonUtils.toList(goodsDO.getExtendedTags(), String.class);

        /**
         * 构建SyncGoodsInfoToEsMsg对象
         */
        SyncGoodsInfoToEsMsg goodsInfoToEsMsg = new SyncGoodsInfoToEsMsg();
        goodsInfoToEsMsg.setGoodsId(goodsDO.getId());
        goodsInfoToEsMsg.setGoodsName(goodsDO.getName());
        goodsInfoToEsMsg.setSubTitle(goodsDO.getTitle());
        goodsInfoToEsMsg.setShopId(goodsDO.getShopId());
        goodsInfoToEsMsg.setShopName(shopDO.getName());
        goodsInfoToEsMsg.setMainImage(mainImage);
        goodsInfoToEsMsg.setFirstLevelCategoryId(category1.getId());
        goodsInfoToEsMsg.setFirstLevelCategoryName(category1.getName());
        goodsInfoToEsMsg.setSecondaryLevelCategoryId(category2.getId());
        goodsInfoToEsMsg.setSecondaryLevelCategoryName(category2.getName());
        goodsInfoToEsMsg.setThreeLevelCategoryId(category3.getId());
        goodsInfoToEsMsg.setThreeLevelCategoryName(category3.getName());
        goodsInfoToEsMsg.setBrandId(brandDO.getId());
        goodsInfoToEsMsg.setBrandName(brandDO.getName());
        goodsInfoToEsMsg.setUpDownStatus(goodsDO.getUpDownStatus());
        goodsInfoToEsMsg.setTenantId(goodsDO.getTenantId());
        goodsInfoToEsMsg.setVersion(goodsDO.getVersion());
        goodsInfoToEsMsg.setSkuId(firstGoodsSku.getId());
        goodsInfoToEsMsg.setPrice(firstGoodsSku.getPrice().doubleValue());
        goodsInfoToEsMsg.setAttributeList(attributes);
        goodsInfoToEsMsg.setPromptWordLabel(promptWordLabel);
        goodsInfoToEsMsg.setExtendedTags(extendedTags);
        goodsInfoToEsMsg.setSkuName(firstGoodsSku.getAttributeDetail());
        goodsInfoToEsMsg.setCreateTime(goodsDO.getCreateTime().getTime());
        goodsInfoToEsMsg.setUpdateTime(goodsDO.getUpdateTime().getTime());
        return goodsInfoToEsMsg;
    }

    private List<SyncGoodsInfoToEsMsg.Attribute> buildAttribute(List<GoodsSkuDO> goodsSkuDOList) {

        List<SyncGoodsInfoToEsMsg.Attribute> sku = new ArrayList<>();
        goodsSkuDOList.forEach(goodsSkuDO -> {

            Long skuId = goodsSkuDO.getId();
            List<AttributesJson> list = JsonUtils.toList(goodsSkuDO.getAttributes(), AttributesJson.class);
            list.forEach(b -> {
                SyncGoodsInfoToEsMsg.Attribute attribute = new SyncGoodsInfoToEsMsg.Attribute();
                attribute.setSkuId(skuId);
                attribute.setAttribute(b.getAttribute());
                attribute.setAttributeValue(b.getAttributeValue());
                sku.add( attribute);
            });
        });

        return sku;
    }

    private String getMainImage(String pictureAddress) {

        List<String> list = JsonUtils.toList(pictureAddress, String.class);
        //默认返回第一张图片
        return list.get(0);
    }

    private GoodsSyncEsRecordDO buildGoodsSyncEsRecordDO(Long goodsId, Long updateVersion) {

        GoodsSyncEsRecordDO goodsSyncEsRecordDO = new GoodsSyncEsRecordDO();
        goodsSyncEsRecordDO.setGoodsId(goodsId);
        goodsSyncEsRecordDO.setMaxVersion(updateVersion);

        return goodsSyncEsRecordDO;
    }

    private void validateUpGoods(Long goodsId) {
        GoodsDO goodsDO = this.getById(goodsId);
        if (goodsDO == null) {
            throw new BizException("商品不存在");
        }
        if (goodsDO.getUpDownStatus() != 1) {
            throw new BizException("商品已上架");
        }

    }
}
