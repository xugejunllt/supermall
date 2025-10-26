package com.lanf.finance.task;


import com.lanf.common.utils.DateUtils;
import com.lanf.finance.service.IContrastBillService;
import com.lanf.order.api.OrderApiService;
import com.lanf.order.model.query.ContrastBillOrderQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class ContrastBillTask {


    @Autowired
    private OrderApiService orderApiService;
    @Autowired
    private IContrastBillService contrastBillService;

    /**
     * 每日晚上8点
     */
    @Scheduled(cron = "0 0 20 ? * ? ")
    public void startContrastBillTask() {

        log.info("开始执行对账任务");
        /**
         * 查询对账任务
         */
        //前一天时间
        Date beforeOneDate = DateUtils.addHour(new Date(), -24L);
        String beforeOneDateFormat = DateUtils.format(beforeOneDate, DateUtils.DATE);

        ContrastBillOrderQuery query = new ContrastBillOrderQuery();
        query.setCreateTimeFormat(beforeOneDateFormat);
        Integer counts = orderApiService.contrastBillOrderCountQuery(query).getData();
        if (counts == 0 ) {
            log.info("没有对账的订单");
            return;
        }
        double count = counts;
        double pageSize = 1;
        //向上取整
        double currentTotal = Math.ceil(count / pageSize);
        long currentTotal2 = (long) currentTotal;

        for (long i = 1; i <= currentTotal2; i++) {

            query.setPage(i);
            query.setPageSize((long) pageSize);
            List<Long> data = null;
            try {
                data = orderApiService.contrastBillOrderIdQuery(query).getData();
            } catch (Exception e) {
                log.error("查询异常--异常处理");
                continue;
            }
            for (Long a : data) {

                try {
                    contrastBillService.commitContrastBillTask(a);
                } catch (Exception e) {
                    log.error("提交对账任务异常");
                }
            }
            break;

        }

    }


}
