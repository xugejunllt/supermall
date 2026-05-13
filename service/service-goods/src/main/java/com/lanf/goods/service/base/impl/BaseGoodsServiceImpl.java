package com.lanf.goods.service.base.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.goods.model.bo.AttributesJson;
import com.lanf.api.goods.model.dto.AddBaseGoodsDTO;
import com.lanf.api.goods.model.dto.BaseGoodsSkuAddDTO;
import com.lanf.api.goods.model.query.BaseGoodsPageQuery;
import com.lanf.api.goods.model.vo.BaseGoodsByCodeVO;
import com.lanf.api.goods.model.vo.BaseGoodsBySkuCodeVO;
import com.lanf.api.goods.model.vo.BaseGoodsPageVO;
import com.lanf.api.goods.model.vo.BaseGoodsSkuByCodeQueryVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.IdUtils;
import com.lanf.goods.mapper.BaseGoodsMapper;
import com.lanf.goods.model.entity.BaseGoodsDO;
import com.lanf.goods.model.entity.BaseGoodsSkuDO;
import com.lanf.goods.service.base.IBaseGoodsService;
import com.lanf.goods.service.base.IBaseGoodsSkuService;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.mybatis.base.BaseEntity;
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
    private IStockService stockService;

    @Override
    @Transactional
    public void addBaseGoods(AddBaseGoodsDTO baseGoodsAdd) {
        Long goodsId = IdUtils.generateId();
        
        BaseGoodsDO baseGoodsDO = buildBaseGoodsDO(baseGoodsAdd, goodsId);
        
        List<BaseGoodsSkuDO> baseGoodsSkuSave = buildBaseGoodsSkuList(baseGoodsAdd, goodsId);

        this.save(baseGoodsDO);
        baseGoodsSkuService.saveBatch(baseGoodsSkuSave);
    }

    /**
     * 构建基础商品对象
     */
    private BaseGoodsDO buildBaseGoodsDO(AddBaseGoodsDTO baseGoodsAdd, Long goodsId) {
        BaseGoodsDO baseGoodsDO = new BaseGoodsDO();
        baseGoodsDO.setId(goodsId);
        baseGoodsDO.setName(baseGoodsAdd.getName());
        baseGoodsDO.setPictureAddress(baseGoodsAdd.getPictureAddress());
        baseGoodsDO.setCode(CodeGenerateUtils.generaCode());
        return baseGoodsDO;
    }

    /**
     * 构建基础商品SKU列表
     */
    private List<BaseGoodsSkuDO> buildBaseGoodsSkuList(AddBaseGoodsDTO baseGoodsAdd, Long goodsId) {
        List<BaseGoodsSkuDO> baseGoodsSkuSave = new ArrayList<>();
        List<List<BaseGoodsSkuAddDTO>> baseGoodsSkuAddList = baseGoodsAdd.getBaseGoodsSkuAddList();
        
        for (List<BaseGoodsSkuAddDTO> skuGroup : baseGoodsSkuAddList) {
            BaseGoodsSkuDO baseGoodsSkuDO = buildBaseGoodsSkuDO(skuGroup, goodsId);
            baseGoodsSkuSave.add(baseGoodsSkuDO);
        }
        
        return baseGoodsSkuSave;
    }

    /**
     * 构建单个基础商品SKU对象
     */
    private BaseGoodsSkuDO buildBaseGoodsSkuDO(List<BaseGoodsSkuAddDTO> skuGroup, Long goodsId) {
        List<AttributesJson> attributes = buildAttributesList(skuGroup);
        String attributeDetail = buildAttributeDetailString(attributes);
        String skuCode = CodeGenerateUtils.generaCode();
        
        BaseGoodsSkuDO baseGoodsSkuDO = new BaseGoodsSkuDO();
        baseGoodsSkuDO.setSkuCode(skuCode);
        baseGoodsSkuDO.setGoodsId(goodsId);
        baseGoodsSkuDO.setAttributes(JsonUtils.toJsonString(attributes));
        baseGoodsSkuDO.setAttributeDetail(attributeDetail);
        baseGoodsSkuDO.setSort(skuGroup.get(0).getSort());
        
        return baseGoodsSkuDO;
    }

    /**
     * 构建属性列表
     */
    private List<AttributesJson> buildAttributesList(List<BaseGoodsSkuAddDTO> skuGroup) {
        List<AttributesJson> attributes = new ArrayList<>();
        for (BaseGoodsSkuAddDTO skuAddDTO : skuGroup) {
            AttributesJson attribute = new AttributesJson();
            attribute.setAttribute(skuAddDTO.getAttribute());
            attribute.setAttributeValue(skuAddDTO.getAttributeDesc());
            attributes.add(attribute);
        }
        return attributes;
    }

    /**
     * 构建属性详情字符串
     * 格式：attribute,attributeValue;attribute,attributeValue;
     */
    private String buildAttributeDetailString(List<AttributesJson> attributes) {
        return attributes.stream()
                .map(attr -> attr.getAttribute() + "," + attr.getAttributeValue() + ";")
                .collect(Collectors.joining());
    }

    /**
     * 保存基础商品和SKU
     */
    private void saveBaseGoods(BaseGoodsDO baseGoodsDO, List<BaseGoodsSkuDO> baseGoodsSkuSave) {

    }

    @Override
    public PageResult<BaseGoodsPageVO> baseGoodsPageQuery(BaseGoodsPageQuery query) {

        IPage<BaseGoodsDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<BaseGoodsDO> result = this.lambdaQuery().
                eq(!StringUtils.isEmpty(query.getGoodsCode()), BaseGoodsDO::getCode, query.getGoodsCode()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (result.getRecords().isEmpty()) {

            return PageResult.emptyResult();
        }
        PageResult<BaseGoodsPageVO> resultVo = new PageResult<>();
        resultVo.setRecords(BeanCopyUtils.copyBeanList(result.getRecords(), BaseGoodsPageVO.class));
        resultVo.setTotal(result.getTotal());
        resultVo.setSize(result.getSize());
        return resultVo;

    }

    @Override
    public BaseGoodsByCodeVO baseGoodsByCodeQuery(String code) {
        BaseGoodsDO goodsDO = getBaseGoodsByCode(code);

        List<BaseGoodsSkuDO> baseGoodsSkuDOList =  baseGoodsSkuService.lambdaQuery()
                .eq(BaseGoodsSkuDO::getGoodsId, goodsDO.getId())
                .list();
        
        List<BaseGoodsSkuByCodeQueryVO> skuVOList = buildBaseGoodsSkuVOList(baseGoodsSkuDOList);

        return buildBaseGoodsByCodeVO(goodsDO, skuVOList);
    }

    /**
     * 根据商品编码查询基础商品
     */
    private BaseGoodsDO getBaseGoodsByCode(String code) {
        BaseGoodsDO goodsDO = this.lambdaQuery().eq(BaseGoodsDO::getCode, code).one();
        if (goodsDO == null) {
            throw new BizException("商品信息不存在");
        }
        return goodsDO;
    }



    /**
     * 构建基础商品SKU VO列表
     */
    private List<BaseGoodsSkuByCodeQueryVO> buildBaseGoodsSkuVOList(List<BaseGoodsSkuDO> baseGoodsSkuDOList) {
        List<BaseGoodsSkuByCodeQueryVO> skuVOList = new ArrayList<>();
        for (BaseGoodsSkuDO baseGoodsSkuDO : baseGoodsSkuDOList) {
            BaseGoodsSkuByCodeQueryVO skuVO = buildBaseGoodsSkuVO(baseGoodsSkuDO);
            skuVOList.add(skuVO);
        }
        return skuVOList;
    }

    /**
     * 构建单个基础商品SKU VO
     */
    private BaseGoodsSkuByCodeQueryVO buildBaseGoodsSkuVO(BaseGoodsSkuDO baseGoodsSkuDO) {
        BaseGoodsSkuByCodeQueryVO skuVO = new BaseGoodsSkuByCodeQueryVO();
        skuVO.setSkuCode(baseGoodsSkuDO.getSkuCode());
        skuVO.setSort(baseGoodsSkuDO.getSort());
        skuVO.setAttributeDetail(baseGoodsSkuDO.getAttributeDetail());
        skuVO.setAttributes(JsonUtils.toList(baseGoodsSkuDO.getAttributes(), AttributesJson.class));
        skuVO.setBaseGoodsSkuId(baseGoodsSkuDO.getId());
        return skuVO;
    }





    /**
     * 构建基础商品按编码查询VO
     */
    private BaseGoodsByCodeVO buildBaseGoodsByCodeVO(BaseGoodsDO goodsDO, List<BaseGoodsSkuByCodeQueryVO> skuVOList) {
        BaseGoodsByCodeVO baseGoodsByCodeVO = new BaseGoodsByCodeVO();
        baseGoodsByCodeVO.setName(goodsDO.getName());
        baseGoodsByCodeVO.setGoodsId(goodsDO.getId());
        baseGoodsByCodeVO.setBaseGoodsSkuByCodeQueryVOList(skuVOList);
        return baseGoodsByCodeVO;
    }

    @Override
    public BaseGoodsBySkuCodeVO baseGoodsBySkuCodeQuery(String skuCode) {

        List<BaseGoodsSkuDO> baseGoodsSkuDOList = baseGoodsSkuService.lambdaQuery().eq(BaseGoodsSkuDO::getSkuCode, skuCode).list();
        if (baseGoodsSkuDOList.isEmpty()) {

            return null;
        }
        Long goodsId = baseGoodsSkuDOList.get(0).getGoodsId();
        String goodsName = this.getById(goodsId).getName();
        String skuName = buildSkuName(baseGoodsSkuDOList);
        BaseGoodsBySkuCodeVO vo = new BaseGoodsBySkuCodeVO();
        vo.setSkuName(skuName);
        vo.setName(goodsName);
        return vo;
    }

    private String buildSkuName(List<BaseGoodsSkuDO> baseGoodsSkuDOList) {

        StringBuffer skuName = new StringBuffer();
        baseGoodsSkuDOList.forEach(a -> {

//            skuName.append(a.getAttribute())
//                    .append(",").
//                    append(a.getAttributeDesc()).
//                    append(";");

        });
        return skuName.toString();
    }

    @Override
    public List<BaseGoodsBySkuCodeVO> baseGoodsBySkuCodeBathQuery(List<String> skuCodeList) {


        ThreadLocalUtils.addIgnoreTableName(true);
        List<BaseGoodsSkuDO> baseGoodsSkuDOList = baseGoodsSkuService.lambdaQuery().in(BaseGoodsSkuDO::getSkuCode, skuCodeList).list();
        if (baseGoodsSkuDOList.isEmpty()) {

            return new ArrayList<>();
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
        List<BaseGoodsBySkuCodeVO> baseGoodsBySkuCodeQueryVOList = new ArrayList<>(skuCodeSet.size());

        for (String a : skuCodeSet) {

            BaseGoodsBySkuCodeVO vo = new BaseGoodsBySkuCodeVO();
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
