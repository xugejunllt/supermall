package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.goods.model.dto.CalculateOrderTotalAmountDTO;
import com.lanf.api.goods.model.query.GoodsSkuPageQuery;
import com.lanf.api.goods.model.vo.CalculateOrderTotalAmountVO;
import com.lanf.api.goods.model.vo.GoodsSkuPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.mapper.GoodsSkuMapper;
import com.lanf.goods.model.entity.GoodsSkuDO;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.utils.GoodsServiceUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Service
public class GoodsSkuServiceImpl extends ServiceImpl<GoodsSkuMapper, GoodsSkuDO> implements IGoodsSkuService {

    @Override
    public CalculateOrderTotalAmountVO calculateOrderTotalAmount(CalculateOrderTotalAmountDTO dto) {

        Long skuId = dto.getSkuId();
        GoodsSkuDO goodsSkuDO = this.getById(skuId);
        BigDecimal price = goodsSkuDO.getPrice();
        Integer quantity = dto.getQuantity();
        //订单总金额
        BigDecimal totalAmount =  GoodsServiceUtils.calculateTotalAmount(price, quantity);
        CalculateOrderTotalAmountVO vo = new CalculateOrderTotalAmountVO();
        vo.setTotalAmount(totalAmount);


        return vo;
    }

    @Override
    public PageResult<GoodsSkuPageVO> goodsSkuPageQuery(GoodsSkuPageQuery query) {
        Page<GoodsSkuDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<GoodsSkuDO> skuPage = this.page(page, new QueryWrapper<GoodsSkuDO>()
                .eq("goods_id", query.getGoodsId()));

        List<GoodsSkuPageVO> records = skuPage.getRecords().stream().map(sku -> {
            GoodsSkuPageVO vo = new GoodsSkuPageVO();
            vo.setId(sku.getId());
            vo.setGoodsId(sku.getGoodsId());
            vo.setAttributes(sku.getAttributes());
            vo.setAttributeDetail(sku.getAttributeDetail());
            vo.setSkuCode(sku.getSkuCode());
            vo.setSkuPictureAddress(sku.getSkuPictureAddress());
            vo.setPrice(sku.getPrice());
            vo.setCostPrice(sku.getCostPrice());
            vo.setDefaultSelect(sku.getDefaultSelect());
            vo.setSort(sku.getSort());
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(records, records.size(), skuPage.getTotal());
    }
}