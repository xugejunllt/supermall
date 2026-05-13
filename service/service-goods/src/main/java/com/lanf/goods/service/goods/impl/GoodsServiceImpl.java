package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.lanf.goods.model.bo.*;
import com.lanf.goods.model.dto.CheckAndQueryGoodsDTO;
import com.lanf.goods.model.entity.*;
import com.lanf.goods.model.query.GoodsPageQuery;
import com.lanf.goods.model.vo.*;
import com.lanf.goods.service.goods.*;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.enums.EventCodeEnum;
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
    public void goodsAdd(GoodsAddDTO dto) {

        validateAddGoods(dto);

        /**
         * 计算DB数据
         */
        GoodsDO goodsDO = BeanCopyUtils.copyBean(dto, GoodsDO.class);
        Long goodsId = IdUtils.generateId();
        List<GoodsSkuAddBO> goodsSkuAddDTOList = getGoodsSkuAddDTOList(dto);
        List<GoodsSkuDO> goodsDOS = BeanCopyUtils.copyBeanList(goodsSkuAddDTOList, GoodsSkuDO.class);

        //搜索标签 --转成json字符串
        List<String> promptWordLabelList = IStringUtils.toList(dto.getPromptWordLabel(), ",");
        String promptWordLabel = JsonUtils.toJsonString(promptWordLabelList);
        //扩展表标签
        List<String> extendedTagsList = IStringUtils.toList(dto.getExtendedTags(), ",");
        String extendedTags = JsonUtils.toJsonString(extendedTagsList);
        //图片地址处理
        String pictureAddressList = JsonUtils.toJsonString(dto.getPictureAddressList());
        //版本号 用于追随
        Long version = 1L;

        /**
         * 初始化属性
         */

        goodsDOS.forEach(a -> {
            a.setGoodsId(goodsId);
            a.setVersion(version);
        });

        //构建SkuAttributeBO  UnitCodeSkuCodeBO
        List<SkuAttributeBO> skuAttributeVOS = buildSkuAttributeVO(goodsDOS);
        List<UnitCodeSkuCodeBO> codeSkuCodeVOList = buildUnitCodeSkuCodeVOList(goodsDOS);
        //设置商品主图
        goodsDO.setPictureAddress(pictureAddressList);
        //默认下架 手动进行上架
        goodsDO.setUpDownStatus(1);
        goodsDO.setVersion(version);
        goodsDO.setPromptWordLabel(promptWordLabel);
        goodsDO.setExtendedTags(extendedTags);
        goodsDO.setId(goodsId);
        goodsDO.setSkuAttributeDetail(JsonUtils.toJsonString(skuAttributeVOS));
        goodsDO.setUnitCodeSkuCode(JsonUtils.toJsonString(codeSkuCodeVOList));
        /**
         * 构建历史记录
         */
        GoodsHistoryVersionDO goodsHistoryVersionDO = BeanCopyUtils.copyBean(goodsDO, GoodsHistoryVersionDO.class);
        List<GoodsSkuHistoryVersionDO> goodsSkuHistoryVersionDOS = BeanCopyUtils.copyBeanList(goodsDOS, GoodsSkuHistoryVersionDO.class);

        this.save(goodsDO);
        goodsSkuService.saveBatch(goodsDOS);
        goodsHistoryVersionService.save(goodsHistoryVersionDO);
        goodsSkuHistoryVersionService.saveBatch(goodsSkuHistoryVersionDOS);


    }

    private void validateAddGoods(GoodsAddDTO dto) {

        Long categoryId = dto.getCategoryId();
        Long brandId = dto.getBrandId();
        String name = dto.getName();
        String code = dto.getCode();
//        GoodsDO goods1 = this.lambdaQuery().eq(GoodsDO::getCode, code).one();
////        if (goods1 != null) {
////            throw new BizException("商品已发布");
////        }
        List<GoodsSkuAddBO> goodsSkuAddDTOList1 = dto.getGoodsSkuAddDTOList();
        GoodsDO goods = this.lambdaQuery().eq(GoodsDO::getName, name).one();
        if (goods != null) {
            throw new BizException("商品名称已存在");
        }

        GoodsCategoryDO categoryDO = goodsCategoryService.lambdaQuery().eq(BaseEntity::getId, categoryId).eq(GoodsCategoryDO::getLevel, 3).one();
        if (categoryDO == null) {
            throw new BizException("商品分类不存在");
        }
        GoodsBrandDO brandDO = goodsBrandService.getById(brandId);
        if (brandDO == null) {
            throw new BizException("商品品牌不存在");

        }

    }

    private static List<GoodsSkuAddBO> getGoodsSkuAddDTOList(GoodsAddDTO dto) {
        List<GoodsSkuAddBO> goodsSkuAddDTOList = dto.getGoodsSkuAddDTOList();


        for (int i = 0; i < goodsSkuAddDTOList.size(); i++) {

            boolean defaultSelect = i == 0;
            GoodsSkuAddBO a = goodsSkuAddDTOList.get(i);
            if (defaultSelect) {
                //将第一组sku设为默认 这里通常前端选择第一组sku 页面没有写所以这里写死
                a.setDefaultSelect(1);
            }

            List<SkuNameBO> skuNameList = a.getSkuNameList();
            Collections.sort(skuNameList, Comparator.comparing(SkuNameBO::getSort));
            /**
             * 给每个属性值添加一个唯一id 在商品详细页-sku组合选择时方便计算
             */
            skuNameList.forEach(b -> {
                b.setUnitId(IdUtils.generateId());
                b.setDefaultSelect(defaultSelect ? 1 : 0);
            });
            /**
             *
             * [{"attribute":"颜色","desc":"白色","sort":1},
             * {"attribute":"内存","desc":"32g","sort":2}]
             *
             */
            String skuNameJson = JsonUtils.toJsonString(skuNameList);
            //拼接skuName
            StringBuffer v1 = new StringBuffer();
            skuNameList.forEach(b -> {

                v1.append(b.getAttribute()).
                        append(",").
                        append(b.getDesc()).
                        append(";");
            });
            a.setSkuNameJson(skuNameJson);
            a.setSkuName(v1.toString());

        }

        return goodsSkuAddDTOList;
    }


    @Override
    public PageResult<GoodsPageVO> goodsPage(GoodsPageQuery query) {
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
    public GoodsDetailVO goodsDetail(Long id) {

        GoodsDO goodsDO = this.getById(id);
        if (goodsDO == null) {
            throw new BizException("商品信息不存在");
        }

        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().eq(GoodsSkuDO::getGoodsId, id).list();
        GoodsCategoryDO categoryDO = goodsCategoryService.lambdaQuery().eq(BaseEntity::getId, goodsDO.getCategoryId()).eq(GoodsCategoryDO::getLevel, 3).one();
        GoodsBrandDO brandDO = goodsBrandService.getById(goodsDO.getBrandId());
        List<String> skuCodes = goodsSkuDOList.stream().map(GoodsSkuDO::getSkuCode).collect(Collectors.toList());
        Map<String, SkuCodeStockBO> stockBOMap = stockService.findBySkuCode(skuCodes);
        List<GoodsSkuDetailVO> goodsSkuDetailVOList = BeanCopyUtils.copyBeanList(goodsSkuDOList, GoodsSkuDetailVO.class);


        goodsSkuDetailVOList.forEach(a -> {
            /**
             * 添加库存
             */
            SkuCodeStockBO stockBO = stockBOMap.get(a.getSkuCode());
            a.setStock(stockBO.getTotalStock());

            /**
             * 图片地址转成CDN地址
             */
            String skuPictureAddress = cndUtils.replace(a.getSkuPictureAddress());
            a.setSkuPictureAddress(skuPictureAddress);

        });

        GoodsDetailVO goodsDetailVO = new GoodsDetailVO();
        BeanCopyUtils.copy(goodsDO, goodsDetailVO);
        goodsDetailVO.setCategoryName(categoryDO.getName());
        goodsDetailVO.setBrandName(brandDO.getName());
        goodsDetailVO.setGoodsSkuDetailVOList(goodsSkuDetailVOList);

        return goodsDetailVO;
    }


    /**
     * 能不用list就不用 丢掉了顺序
     *
     * @param id
     * @return
     */
    @Override
    public UserGoodsDetailVO userGoodsDetail(Long id) {

        //从缓存获取
        UserGoodsDetailVO detailVO = getCache(id);
        if ( detailVO != null) {
            log.info("从缓存获取商品详细");
        }

        if (detailVO == null) {
            log.info("从DB获取商品详细");
            //从DB加载
            String key = "lock:userGoodsDetail:" + id;
            try {

                boolean lock = distributedLocker.getLock(key);
                if (lock) {
                    detailVO = buildUserGoodsDetailVO(id);
                } else {
                    GoodsCodeEnum codeEnum = GoodsCodeEnum.LOCK_FAIL;
                    throw new BizException(codeEnum.getCode(), codeEnum.getMessage());
                }
                //加入缓存中
                addCache(id, detailVO);
            }  finally {
                distributedLocker.unlock(key);
            }
        }
        UserGoodsDetailVO goodsDetailVO = getCache(id);
        List<GoodsSkuVO> goodsSkuVOS = goodsDetailVO.getGoodsSkuVOList();
        //添加库存
        List<String> skuCodes = goodsSkuVOS.stream().map(GoodsSkuVO::getSkuCode).collect(Collectors.toList());
        Map<String, SkuCodeStockBO> stockBOMap = stockService.findBySkuCode(skuCodes);
        goodsSkuVOS.forEach(a -> {
            SkuCodeStockBO stockBO = stockBOMap.get(a.getSkuCode());
            a.setTotalStock(stockBO.getTotalStock());
        });

        return goodsDetailVO;
    }

    private UserGoodsDetailVO buildUserGoodsDetailVO(Long id) {

        GoodsDO goodsDO = this.getById(id);

        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().in(GoodsSkuDO::getGoodsId, id).list();

        /**
         * 准备数据
         */
        //获取默认选中的sku
        GoodsSkuDO defaultGoodsSkuDO = getDefaultGoodsSkuDO(goodsSkuDOList);
        List<SkuAttributeBO> skuAttributeVOS = JsonUtils.toList(goodsDO.getSkuAttributeDetail(), SkuAttributeBO.class);

        List<UnitCodeSkuCodeBO> codeSkuCodeVOList = JsonUtils.toList(goodsDO.getUnitCodeSkuCode(), UnitCodeSkuCodeBO.class);
        List<GoodsSkuVO> goodsSkuVOS = BeanCopyUtils.copyBeanList(goodsSkuDOList, GoodsSkuVO.class);
        /**
         * 构建返回数据
         */
        UserGoodsDetailVO goodsDetailVO = new UserGoodsDetailVO();
        goodsDetailVO.setId(goodsDO.getId());
        goodsDetailVO.setShopId(goodsDO.getShopId());
        goodsDetailVO.setPictureList(JsonUtils.toList(goodsDO.getPictureAddress(), String.class));
        goodsDetailVO.setGoodsName(goodsDO.getName());
        goodsDetailVO.setPrice(defaultGoodsSkuDO.getPrice());
        goodsDetailVO.setSkuAttributeVOList(skuAttributeVOS);
        goodsDetailVO.setUnitCodeSkuCodeVOList(codeSkuCodeVOList);
        goodsDetailVO.setGoodsSkuVOList(goodsSkuVOS);

        return goodsDetailVO;
    }

    private void addCache(Long keyPrefix, UserGoodsDetailVO value) {
        redissonCacheService.set(GoodsRedisKeyConstants.getGoodsDetailKey(keyPrefix),
                JsonUtils.toJsonString(value), GoodsRedisKeyConstants.GOODS_DETAIL_EXP_TIME, TimeUnit.SECONDS);
    }

    private UserGoodsDetailVO getCache(Long keyPrefix) {
        String cache = redissonCacheService.get(GoodsRedisKeyConstants.getGoodsDetailKey(keyPrefix));
        if (cache == null) {
            return null;
        }
        return JsonUtils.toObject(cache, UserGoodsDetailVO.class);
    }




    private List<UnitCodeSkuCodeBO> buildUnitCodeSkuCodeVOList(List<GoodsSkuDO> goodsSkuDOList) {


        List<UnitCodeSkuCodeBO> unitCodeSkuCodeVOList = new ArrayList<>();
        goodsSkuDOList.forEach(a -> {

            String skuNameJson = a.getSkuNameJson();
            List<SkuNameBO> nameJsonBOList2 = JsonUtils.toList(skuNameJson, SkuNameBO.class);

            List<Long> unitIdList = nameJsonBOList2.stream().map(SkuNameBO::getUnitId).collect(Collectors.toList());
            String unitCode = generateUnitCode(unitIdList);

            UnitCodeSkuCodeBO unitCodeSkuCodeVO = new UnitCodeSkuCodeBO();
            unitCodeSkuCodeVO.setUnitCode(unitCode);
            unitCodeSkuCodeVO.setSkuCode(a.getSkuCode());
            unitCodeSkuCodeVOList.add(unitCodeSkuCodeVO);

        });
        return unitCodeSkuCodeVOList;
    }

    private String generateUnitCode(List<Long> unitIdList) {

        //根据id值进行升序
        Collections.sort(unitIdList);
        StringBuilder stringBuilder = new StringBuilder();
        for (Long unitId : unitIdList) {
            stringBuilder.append(unitId).append(",");
        }
        //移除了最后一个字符 即末尾不会有 ","
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    private List<SkuAttributeBO> buildSkuAttributeVO(List<GoodsSkuDO> goodsSkuDOList) {


        /**
         * 解析所有 sku 的属性
         */
        List<SkuNameBO> allSkuNameJsonBOList = new ArrayList<>();
        for (GoodsSkuDO skuDO : goodsSkuDOList) {
            String skuNameJson = skuDO.getSkuNameJson();
            List<SkuNameBO> nameJsonBOList2 = JsonUtils.toList(skuNameJson, SkuNameBO.class);
            allSkuNameJsonBOList.addAll(nameJsonBOList2);
        }
        //进行分组 key:属性名称 value:属性值
        Map<String, List<SkuNameBO>> groupByAttribute = groupByAttribute(allSkuNameJsonBOList);

        /**
         * 解析属性名称
         */
        List<String> attributeNameSet = new ArrayList<>();
        //选取第一个sku属性进行解析
        String doSkuNameJson = goodsSkuDOList.get(0).getSkuNameJson();
        List<SkuNameBO> nameJsonBOList = JsonUtils.toList(doSkuNameJson, SkuNameBO.class);
        //按 sort进行降序 这样才能保证属性名称按顺序展示
        nameJsonBOList.sort(Comparator.comparing(SkuNameBO::getSort).reversed());
        for (SkuNameBO jsonBO : nameJsonBOList) {
            attributeNameSet.add(jsonBO.getAttribute());
        }
        attributeNameSet.forEach(a -> {
            SkuAttributeBO skuAttributeVO = new SkuAttributeBO();
            skuAttributeVO.setAttributeName(a);

        });

        /**
         * 构建 SkuAttributeVO
         */
        List<SkuAttributeBO> skuAttributeVOList = new ArrayList<>();
        for (String attributeName : attributeNameSet) {
            SkuAttributeBO skuAttributeVO = new SkuAttributeBO();
            List<SkuNameBO> skuNameJsonBOS = groupByAttribute.get(attributeName);
            //此时 SkuAttributeDetailVO 没有defaultSelect值
            List<SkuAttributeDetailBO> skuAttributeDetailVOS = BeanCopyUtils.copyBeanList(skuNameJsonBOS, SkuAttributeDetailBO.class);
            skuAttributeVO.setAttributeValue(skuAttributeDetailVOS);
            skuAttributeVO.setAttributeName(attributeName);
            skuAttributeVOList.add(skuAttributeVO);
        }

        return skuAttributeVOList;
    }

    public Map<String, List<SkuNameBO>> groupByAttribute(List<SkuNameBO> skuList) {


        // 使用 groupingBy 进行分组
        return skuList.stream()
                .collect(Collectors.groupingBy(
                        SkuNameBO::getAttribute,
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
    public void upDownStatus(UpDownStatusDTO dto) {


        Integer upDownStatus = dto.getUpDownStatus();
        Long id = dto.getId();
        GoodsDO goodsDOUpdate = new GoodsDO();
        goodsDOUpdate.setId(id);
        goodsDOUpdate.setUpDownStatus(upDownStatus);
        this.updateById(goodsDOUpdate);

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

    @DistributedLock(key = "#goodsId")
    @Transactional
    @Override
    public void upGoods(Long goodsId) {

        //validateUpGoods(goodsId);
        GoodsDO goodsDO = this.getById(goodsId);
        Long updateVersion = goodsDO.getVersion() + 1;
        Long goodsDOId = goodsDO.getId();
        boolean saveGoodsSyncEsRecord = true;
        Integer getUpDownStatus = 0;
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

        //商品数据同步到ES中
        SyncGoodsInfoToEsMsg goodsInfoToEsMsg = buildSyncGoodsInfoToEsMsg(goodsId);
        String key = goodsDOId + ":" + updateVersion + ":" + EventCodeEnum.GOODS_TO_ES.getCode();
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
        GoodsSkuDO firstGoodsSku = goodsSkuDOList.get(0);
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
        goodsInfoToEsMsg.setSkuName(firstGoodsSku.getSkuName());
        goodsInfoToEsMsg.setPrice(firstGoodsSku.getPrice().doubleValue());
        goodsInfoToEsMsg.setAttributes(attributes);
        goodsInfoToEsMsg.setPromptWordLabel(promptWordLabel);
        goodsInfoToEsMsg.setExtendedTags(extendedTags);

        return goodsInfoToEsMsg;
    }

    private List<SyncGoodsInfoToEsMsg.Attribute> buildAttribute(List<GoodsSkuDO> goodsSkuDOList) {

        List<SyncGoodsInfoToEsMsg.Attribute> sku = new ArrayList<>();

        goodsSkuDOList.forEach(a -> {
            String skuNameJson = a.getSkuNameJson();
            //SkuNameDTO
            List<SkuNameBO> list = JsonUtils.toList(skuNameJson, SkuNameBO.class);

            list.forEach(b -> {
                SyncGoodsInfoToEsMsg.Attribute attribute = new SyncGoodsInfoToEsMsg.Attribute();
                attribute.setAttrName(b.getAttribute());
                attribute.setAttrValue(b.getDesc());
                sku.add(attribute);
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
