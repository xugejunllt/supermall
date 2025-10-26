package com.lanf.logistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.enums.LogisticsTrackStatusEnum;
import com.lanf.logistics.mapper.LogisticsMapper;
import com.lanf.logistics.model.entity.LogisticsTrackDO;
import com.lanf.logistics.model.vo.LogisticsTrackStatusVO;
import com.lanf.logistics.model.vo.LogisticsTrackVO;
import com.lanf.logistics.model.vo.LogisticsVO;
import com.lanf.logistics.service.ILogisticsTrackService;
import com.lanf.messagemanager.client.annotation.SendMessage;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.order.api.OrderApiService;
import com.lanf.order.model.vo.OrderVO2;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.logistics.model.bo.ExpressSubscribeBO;
import com.lanf.logistics.model.dto.LogisticsAddDTO;
import com.lanf.logistics.model.entity.ExpressDO;
import com.lanf.logistics.model.entity.LogisticsDO;
import com.lanf.logistics.service.IExpressService;
import com.lanf.logistics.service.ILogisticsService;
import com.lanf.logistics.service.IUseDeliveryAddressService;
import com.lanf.logistics.service.manager.LogisticsManagerService;
import com.lanf.rocketmq.model.message.LogisticsTrackBathAddDTO;
import com.lanf.rocketmq.model.message.PaySuccessEventMessage;
import com.lanf.web.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 * 物流信息 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-25
 */
@Slf4j
@Service
public class LogisticsServiceImpl extends ServiceImpl<LogisticsMapper, LogisticsDO> implements ILogisticsService {

    @Autowired
    private IExpressService expressService;
    @Autowired
    private IUseDeliveryAddressService useDeliveryAddressService;
    @Autowired
    private LogisticsManagerService logisticsManagerService;
    @Autowired
    private ILogisticsTrackService logisticsTrackService;
    @Autowired
    private ISendMqMessageService sendMqMessageService;
    @Autowired
    private OrderApiService orderApiService;

    @Override
    public void logisticsAdd(LogisticsAddDTO addDTO) {

        log.info("用户下单:{}",1);

        logisticsAddCheck(addDTO);
        ExpressDO expressDO = expressService.getById(addDTO.getExpressId());
        /**
         * 构建ExpressPushBO
         */
        ExpressSubscribeBO expressPushBO = new ExpressSubscribeBO();
        expressPushBO.setNumber(addDTO.getNumber());
        expressPushBO.setCompanyNumber(expressDO.getCompanyCode());
        //logisticsManagerService.expressSubscribe(expressPushBO);
        /**
         * 构建LogisticsDO  优化成异步写入 即第三方成功写入失败 可以重试写入
         */
        this.lambdaUpdate().
                eq(LogisticsDO::getOrderId, addDTO.getOrderId()).
                set(LogisticsDO::getExpressName, expressDO.getExpressName()).
                set(LogisticsDO::getNumber, addDTO.getNumber()).
                set(LogisticsDO::getToAddress, addDTO.getToAddress()).update();

        log.info("更新物流信息成功");
    }


    private void logisticsAddCheck(LogisticsAddDTO addDTO) {
        ExpressDO expressDO = expressService.getById(addDTO.getExpressId());
        if (expressDO == null) {

            throw new BizException("快递公司不存在");
        }


    }

    /**
     * 技术点
     */
    @Override
    public LogisticsVO logisticsDetail(Long orderId) {


        List<LogisticsTrackDO> trackDOList = logisticsTrackService.lambdaQuery().eq(LogisticsTrackDO::getOrderId, orderId).list();

        /**
         * 构建LogisticsTrackStatusVO
         */
        Collections.sort(trackDOList, Comparator.comparing(LogisticsTrackDO::getFinishTime).reversed());

        //key:状态+_+次数
        Map<String, List<LogisticsTrackDO>> logisticsTrackMap = new HashMap<>();
        //不同key跳过次数
        Integer breakCount = 0;
        Integer lastStatus = null;
        for (LogisticsTrackDO le : trackDOList) {

            Integer status = le.getStatus();
            if (lastStatus == null) {
                //初始化状态
                lastStatus = status;
            }
            if (!status.equals(lastStatus)) {
                //跳过一个不同的状态了
                breakCount += 1;
                lastStatus = status;
            }
            String key = status + "_" + breakCount;
            List<LogisticsTrackDO> logisticsTrackDOS = logisticsTrackMap.get(key);
            if (logisticsTrackDOS == null) {
                logisticsTrackDOS = new ArrayList<>();
                logisticsTrackMap.put(key, logisticsTrackDOS);
            }
            logisticsTrackDOS.add(le);

        }
        List<LogisticsTrackStatusVO> logisticsTrackStatusVOList = new ArrayList<>(logisticsTrackMap.size());

        for (Map.Entry<String, List<LogisticsTrackDO>> entry : logisticsTrackMap.entrySet()) {

            String entryKey = entry.getKey();
            List<LogisticsTrackDO> value = entry.getValue();
            Integer status = Integer.parseInt(entryKey.split("_")[0]);

            LogisticsTrackStatusVO vo = new LogisticsTrackStatusVO();
            //在上面构建map时 已经是按照最大完成时间把元素加入集合里 所以取第一个就行
            Date maxFinishTime = value.get(0).getFinishTime();
            vo.setStatusName(LogisticsTrackStatusEnum.getLogisticsTrackStatusEnum(status).getName());
            vo.setMaxFinishTime(maxFinishTime);
            List<LogisticsTrackVO> logisticsTrackVOList = BeanCopyUtils.copyBeanList(value, LogisticsTrackVO.class);
            logisticsTrackVOList.forEach(a -> {
                a.setStatusName(LogisticsTrackStatusEnum.getLogisticsTrackStatusEnum(a.getStatus()).getName());
            });
            vo.setLogisticsTrackVOList(logisticsTrackVOList);
            logisticsTrackStatusVOList.add(vo);
        }
        //安照完成时间降序
        Collections.sort(logisticsTrackStatusVOList, Comparator.comparing(LogisticsTrackStatusVO::getMaxFinishTime).reversed());

        LogisticsDO logisticsDO = this.lambdaQuery().eq(LogisticsDO::getOrderId, orderId).one();
        String contacts = null;
        String address = null;

        String toAddress = logisticsDO.getToAddress();
        String[] split = toAddress.split(",");
        contacts = split[0];
        address = split[1] + " " + split[2];


        /**
         * 构建LogisticsVO
         */
        LogisticsVO logisticsVO = new LogisticsVO();
        logisticsVO.setContacts(contacts);
        logisticsVO.setAddress(address);
        logisticsVO.setExpressCompany(logisticsDO.getExpressName());
        logisticsVO.setNumber(logisticsDO.getNumber());
        logisticsVO.setLogisticsTrackStatusVOList(logisticsTrackStatusVOList);

        return logisticsVO;
    }

    @SendMessage
    @Transactional
    @Override
    public void paySuccessHandle(PaySuccessEventMessage paySuccessEventMessage) {

        log.info("添加物流信息");
        Long orderId = paySuccessEventMessage.getOrderId();
        OrderVO2 orderVO2 = orderApiService.queryById2(orderId).getData();
        LogisticsDO logisticsDO = new LogisticsDO();
        logisticsDO.setUserId(orderVO2.getUserId());
        logisticsDO.setOrderId(orderId);
        logisticsDO.setToAddress(orderVO2.getTakeAddress());
        this.save(logisticsDO);
        log.info("添加物流轨迹,发送到批量写入队列中:{}", paySuccessEventMessage.getBizKeyValue());
        //发送批量消费队列中
        LogisticsTrackBathAddDTO logisticsTrackBathAddDTO = paySuccessEventMessage.getLogisticsTrackBathAddDTO();
        logisticsTrackBathAddDTO.setBizKeyValue(paySuccessEventMessage.getBizKeyValue());
        sendMqMessageService.sendMessage(TopicName.BATH_ADD_LOGISTICS_TRACK_TOPIC, logisticsTrackBathAddDTO);
    }


}

