package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.goods.model.dto.AddGoodsCategoryDTO;
import com.lanf.api.goods.model.vo.GoodsCategoryPageVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.mapper.GoodsCategoryMapper;
import com.lanf.goods.model.entity.GoodsCategoryDO;
import com.lanf.goods.service.goods.IGoodsCategoryService;
import org.springframework.stereotype.Service;

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
        
        IPage<GoodsCategoryDO> levelOnePage = queryLevelOneCategories(page);
        
        if (levelOnePage.getRecords().isEmpty()) {
            return PageResult.emptyResult();
        }
        
        PageResult<GoodsCategoryPageVO> result = buildPageResult(levelOnePage);
        
        List<Long> levelOneIds = extractCategoryIds(levelOnePage.getRecords());
        
        Map<Long, List<GoodsCategoryPageVO>> levelTwoMap = queryAndGroupCategoriesByParentId(levelOneIds);
        
        if (levelTwoMap.isEmpty()) {
            return result;
        }
        
        List<Long> levelTwoIds = extractAllChildIds(levelTwoMap);
        
        Map<Long, List<GoodsCategoryPageVO>> levelThreeMap = queryAndGroupCategoriesByParentId(levelTwoIds);
        
        assembleCategoryTree(result.getRecords(), levelTwoMap, levelThreeMap);
        
        return result;
    }

    /**
     * 查询一级分类
     */
    private IPage<GoodsCategoryDO> queryLevelOneCategories(IPage<GoodsCategoryDO> page) {
        return this.lambdaQuery()
                .eq(GoodsCategoryDO::getLevel, 1)
                .orderByDesc(GoodsCategoryDO::getUpdateTime)
                .page(page);
    }

    /**
     * 构建分页结果
     */
    private PageResult<GoodsCategoryPageVO> buildPageResult(IPage<GoodsCategoryDO> categoryPage) {
        PageResult<GoodsCategoryPageVO> result = new PageResult<>();
        result.setRecords(BeanCopyUtils.copyBeanList(categoryPage.getRecords(), GoodsCategoryPageVO.class));
        result.setTotal(categoryPage.getTotal());
        result.setSize(categoryPage.getSize());
        return result;
    }

    /**
     * 提取分类ID列表
     */
    private List<Long> extractCategoryIds(List<GoodsCategoryDO> categories) {
        return categories.stream()
                .map(GoodsCategoryDO::getId)
                .collect(Collectors.toList());
    }

    /**
     * 根据父ID列表查询分类并按父ID分组
     */
    private Map<Long, List<GoodsCategoryPageVO>> queryAndGroupCategoriesByParentId(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<GoodsCategoryDO> categories = this.lambdaQuery()
                .in(GoodsCategoryDO::getParentId, parentIds)
                .list();
        
        if (categories.isEmpty()) {
            return new HashMap<>();
        }
        
        List<GoodsCategoryPageVO> categoryVOList = BeanCopyUtils.copyBeanList(categories, GoodsCategoryPageVO.class);
        
        return categoryVOList.stream()
                .collect(Collectors.groupingBy(GoodsCategoryPageVO::getParentId));
    }

    /**
     * 提取所有子分类ID
     */
    private List<Long> extractAllChildIds(Map<Long, List<GoodsCategoryPageVO>> categoryMap) {
        return categoryMap.values().stream()
                .flatMap(List::stream)
                .map(GoodsCategoryPageVO::getId)
                .collect(Collectors.toList());
    }

    /**
     * 组装分类树结构
     */
    private void assembleCategoryTree(List<GoodsCategoryPageVO> levelOneList,
                                      Map<Long, List<GoodsCategoryPageVO>> levelTwoMap,
                                      Map<Long, List<GoodsCategoryPageVO>> levelThreeMap) {
        for (GoodsCategoryPageVO levelOne : levelOneList) {
            List<GoodsCategoryPageVO> levelTwoList = levelTwoMap.get(levelOne.getId());
            
            if (levelTwoList == null || levelTwoList.isEmpty()) {
                continue;
            }
            
            levelOne.setChildren(levelTwoList);
            
            for (GoodsCategoryPageVO levelTwo : levelTwoList) {
                List<GoodsCategoryPageVO> levelThreeList = levelThreeMap.get(levelTwo.getId());
                levelTwo.setChildren(levelThreeList);
            }
        }
    }

}
