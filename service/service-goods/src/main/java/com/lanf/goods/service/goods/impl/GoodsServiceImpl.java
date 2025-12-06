package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.goods.mapper.GoodsMapper;
import com.lanf.goods.model.bo.SkuCodeStockBO;
import com.lanf.goods.model.dto.*;
import com.lanf.goods.model.entity.*;
import com.lanf.goods.model.query.GoodsPageQuery;
import com.lanf.goods.model.query.UserGoodsPageQuery;
import com.lanf.goods.model.vo.*;
import com.lanf.goods.service.goods.*;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.enums.EventCodeEnum;
import com.lanf.rocketmq.model.message.GoodsAddMsg;
import com.lanf.rocketmq.model.message.SyncGoodsInfoToEsMsg;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.system.api.SystemService;
import com.lanf.system.model.vo.ShopVO;
import com.lanf.web.utils.CndUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
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
    private SystemService systemService;
    @Autowired
    private ISendMqMessageService sendMqMessageService;
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


    @DistributedLock(key = "#dto.code")
    @Transactional
    @Override
    public void goodsAdd(GoodsAddDTO dto) {

        validateAddGoods(dto);

        /**
         * 计算DB数据
         */
        GoodsDO goodsDO = BeanCopyUtils.copyBean(dto, GoodsDO.class);
        List<GoodsSkuAddDTO> goodsSkuAddDTOList = getGoodsSkuAddDTOList(dto);
        List<GoodsSkuDO> goodsDOS = BeanCopyUtils.copyBeanList(goodsSkuAddDTOList, GoodsSkuDO.class);
        //搜索标签 --转成json字符串
        List<String> promptWordLabelList = IStringUtils.toList(dto.getPromptWordLabel(), ",");
        String promptWordLabel = JsonUtils.toJsonString(promptWordLabelList);
        //扩展表标签
        List<String> extendedTagsList = IStringUtils.toList(dto.getExtendedTags(), ",");
        String extendedTags = JsonUtils.toJsonString(extendedTagsList);
        //图片地址处理
        String pictureAddressList = JsonUtils.toJsonString(dto.getPictureAddressList());
        /**
         * 初始化属性
         */
        //版本号 用于追随
        Long version = 1L;
        //设置商品主图
        goodsDO.setPictureAddress(pictureAddressList);
        //默认下架 手动进行上架
        goodsDO.setUpDownStatus(1);
        goodsDO.setVersion(version);
        goodsDO.setPromptWordLabel(promptWordLabel);
        goodsDO.setExtendedTags(extendedTags);
        this.save(goodsDO);
        goodsDOS.forEach(a -> {
            a.setGoodsId(goodsDO.getId());
            a.setVersion(version);
        });
        //构建历史记录
        GoodsHistoryVersionDO goodsHistoryVersionDO = BeanCopyUtils.copyBean(goodsDO, GoodsHistoryVersionDO.class);
        List<GoodsSkuHistoryVersionDO> goodsSkuHistoryVersionDOS = BeanCopyUtils.copyBeanList(goodsDOS, GoodsSkuHistoryVersionDO.class);

        goodsSkuService.saveBatch(goodsDOS);
        goodsHistoryVersionService.save(goodsHistoryVersionDO);
        goodsSkuHistoryVersionService.saveBatch(goodsSkuHistoryVersionDOS);


    }

    private void validateAddGoods(GoodsAddDTO dto) {

        Long categoryId = dto.getCategoryId();
        Long brandId = dto.getBrandId();
        String name = dto.getName();
        String code = dto.getCode();
        GoodsDO goods1 = this.lambdaQuery().eq(GoodsDO::getCode, code).one();
        if (goods1 != null) {
            throw new BizException("商品已发布");
        }
        List<GoodsSkuAddDTO> goodsSkuAddDTOList1 = dto.getGoodsSkuAddDTOList();
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

    private static List<GoodsSkuAddDTO> getGoodsSkuAddDTOList(GoodsAddDTO dto) {
        List<GoodsSkuAddDTO> goodsSkuAddDTOList = dto.getGoodsSkuAddDTOList();
        goodsSkuAddDTOList.forEach(a -> {
            List<SkuNameDTO> skuNameList = a.getSkuNameList();
            Collections.sort(skuNameList, Comparator.comparing(SkuNameDTO::getSort));
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

        });
        return goodsSkuAddDTOList;
    }

    private void sendToMq(GoodsDO goodsDO, List<GoodsSkuDO> goodsDOS) {

        //将sku集合根据sort进行升序，取第一条sku的价格
        Collections.sort(goodsDOS, Comparator.comparing(GoodsSkuDO::getSort));
        GoodsSkuDO goodsSkuDO = goodsDOS.get(0);
        //构建GoodsAddMsg
        GoodsAddMsg msg = new GoodsAddMsg();
        msg.setGoodsId(goodsDO.getId());
        msg.setCode(goodsDO.getCode());
        msg.setName(goodsDO.getName());
        msg.setUpDownStatus(goodsDO.getUpDownStatus());
        msg.setPrice(goodsSkuDO.getPrice());
        msg.setPicture(goodsSkuDO.getSkuPictureAddress());
        msg.setCreateTime(goodsDO.getCreateTime());
        msg.setUpdateTime(goodsDO.getUpdateTime());
        msg.setSearchWords(goodsDO.getName());
        //发送到mq
        // sendMqMessageService.sendMessage(TopicName.SAVE_GOODS_ES_TOPIC,msg);

    }


    @Override
    public PageResult<GoodsPageVO> goodsPage(GoodsPageQuery query) {
        IPage<GoodsDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<GoodsDO> purchaseStorageOrderPage = this.lambdaQuery().
                eq(!org.apache.commons.lang3.StringUtils.isEmpty(query.getCode()), GoodsDO::getCode, query.getCode()).
                eq(!org.apache.commons.lang3.StringUtils.isEmpty(query.getName()), GoodsDO::getName, query.getName()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (purchaseStorageOrderPage.getRecords().isEmpty()) {

            return PageResult.emptyResult(GoodsPageVO.class);
        }

        return PageResult.toPageResult(page, GoodsPageVO.class);
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

    @Override
    public PageResult<UserGoodsPageVO> userGoodsPage(UserGoodsPageQuery query) {


        IPage<GoodsDO> page = new Page<>(query.getPage(), query.getPageSize());
        ThreadLocalUtils.addIgnoreTableName(true);
        IPage<GoodsDO> purchaseStorageOrderPage = this.lambdaQuery().
                like(!org.apache.commons.lang3.StringUtils.isEmpty(query.getName()), GoodsDO::getName, query.getName()).
                like(!org.apache.commons.lang3.StringUtils.isEmpty(query.getTitle()), GoodsDO::getTitle, query.getTitle()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);
        if (purchaseStorageOrderPage.getRecords().isEmpty()) {

            return PageResult.emptyResult(UserGoodsPageVO.class);
        }
        List<GoodsDO> records = purchaseStorageOrderPage.getRecords();
        List<Long> goodsIdList = records.stream().map(GoodsDO::getId).collect(Collectors.toList());
        ThreadLocalUtils.addIgnoreTableName(true);
        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().in(GoodsSkuDO::getGoodsId, goodsIdList).list();
        Map<Long, List<GoodsSkuDO>> skuMap = new HashMap<>();
        for (GoodsSkuDO s : goodsSkuDOList) {
            List<GoodsSkuDO> goodsSkuDOS = skuMap.get(s.getGoodsId());
            if (goodsSkuDOS == null) {
                goodsSkuDOS = new ArrayList<>();
                skuMap.put(s.getGoodsId(), goodsSkuDOS);
            }
            goodsSkuDOS.add(s);
        }
        List<UserGoodsPageVO> userGoodsPageVOS = new ArrayList<>();
        records.forEach(a -> {
            UserGoodsPageVO userGoodsPageVO = new UserGoodsPageVO();
            userGoodsPageVOS.add(userGoodsPageVO);
            userGoodsPageVO.setId(a.getId());
            userGoodsPageVO.setName(a.getName());
            String picture = a.getPictureAddress().split(",")[0];
            userGoodsPageVO.setPicture(picture);
            userGoodsPageVO.setShopId(a.getShopId());
            //取排序码最大sku展示
            List<GoodsSkuDO> goodsSkuDOS = skuMap.get(a.getId());
            Collections.sort(goodsSkuDOS, Comparator.comparing(GoodsSkuDO::getSort).reversed());
            GoodsSkuDO goodsSkuDO = goodsSkuDOS.get(0);
            userGoodsPageVO.setPrice(goodsSkuDO.getPrice());

        });

        return PageResult.toPageResult(purchaseStorageOrderPage, UserGoodsPageVO.class, userGoodsPageVOS);

    }

    /**
     * 能不用list就不用 丢掉了顺序
     *
     * @param id
     * @return
     */
    @Override
    public UserGoodsDetailVO userGoodsDetail(Long id) {

        ThreadLocalUtils.addIgnoreTableName(true);
        GoodsDO goodsDO = this.getById(id);
        ThreadLocalUtils.addIgnoreTableName(true);

        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().in(GoodsSkuDO::getGoodsId, id).list();
        //
        Collections.sort(goodsSkuDOList, Comparator.comparing(GoodsSkuDO::getSort).reversed());
        //默认选中的sku
        GoodsSkuDO goodsSkuDO = goodsSkuDOList.get(0);
        BigDecimal price = goodsSkuDO.getPrice();
        String pictureAddress = goodsDO.getPictureAddress();
        //获取默认sku
        List<SkuNameVO> skuNameVOList = JsonUtils.toList(goodsSkuDO.getSkuNameJson(), SkuNameVO.class);
        //构建SkuAttributeVO

        //按排序码大小展示属性名称
        Collections.sort(skuNameVOList, Comparator.comparing(SkuNameVO::getSort));
        List<SkuAttributeVO> skuAttributeVOSet = buildSkuAttributeVO(skuNameVOList);
        //给属性添加属性值
        addAttributeValue(skuAttributeVOSet, goodsSkuDOList);
        //构建detailName
        String detailName = buildDetailName(skuNameVOList, goodsDO.getTitle());
        //key：属性值拼接 value:skuId
        Map<String, Long> skuIdVOMap = new HashMap<>();

        goodsSkuDOList.forEach(a -> {

            List<SkuNameVO> skuNameVOList2 = JsonUtils.toList(a.getSkuNameJson(), SkuNameVO.class);
            Collections.sort(skuNameVOList2, Comparator.comparing(SkuNameVO::getSort));

            StringBuffer keyBuffer = new StringBuffer();
            for (int i = 0; i < skuNameVOList2.size(); i++) {

                keyBuffer.append(skuNameVOList2.get(i).getDesc());
                if (i != skuNameVOList2.size() - 1) {
                    //不是最后一个
                    keyBuffer.append(",");
                }

            }
            skuIdVOMap.put(keyBuffer.toString(), a.getId());
        });
        //截取pictureList
        List<String> pictureList = IStringUtils.toList(goodsDO.getPictureAddress(), ",");

        /**
         * 构建返回信息
         */
        UserGoodsDetailVO goodsDetailVO = new UserGoodsDetailVO();
        goodsDetailVO.setId(id);
        goodsDetailVO.setPrice(price);
        goodsDetailVO.setShopId(goodsDO.getShopId());
        goodsDetailVO.setPictureList(pictureList);
        goodsDetailVO.setSkuAttributeVOList(skuAttributeVOSet);
        goodsDetailVO.setDetailName(detailName);
        goodsDetailVO.setSkuIdVOMap(skuIdVOMap);
        return goodsDetailVO;
    }


    private String buildDetailName(List<SkuNameVO> skuNameVOList, String title) {
        StringBuffer skuDescJointBuffer = new StringBuffer();
        skuNameVOList.forEach(a -> {
            skuDescJointBuffer.append(a.getDesc()).
                    append(" ");

        });
        return title + " " + skuDescJointBuffer;
    }

    private String buildSkuName(List<SkuNameVO> skuNameVOList) {
        StringBuffer stringBuffer = new StringBuffer();
        skuNameVOList.forEach(a -> {
            stringBuffer.append(a.getAttribute()).
                    append(";");

        });
        return stringBuffer.toString();
    }

    private void addAttributeValue(List<SkuAttributeVO> skuAttributeVOSet, List<GoodsSkuDO> goodsSkuDOList) {
        skuAttributeVOSet.forEach(a -> {
            String attributeName = a.getAttributeName();
            List<String> attributeValue = new ArrayList<>();
            a.setAttributeValue(attributeValue);

            goodsSkuDOList.forEach(b -> {
                String skuName1 = b.getSkuNameJson();
                List<SkuNameVO> skuNameVOList1 = JsonUtils.toList(skuName1, SkuNameVO.class);
                for (SkuNameVO sd : skuNameVOList1) {
                    if (attributeName.equals(sd.getAttribute())) {
                        if (!attributeValue.contains(sd.getDesc())) {
                            attributeValue.add(sd.getDesc());
                        }

                    }
                }

            });


        });

    }

    private List<SkuAttributeVO> buildSkuAttributeVO(List<SkuNameVO> skuNameVOList) {
        List<SkuAttributeVO> skuAttributeVOSet = new ArrayList<>();
        //取出所有属性
        List<String> attributeSet = new ArrayList<>();
        for (SkuNameVO a : skuNameVOList) {
            attributeSet.add(a.getAttribute());
        }

        //添加属性
        for (String s : attributeSet) {
            SkuAttributeVO skuAttributeVO = new SkuAttributeVO();
            skuAttributeVO.setAttributeName(s);
            skuAttributeVOSet.add(skuAttributeVO);
        }
        return skuAttributeVOSet;
    }

    private Map<String, EmptyCartGoodsSkuVO> buildGoodsSkuMap(List<GoodsSkuDO> goodsSkuDOList) {

        Map<String, EmptyCartGoodsSkuVO> goodsSkuMap = new HashMap<>();
        goodsSkuDOList.forEach(a -> {

            String skuName1 = a.getSkuNameJson();
            List<SkuNameVO> skuNameVOList1 = JsonUtils.toList(skuName1, SkuNameVO.class);
            String key = buildSkuName(skuNameVOList1);
            EmptyCartGoodsSkuVO goodsSkuVO = new EmptyCartGoodsSkuVO();
            BeanCopyUtils.copy(a, goodsSkuVO);
            goodsSkuMap.put(key, goodsSkuVO);
        });
        return goodsSkuMap;
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
        Long shopId = goodsDO.getShopId();
        List<ShopVO> shopVOList = systemService.shopQuery(Arrays.asList(shopId)).getData();
        if (shopVOList.isEmpty()) {
            return null;
        }
        Map<Long, ShopVO> shopVOMap = shopVOList.stream()
                .collect(Collectors.toMap(ShopVO::getId, Function.identity()));

        SkuDetailVO skuDetailVO = new SkuDetailVO();
        skuDetailVO.setShopId(shopId);
        skuDetailVO.setId(skuId);
        skuDetailVO.setShopName(shopVOMap.get(shopId).getName());
        skuDetailVO.setSkuPictureAddress(goodsSkuDO.getSkuPictureAddress());
        skuDetailVO.setGoodsName(goodsDO.getName());
        skuDetailVO.setSkuName(goodsSkuDO.getSkuName());
        skuDetailVO.setPrice(goodsSkuDO.getPrice());

        return skuDetailVO;
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
        String key = goodsDOId+":"+updateVersion+":"+ EventCodeEnum.GOODS_TO_ES.getCode();
        rocketMqClient.syncSendOrderly(TopicName.SAVE_GOODS_ES_TOPIC,
                JsonUtils.toJsonString(goodsInfoToEsMsg),key);

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
            List<SkuNameDTO> list = JsonUtils.toList(skuNameJson, SkuNameDTO.class);

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
