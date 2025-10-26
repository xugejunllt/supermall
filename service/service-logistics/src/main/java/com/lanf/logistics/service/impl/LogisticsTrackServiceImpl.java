package com.lanf.logistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.MD5;
import com.lanf.logistics.mapper.LogisticsTrackMapper;
import com.lanf.logistics.model.entity.LogisticsDO;
import com.lanf.logistics.model.entity.LogisticsTrackDO;
import com.lanf.constant.enums.LogisticsTrackStatusEnum;
import com.lanf.logistics.service.ILogisticsService;
import com.lanf.logistics.service.ILogisticsTrackService;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.bo.ExpressPushBO;
import com.lanf.rocketmq.model.bo.ExpressPushLastResultBO;
import com.lanf.rocketmq.model.bo.ExpressPushLastResultDataBO;
import com.lanf.rocketmq.model.message.LogisticsTrackAddDTO;
import com.lanf.rocketmq.model.message.LogisticsTrackBathAddDTO;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 物流轨迹 服务实现类
 * 一个订单只能绑定一次物流单号 如果更改 手动修改轨迹信息
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-25
 */
@Slf4j
@Service
public class LogisticsTrackServiceImpl extends ServiceImpl<LogisticsTrackMapper, LogisticsTrackDO> implements ILogisticsTrackService {

    @Autowired
    private RocketMqClient rocketMqClient;
    @Lazy
    @Autowired
    private ILogisticsService logisticsService;
    @Autowired
    private ISendMqMessageService sendMqMessageService;

    @Override
    public void LogisticsTrackAdd(ExpressPushBO expressPushBO) {

        ExpressPushLastResultBO lastResult = expressPushBO.getLastResult();
        String nu = lastResult.getNu();
        String state = lastResult.getState();
        List<ExpressPushLastResultDataBO> lastResultData = lastResult.getData();
        LogisticsDO logisticsDO = logisticsService.lambdaQuery().eq(LogisticsDO::getNumber, nu).one();
        List<LogisticsTrackDO> trackDOList = this.lambdaQuery().eq(LogisticsTrackDO::getOrderId, logisticsDO.getOrderId()).list();
        List<LogisticsTrackDO> trackAddList = new ArrayList<>();
        Integer status = LogisticsTrackStatusEnum.getLogisticsTrackStatusEnum(Integer.parseInt(state)).getCode();
        Long orderId = logisticsDO.getOrderId();
        if (trackDOList.isEmpty()) {
            //不存在轨迹信息
            lastResultData.forEach(a -> {
                Date v2 = DateUtils.parse(a.getTime(), DateUtils.DATE_TIME);
                LogisticsTrackDO trackDO = new LogisticsTrackDO();
                trackDO.setStatus(status);
                trackDO.setFinishTime(v2);
                trackDO.setFinishContent(a.getContext());
                trackDO.setOrderId(orderId);
                trackAddList.add(trackDO);
            });

        }
        if (!trackDOList.isEmpty()) {
            //已存在轨迹信息
            lastResultData.forEach(a -> {
                /**
                 * 找出比当前完成时间最大的轨迹信息，并添加
                 */
                Date finishTime = findMaxFinishTime(trackDOList);
                Date v2 = DateUtils.parse(a.getTime(), DateUtils.DATE_TIME);
                if (v2.getTime() > finishTime.getTime()) {
                    //最新的轨迹数据
                    LogisticsTrackDO trackDO = new LogisticsTrackDO();
                    trackDO.setStatus(status);
                    trackDO.setFinishTime(v2);
                    trackDO.setFinishContent(a.getContext());
                    trackDO.setOrderId(orderId);
                    trackAddList.add(trackDO);
                }

            });

        }
        if (!trackAddList.isEmpty()) {

            log.info("发送mq进行批量写入");
            LogisticsTrackBathAddDTO logisticsTrackBathAddDTO = new LogisticsTrackBathAddDTO();
            List<LogisticsTrackAddDTO> logisticsTrackAddDTOS = BeanCopyUtils.copyBeanList(trackAddList, LogisticsTrackAddDTO.class);
            logisticsTrackBathAddDTO.setAddDTOList(logisticsTrackAddDTOS);
            logisticsTrackBathAddDTO.setBizKeyValue(generateKey(logisticsTrackAddDTOS));
            sendMqMessageService.sendMessage(TopicName.BATH_ADD_LOGISTICS_TRACK_TOPIC,logisticsTrackBathAddDTO);

        }
        log.error("添加轨迹信息完成");
    }

    private String generateKey(List<LogisticsTrackAddDTO> logisticsTrackAddDTOS) {

        StringBuffer key = new StringBuffer();
        for (LogisticsTrackAddDTO a : logisticsTrackAddDTOS) {

            key.append(a.getOrderId()).append(a.getFinishContent());
        }

        return MD5.encrypt(key.toString());
    }

    private Date findMaxFinishTime(List<LogisticsTrackDO> trackDOList) {

        //trackDOList按照完成时间降序
        Collections.sort(trackDOList, Comparator.comparing(LogisticsTrackDO::getFinishTime).reversed());
        //当前轨迹最大完成时间
        return trackDOList.get(0).getFinishTime();
    }

    @Override
    public void callback(ExpressPushBO expressPushBO) {

        rocketMqClient.sendMessage(TopicName.ADD_LOGISTICS_TRACK_TOPIC, expressPushBO);

    }

    @Override
    public void logisticsTrackBathAdd(List<LogisticsTrackBathAddDTO> addDTOList) {

        List<LogisticsTrackAddDTO> trackAddList = new ArrayList<>();
        addDTOList.forEach(a -> {
            trackAddList.addAll(a.getAddDTOList());
        });
        List<LogisticsTrackDO> logisticsTrackDOS = BeanCopyUtils.copyBeanList(trackAddList, LogisticsTrackDO.class);
        this.saveBatch(logisticsTrackDOS, logisticsTrackDOS.size());
        log.info("批量写入轨迹信息成功");

    }


}
