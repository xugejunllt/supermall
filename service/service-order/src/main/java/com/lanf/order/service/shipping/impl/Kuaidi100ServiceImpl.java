package com.lanf.order.service.shipping.impl;


import com.kuaidi100.sdk.api.Subscribe;
import com.kuaidi100.sdk.contant.ApiInfoConstant;
import com.kuaidi100.sdk.core.IBaseClient;
import com.kuaidi100.sdk.request.SubscribeParam;
import com.kuaidi100.sdk.request.SubscribeParameters;
import com.kuaidi100.sdk.request.SubscribeReq;
import com.kuaidi100.sdk.response.SubscribeResp;
import com.kuaidi100.sdk.utils.SignUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.order.config.Express100Config;
import com.lanf.order.model.bo.ExpressRequestResultBO;
import com.lanf.order.model.bo.ShippingSubscribeBO;
import com.lanf.order.model.bo.ShippingVO;
import com.lanf.order.model.entity.ShippingInfoDO;
import com.lanf.order.model.enums.Express100StatusEnum;
import com.lanf.order.model.query.ShippingQuery;
import com.lanf.order.mq.constant.OrderMqTopicName;
import com.lanf.order.mq.message.BathAddShippingTrackMessage;
import com.lanf.order.mq.message.ShippingTrackMessage;
import com.lanf.order.service.shipping.IShippingInfoService;
import com.lanf.order.service.shipping.Kuaidi100Service;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.bo.ExpressPushBO;
import com.lanf.rocketmq.model.bo.ExpressPushLastResultBO;
import com.lanf.rocketmq.model.bo.ExpressPushLastResultDataBO;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import com.lanf.web.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class Kuaidi100ServiceImpl implements Kuaidi100Service {

    @Value("${express100.callback}")
    private String callback;
    @Autowired
    private IShippingInfoService shippingInfoService;
    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;

    @Override
    public ShippingVO shippingQuery(ShippingQuery query) {
        return null;
    }

    @Override
    public void subscribe(ShippingSubscribeBO expressSubscribeBO) {


        log.info("订阅快递100开始:{}expressSubscribeBO",expressSubscribeBO);
        //提交成功code
        final String okCode = "200";
        SubscribeParam subscribeParam = getSubscribeParam(expressSubscribeBO);

        log.info( "订阅参数:{}", subscribeParam);

        SubscribeReq subscribeReq = new SubscribeReq();
        subscribeReq.setSchema(ApiInfoConstant.SUBSCRIBE_SCHEMA);
        subscribeReq.setParam(JsonUtils.toJsonString(subscribeParam));

        IBaseClient subscribe = new Subscribe();
        String body = "";
        ExpressRequestResultBO resultBO = null;
        try {
            body = subscribe.execute(subscribeReq).getBody();
            resultBO = JsonUtils.toObject(body, ExpressRequestResultBO.class);
            String returnCode = resultBO.getReturnCode();

            if (okCode.equals(returnCode)) {
                log.info("订阅快递100成功");
            } else {
                log.error("订阅失败:body{},expressSubscribeBO{}", body, expressSubscribeBO);
                throw new MessageRetryConsumeException("订阅快递100异常");
            }
        } catch (Exception e) {
            log.error("订阅快递100异常:body{}", body, e);
            throw new MessageRetryConsumeException("订阅快递100异常");
        }


    }


    private SubscribeParam getSubscribeParam(ShippingSubscribeBO expressSubscribeBO) {
        SubscribeParameters subscribeParameters = new SubscribeParameters();
        subscribeParameters.setCallbackurl(callback);
        subscribeParameters.setSalt(Express100Config.SALT);
        SubscribeParam subscribeParam = new SubscribeParam();
        subscribeParam.setParameters(subscribeParameters);
        //快递公司编码
        subscribeParam.setCompany(expressSubscribeBO.getLogisticsCode());
        //快递单号
        subscribeParam.setNumber(expressSubscribeBO.getTrackingNumber());
        subscribeParam.setKey(Express100Config.KEY);
        return subscribeParam;
    }

    @Override
    public void shippingCallback(HttpServletRequest request,
                                 HttpServletResponse response) {


        String param = request.getParameter("param");
        String sign = request.getParameter("sign");
        String ourSign = SignUtils.sign(param + Express100Config.SALT);
        if (!ourSign.equals(sign)) {
            //加密如果相等，属于快递100推送
            log.error("快递信息验签失败,param:{},sign:{}", param, sign);
            throw new BizException("快递信息验签失败");
        }


        /**
         * 构建返回结果
         *
         */
        ExpressPushBO expressPushBO = null;
        try {
            expressPushBO = JsonUtils.toObject(param, ExpressPushBO.class);

        } catch (Exception e) {

            log.error("快递信息反序列化失败,param:{}", param);
            return;
        }
        ExpressPushLastResultBO lastResult = expressPushBO.getLastResult();
        String nu = lastResult.getNu();
        String state = lastResult.getState();
        List<ExpressPushLastResultDataBO> lastResultData = lastResult.getData();

        Express100StatusEnum baseTrackStatus = Express100StatusEnum.getByCode(Integer.parseInt(state));

        ShippingInfoDO one = shippingInfoService.lambdaQuery().eq(ShippingInfoDO::getTrackingNumber, nu)
                .one();

        BathAddShippingTrackMessage message = new BathAddShippingTrackMessage();
        message.setOrderId(one.getOrderId());
        message.setUserId(one.getUserId());
        message.setTenantId(one.getTenantId());
        List<ShippingTrackMessage> shippingTrackList = new ArrayList<>();

        for (ExpressPushLastResultDataBO data : lastResultData) {
            ShippingTrackMessage  shippingTrackMessage = new ShippingTrackMessage();
            shippingTrackMessage.setBaseTrackStatus(baseTrackStatus);
            shippingTrackMessage.setStatus(baseTrackStatus.getShippingStatus());
            shippingTrackMessage.setFinishTime(DateUtils.parse(data.getTime(), DateUtils.DATE_TIME));
            shippingTrackMessage.setFinishContent(data.getContext());
            shippingTrackMessage.setFlowNo(IStringUtils.hashToUniqueString(one.getOrderId() +
                    data.getContext()));
            shippingTrackList.add(shippingTrackMessage);
        }
        message.setShippingTrackList(shippingTrackList);
        mqSendMessageUtils.sendMessage(OrderMqTopicName.BATH_ADD_SHIPPING_TRACK_TOPIC,
                JsonUtils.toJsonString(message),null);


        //响应给快递100
        SubscribeResp subscribeResp = new SubscribeResp();
        subscribeResp.setResult(Boolean.TRUE);
        subscribeResp.setReturnCode("200");
        subscribeResp.setMessage("成功");
        ResponseUtil.outSuccess(response, subscribeResp);
        log.info("快递信息推送结束");
    }
}
