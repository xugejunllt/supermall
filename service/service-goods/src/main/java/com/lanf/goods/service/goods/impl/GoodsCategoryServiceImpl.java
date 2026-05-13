package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.mapper.GoodsCategoryMapper;
import com.lanf.api.goods.model.dto.AddGoodsCategoryDTO;
import com.lanf.goods.model.entity.GoodsCategoryDO;
import com.lanf.api.goods.model.vo.GoodsCategoryPageVO;
import com.lanf.goods.service.goods.IGoodsCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品分类 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategoryDO> implements IGoodsCategoryService {

    @Override
    public void addGoodsCategory(AddGoodsCategoryDTO dto) {

        GoodsCategoryDO goodsCategoryDO = new GoodsCategoryDO();
        BeanCopyUtils.copy(dto, goodsCategoryDO);
        this.save(goodsCategoryDO);

    }

    @Override
    public PageResult<GoodsCategoryPageVO> goodsCategoryPageQuery(PageQuery query) {

        IPage<GoodsCategoryDO> page = new Page<>(query.getPage(), query.getPageSize());
        //查一级分类
        IPage<GoodsCategoryDO> pageResult = this.lambdaQuery().
                eq(GoodsCategoryDO::getLevel, 1).
                orderByDesc(GoodsCategoryDO::getUpdateTime)
                .page(page);
        if (pageResult.getRecords().isEmpty()){

            return  PageResult.emptyResult();
        }
        PageResult<GoodsCategoryPageVO> pageResult1 = new PageResult<>();
        pageResult1.setRecords(BeanCopyUtils.copyBeanList(pageResult.getRecords(),
                GoodsCategoryPageVO.class));
        pageResult1.setTotal(pageResult.getTotal());
        pageResult1.setSize(pageResult.getSize());

        //查二级分类
        List<Long> twoIdList = pageResult.getRecords().stream().map(GoodsCategoryDO::getId).collect(Collectors.toList());
        List<GoodsCategoryDO> list = this.lambdaQuery().in(GoodsCategoryDO::getParentId, twoIdList).list();
        if (list.isEmpty()){
            return pageResult1;
        }
        //二级分类
        List<GoodsCategoryPageVO> goodsCategoryPageVOList = BeanCopyUtils.copyBeanList(list, GoodsCategoryPageVO.class);
        //二级分类map
        Map<Long, List<GoodsCategoryPageVO>> goodsCategoryPageVOMap = new HashMap<>();

        for (GoodsCategoryPageVO ca : goodsCategoryPageVOList) {

            List<GoodsCategoryPageVO> categoryPageVOList = goodsCategoryPageVOMap.get(ca.getParentId());
            if (categoryPageVOList == null) {
                categoryPageVOList = new ArrayList<>();
                goodsCategoryPageVOMap.put(ca.getParentId(), categoryPageVOList);
            }
            categoryPageVOList.add(ca);

        }
        //查三级分类
        List<Long> threeIdList = list.stream().map(GoodsCategoryDO::getId).collect(Collectors.toList());
        if (threeIdList.isEmpty()){
            return pageResult1;
        }
        List<GoodsCategoryDO> threeList = this.lambdaQuery().in(GoodsCategoryDO::getParentId, threeIdList).list();

        Map<Long, List<GoodsCategoryPageVO>> threeCategoryPageVOMap = new HashMap<>();

        List<GoodsCategoryPageVO> threeCategoryPageVOList = BeanCopyUtils.copyBeanList(threeList, GoodsCategoryPageVO.class);

        for (GoodsCategoryPageVO ca : threeCategoryPageVOList) {

            List<GoodsCategoryPageVO> categoryPageVOList = threeCategoryPageVOMap.get(ca.getParentId());
            if (categoryPageVOList == null) {
                categoryPageVOList = new ArrayList<>();
                threeCategoryPageVOMap.put(ca.getParentId(), categoryPageVOList);
            }
            categoryPageVOList.add(ca);

        }
        /**
         * 添加级别引用
         */

        for (GoodsCategoryPageVO a : pageResult1.getRecords()){


                List<GoodsCategoryPageVO> goodsCategoryPageVOList1 = goodsCategoryPageVOMap.get(a.getId());

                if (goodsCategoryPageVOList1 == null){

                    continue;
                }
                a.setChildren(goodsCategoryPageVOList1);
                goodsCategoryPageVOList1.forEach(b -> {
                    List<GoodsCategoryPageVO> goodsCategoryPageVOList3 = threeCategoryPageVOMap.get(b.getId());
                    b.setChildren(goodsCategoryPageVOList3);

                });

        }

        return pageResult1;
    }
}
