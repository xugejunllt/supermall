package com.lanf.search.task;

import com.lanf.common.utils.JsonUtils;
import com.lanf.search.model.document.GoodsDocument;
import com.lanf.search.model.entity.GoodsInfoDO;
import com.lanf.search.repository.GoodsRepository;
import com.lanf.search.service.IGoodsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RefreshESTask {

    @Autowired
    private IGoodsInfoService goodsInfoService;
    //最大查询数量
    private final int MAX_COUNT = 2000;

    @Autowired
    private GoodsRepository goodsRepository;

    /**
     * 这里需要使用分布式锁 全局只允许一个线程执行
     */
    @Scheduled(cron = "0 */1 * * * ?")  // 每分钟执行一次
    public void refreshESTask() {

        log.info("刷新es任务开始");

        //根据创建时间降序 每次取2000条
        List<GoodsInfoDO> goodsInfoDOList = goodsInfoService.lambdaQuery()
                .orderByAsc(GoodsInfoDO::getCreateTime)
                .last("LIMIT " + MAX_COUNT).list();

        if (goodsInfoDOList.isEmpty()){
            log.info("没有需要同步的数据");
            return;
        }
        /**
         *
         * goodsId 可能重复 取出version最大的那条 写入新的list中
         *
         */
        List<GoodsInfoDO> infoDOS = new ArrayList<>(goodsInfoDOList.stream()
                .collect(Collectors.toMap(
                        GoodsInfoDO::getGoodsId,
                        Function.identity(),
                        // 当有重复的goodsId时，保留version较大的
                        (existing, replacement) ->
                                compareVersions(existing.getVersion(), replacement.getVersion()) >= 0
                                        ? existing : replacement
                ))
                .values());

        List<GoodsDocument> goodsDocumentList = new ArrayList<>();
        infoDOS.forEach(a->{
            GoodsDocument dos = JsonUtils.toObject(a.getGoodsInfo(), GoodsDocument.class);
            goodsDocumentList.add(dos);
        });
        goodsDocumentList.forEach(a ->{
            long timeMillis = System.currentTimeMillis();

            a.setCreateTime(timeMillis);
            a.setUpdateTime(timeMillis);
        });

        goodsRepository.saveAll(goodsDocumentList);
        log.info("刷新数据到ES成功");
        /**
         * 删除已同步的数据
         */
        //取出goods id 用于删除
        List<Long> idList = goodsInfoDOList.stream()
                .map(GoodsInfoDO::getId).collect(Collectors.toList());
        goodsInfoService.removeByIds(idList);
        log.info("删除数据成功");
        log.info("刷新es任务结束");

    }

    // 自定义版本比较方法
    private int compareVersions(Long ver1, Long ver2) {


        // 如果是数字版本
        return ver1.compareTo(ver2);

    }


}
