package com.lanf.goods.service.base.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.goods.mapper.BaseGoodsMapper;
import com.lanf.goods.model.dto.BaseGoodsAddDTO;
import com.lanf.goods.model.dto.BaseGoodsSkuAddDTO;
import com.lanf.goods.model.entity.BaseGoodsDO;
import com.lanf.goods.model.entity.BaseGoodsSkuDO;
import com.lanf.goods.model.query.BaseGoodsPageQuery;
import com.lanf.goods.model.vo.*;
import com.lanf.goods.service.base.IBaseGoodsService;
import com.lanf.goods.service.base.IBaseGoodsSkuService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.security.utils.MerchantIdContext;
import com.lanf.storage.api.StorageApiService;
import com.lanf.storage.model.vo.StockVO;
import com.lanf.web.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 基础商品 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
@Service
public class BaseGoodsServiceImpl extends ServiceImpl<BaseGoodsMapper, BaseGoodsDO> implements IBaseGoodsService {

    @Autowired
    private IBaseGoodsSkuService baseGoodsSkuService;
    @Autowired
    private StorageApiService storageApiService;
    @Override
    @Transactional
    public void baseGoodsAdd(BaseGoodsAddDTO baseGoodsAdd) {

        BaseGoodsDO baseGoodsDO = new BaseGoodsDO();
        BeanCopyUtils.copy(baseGoodsAdd, baseGoodsDO);
        baseGoodsDO.setCode(CodeGenerateUtils.generaCode());
        List<BaseGoodsSkuDO> baseGoodsSkuSave = new ArrayList<>();

        List<List<BaseGoodsSkuAddDTO>> baseGoodsSkuAddList = baseGoodsAdd.getBaseGoodsSkuAddList();
        for (List<BaseGoodsSkuAddDTO> as : baseGoodsSkuAddList) {

            String skuCode = CodeGenerateUtils.generaCode();
            List<BaseGoodsSkuDO> baseGoodsSkuList = BeanCopyUtils.copyBeanList(as, BaseGoodsSkuDO.class);
            baseGoodsSkuList.forEach(a -> {
                a.setSkuCode(skuCode);
            });
            baseGoodsSkuSave.addAll(baseGoodsSkuList);
        }
        //
        this.save(baseGoodsDO);
        baseGoodsSkuSave.forEach(a -> {
            a.setGoodsId(baseGoodsDO.getId());
            a.setTenantId(MerchantIdContext.getMerchantId());
        });
        baseGoodsSkuService.saveBatch(baseGoodsSkuSave);

    }

    @Override
    public PageResult<BaseGoodsPageVO> baseGoodsPage(BaseGoodsPageQuery query) {

        IPage<BaseGoodsDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<BaseGoodsDO> purchaseStorageOrderPage = this.lambdaQuery().
                eq(!StringUtils.isEmpty(query.getGoodsCode()), BaseGoodsDO::getCode, query.getGoodsCode()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (purchaseStorageOrderPage.getRecords().isEmpty()) {

            return PageResult.emptyResult(BaseGoodsPageVO.class);
        }

        return PageResult.toPageResult(page, BaseGoodsPageVO.class);

    }

    @Override
    public BaseGoodsByCodeQueryVO baseGoodsByCodeQuery(String code) {

        BaseGoodsDO goodsDO = this.lambdaQuery().eq(BaseGoodsDO::getCode, code).one();
        if (goodsDO == null) {
            throw new BizException("商品信息不存在");
        }
        List<BaseGoodsSkuDO> baseGoodsSkuDOList = baseGoodsSkuService.lambdaQuery().eq(BaseGoodsSkuDO::getGoodsId, goodsDO.getId()).list();
        BaseGoodsByCodeQueryVO baseGoodsByCodeQueryVO = new BaseGoodsByCodeQueryVO();
        BeanCopyUtils.copy(goodsDO, baseGoodsByCodeQueryVO);
        List<BaseGoodsSkuByCodeQueryVO> baseGoodsSkuByCodeQueryVOS = BeanCopyUtils.copyBeanList(baseGoodsSkuDOList, BaseGoodsSkuByCodeQueryVO.class);
        Map<String, List<BaseGoodsSkuByCodeQueryVO>> skuMap = new HashMap<>();
        List<String> skuCodeList = new ArrayList<>();
        for (BaseGoodsSkuByCodeQueryVO ba : baseGoodsSkuByCodeQueryVOS) {
            String skuCode = ba.getSkuCode();
            List<BaseGoodsSkuByCodeQueryVO> baseGoodsSkuByCodeQueryVOS1 = skuMap.get(skuCode);
            if (baseGoodsSkuByCodeQueryVOS1 == null) {
                baseGoodsSkuByCodeQueryVOS1 = new ArrayList<>();
                skuMap.put(skuCode, baseGoodsSkuByCodeQueryVOS1);
            }
            baseGoodsSkuByCodeQueryVOS1.add(ba);
        }
        List<BaseGoodsSkuByCodeQueryVO> baseGoodsSkuByCodeQueryVOList = new ArrayList<>();
        Set<String> attributeSet = new HashSet<>();
        StringBuffer attributeSplit = new StringBuffer();
        skuMap.values().forEach(a -> {

            BaseGoodsSkuByCodeQueryVO vo = new BaseGoodsSkuByCodeQueryVO();
            List<BaseGoodsSkuDO> skuList = BeanCopyUtils.copyBeanList(a, BaseGoodsSkuDO.class);
            BaseGoodsSkuByCodeQueryVO byCodeQueryVO = a.get(0);
            BeanCopyUtils.copy(byCodeQueryVO, vo);
            List<SkuNameVO> skuNameVOList = new ArrayList<>();

            a.forEach(b -> {
                SkuNameVO skuNameVO = new SkuNameVO();
                skuNameVO.setAttribute(b.getAttribute());
                skuNameVO.setSort(b.getSort());
                skuNameVO.setDesc(b.getAttributeDesc());
                skuNameVOList.add(skuNameVO);
            });
            a.forEach(b -> {
                attributeSet.add(b.getAttribute());
            });
            String skuName = buildSkuName(skuList);
            //进行升序
            skuNameVOList.sort(Comparator.comparing(SkuNameVO::getSort));
            vo.setSkuNameVOList(skuNameVOList);
            vo.setSkuName(skuName);
            skuCodeList.add(vo.getSkuCode());
            baseGoodsSkuByCodeQueryVOList.add(vo);
        });

        List<String> attributeSplitList = new ArrayList<>(attributeSet);
        for (int i=0;i<attributeSplitList.size();i++){
            attributeSplit.append(attributeSplitList.get(i));
            if ( i!=attributeSet.size()-1){
                attributeSplit.append(",");
            }
        }
        /**
         * 添加库存
         */
        List<StockVO> stockVOList = storageApiService.querySkuCodeList(skuCodeList).getData();
        Map<String,StockVO> stockVOMap = new HashMap<>();
        stockVOList.forEach(a ->{
            stockVOMap.put(a.getSkuCode(),a);
        });
        baseGoodsSkuByCodeQueryVOList.forEach(a ->{
            String skuCode = a.getSkuCode();
            StockVO stockVO = stockVOMap.get(skuCode);
            if (stockVO == null){
                a.setUsableStock(0);
            } else {
                a.setUsableStock(stockVO.getUsableStock());
            }
        });

        baseGoodsByCodeQueryVO.setAttributeSplit(attributeSplit.toString());
        baseGoodsByCodeQueryVO.setBaseGoodsSkuByCodeQueryVOList(baseGoodsSkuByCodeQueryVOList);
        return baseGoodsByCodeQueryVO;
    }

    @Override
    public BaseGoodsBySkuCodeQueryVO baseGoodsBySkuCodeQuery(String skuCode) {

        List<BaseGoodsSkuDO> baseGoodsSkuDOList = baseGoodsSkuService.lambdaQuery().eq(BaseGoodsSkuDO::getSkuCode, skuCode).list();
        if (baseGoodsSkuDOList.isEmpty()) {

            return null;
        }
        Long goodsId = baseGoodsSkuDOList.get(0).getGoodsId();
        String goodsName = this.getById(goodsId).getName();
        String skuName = buildSkuName(baseGoodsSkuDOList);
        BaseGoodsBySkuCodeQueryVO vo = new BaseGoodsBySkuCodeQueryVO();
        vo.setSkuName(skuName);
        vo.setName(goodsName);
        return vo;
    }

    private String buildSkuName(List<BaseGoodsSkuDO> baseGoodsSkuDOList) {

        StringBuffer skuName = new StringBuffer();
        baseGoodsSkuDOList.forEach(a -> {

            skuName.append(a.getAttribute())
                    .append(",").
                    append(a.getAttributeDesc()).
                    append(";");

        });
        return skuName.toString();
    }

    @Override
    public List<BaseGoodsBySkuCodeQueryVO> baseGoodsBySkuCodeBathQuery(List<String> skuCodeList) {


        ThreadLocalUtils.addIgnoreTableName(true);
        List<BaseGoodsSkuDO> baseGoodsSkuDOList = baseGoodsSkuService.lambdaQuery().in(BaseGoodsSkuDO::getSkuCode, skuCodeList).list();
        if (baseGoodsSkuDOList.isEmpty()) {

            return null;
        }
        Set<Long> goodsIdSet = baseGoodsSkuDOList.stream().map(BaseGoodsSkuDO::getGoodsId).collect(Collectors.toSet());
        ThreadLocalUtils.addIgnoreTableName(true);
       List<BaseGoodsDO> baseGoodsDOS = this.lambdaQuery().in(BaseEntity::getId, goodsIdSet).list();
        Map<Long, BaseGoodsDO> baseGoodsMap = baseGoodsDOS.stream()
                .collect(Collectors.toMap(BaseGoodsDO::getId, Function.identity()));

        Map<String, List<BaseGoodsSkuDO>> baseGoodsSkuMap = new HashMap<>();
        baseGoodsSkuDOList.forEach(a -> {
            String skuCode = a.getSkuCode();

            List<BaseGoodsSkuDO> baseGoodsSkuDOS = baseGoodsSkuMap.get(skuCode);
            if (baseGoodsSkuDOS == null) {
                baseGoodsSkuDOS = new ArrayList<>();
                baseGoodsSkuMap.put(skuCode, baseGoodsSkuDOS);
            }
            baseGoodsSkuDOS.add(a);

        });
        Set<String> skuCodeSet = baseGoodsSkuMap.keySet();
        List<BaseGoodsBySkuCodeQueryVO> baseGoodsBySkuCodeQueryVOList = new ArrayList<>(skuCodeSet.size());

        for (String a : skuCodeSet) {

            BaseGoodsBySkuCodeQueryVO vo = new BaseGoodsBySkuCodeQueryVO();
            List<BaseGoodsSkuDO> baseGoodsSkuDOS = baseGoodsSkuMap.get(a);
            String skuName = buildSkuName(baseGoodsSkuDOS);
            Long goodsId = baseGoodsSkuDOS.get(0).getGoodsId();
            String goodsName = baseGoodsMap.get(goodsId).getName();
            vo.setSkuName(skuName);
            vo.setName(goodsName);
            vo.setSkuCode(a);
            baseGoodsBySkuCodeQueryVOList.add(vo);
        }



        return baseGoodsBySkuCodeQueryVOList;
    }

}
