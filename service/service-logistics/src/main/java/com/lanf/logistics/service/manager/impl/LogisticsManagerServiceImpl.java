package com.lanf.logistics.service.manager.impl;


import com.kuaidi100.sdk.api.QueryTrack;
import com.kuaidi100.sdk.api.Subscribe;
import com.kuaidi100.sdk.contant.ApiInfoConstant;
import com.kuaidi100.sdk.contant.CompanyConstant;
import com.kuaidi100.sdk.core.IBaseClient;
import com.kuaidi100.sdk.request.*;
import com.kuaidi100.sdk.response.SubscribeResp;
import com.kuaidi100.sdk.utils.SignUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.logistics.model.bo.ExpressQueryBO;
import com.lanf.logistics.model.bo.ExpressQueryResultBO;
import com.lanf.logistics.model.bo.ExpressRequestResultBO;
import com.lanf.logistics.model.bo.ExpressSubscribeBO;
import com.lanf.logistics.service.config.Express100Config;
import com.lanf.logistics.service.manager.ExpressPushSuccessCallback;
import com.lanf.logistics.service.manager.LogisticsManagerService;
import com.lanf.rocketmq.model.bo.ExpressPushBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
public class LogisticsManagerServiceImpl implements LogisticsManagerService {


    @Value("${express100.callback}")
    private String callback;

    @Override
    public ExpressQueryResultBO expressQuery(ExpressQueryBO bo) {

        QueryTrackReq queryTrackReq = new QueryTrackReq();
        QueryTrackParam queryTrackParam = new QueryTrackParam();
        queryTrackParam.setCom(CompanyConstant.YT);
        queryTrackParam.setNum(bo.getExpressNumber());

        String param = JsonUtils.toJsonString(queryTrackParam);

        queryTrackReq.setParam(param);
        queryTrackReq.setCustomer(Express100Config.CUSTOMER);
        queryTrackReq.setSign(SignUtils.querySign(param, Express100Config.KEY, Express100Config.CUSTOMER));

        IBaseClient baseClient = new QueryTrack();
        try {
            System.out.println(baseClient.execute(queryTrackReq));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }


    @Override
    public void expressSubscribe(ExpressSubscribeBO expressPushBO) {

        log.info("订阅快递100开始");
        //提交成功code
        final String okCode = "200";
        SubscribeParameters subscribeParameters = new SubscribeParameters();
        subscribeParameters.setCallbackurl(callback);
        subscribeParameters.setSalt(Express100Config.SALT);
        SubscribeParam subscribeParam = new SubscribeParam();
        subscribeParam.setParameters(subscribeParameters);
        subscribeParam.setCompany(expressPushBO.getCompanyNumber());
        subscribeParam.setNumber(expressPushBO.getNumber());
        subscribeParam.setKey(Express100Config.KEY);

        SubscribeReq subscribeReq = new SubscribeReq();
        subscribeReq.setSchema(ApiInfoConstant.SUBSCRIBE_SCHEMA);
        subscribeReq.setParam(JsonUtils.toJsonString(subscribeParam));

        IBaseClient subscribe = new Subscribe();
        String body = "";
        ExpressRequestResultBO resultBO = null;
        try {
            body = subscribe.execute(subscribeReq).getBody();
            resultBO = JsonUtils.toObject(body, ExpressRequestResultBO.class);

        } catch (Exception e) {

            log.error("订阅快递100异常:body{},expressPushBO{}", body, expressPushBO,e);
            throw new BizException("订阅快递100异常");
        }

        if (!resultBO.getReturnCode().equals(okCode)) {
            log.error("订阅失败:body{},expressPushBO{}", body, expressPushBO);
            throw new BizException("订阅失败");
        }

        log.info("订阅快递100成功");
    }

    @Override
    public void expressPush(HttpServletRequest request, HttpServletResponse response, ExpressPushSuccessCallback callback) {

        String param = request.getParameter("param");
        String sign = request.getParameter("sign");
        String ourSign = SignUtils.sign(param + Express100Config.SALT);
        log.info("打印参数,param:{},sign:{}", param, sign);
        if (!ourSign.equals(sign)) {
            //加密如果相等，属于快递100推送
            log.error("快递信息验签失败,param:{},sign:{}", param, sign);
            throw new BizException("");
        }
        //响应给快递100
        SubscribeResp subscribeResp = new SubscribeResp();
        subscribeResp.setResult(Boolean.TRUE);
        subscribeResp.setReturnCode("200");
        subscribeResp.setMessage("成功");
       // ResponseUtil.outFail(response, JsonUtils.toJsonString(subscribeResp));

        /**
         * 构建返回结果
         *
         */
        ExpressPushBO expressPushBO = null;
        try {
            expressPushBO = JsonUtils.toObject(param, ExpressPushBO.class);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("快递信息反序列化失败,param:{},sign:{}", param, sign);
            return;
        }

        callback.callback(expressPushBO);
        log.info("快递信息推送结束");
    }


}
