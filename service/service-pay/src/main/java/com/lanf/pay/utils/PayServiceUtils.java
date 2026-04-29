package com.lanf.pay.utils;

import com.lanf.client.pay.model.enums.TradePurposeEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.model.bo.PassbackParams;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class PayServiceUtils {

    public static String generateOutTradeNo(String orderNumber) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timeStr = sdf.format(new Date());

        return timeStr + orderNumber;
    }

    public static String buildPassbackParams(Long tradeOrderId,
                                                     Boolean bathPay,
                                                     BigDecimal tradeMoney,
                                                     TradePurposeEnum tradeType){
        PassbackParams passbackParams = new PassbackParams();
        passbackParams.setBathPay(bathPay);
        passbackParams.setTradeOrderId(tradeOrderId);
        passbackParams.setTradeType(tradeType);
        passbackParams.setTradeMoney(tradeMoney);
        passbackParams.setSignValue(PayServiceUtils.generateSign(passbackParams));

        return toStrPSign(passbackParams);
    }

    public static String generateSign(PassbackParams params) {
        String p = toStr(params);
        return SignUtils.generateHmacSha256Sign(p);
    }

    private static String toStr(PassbackParams params) {
        Map<String, String> paramMap = new TreeMap<>();

        if (params.getBathPay() != null) {
            paramMap.put("bathPay", String.valueOf(params.getBathPay()));
        }

        if (params.getTradeMoney() != null) {
            paramMap.put("tradeMoney", params.getTradeMoney().toString());
        }

        if (params.getTradeOrderId() != null) {
            paramMap.put("tradeOrderId", String.valueOf(params.getTradeOrderId()));
        }

        if (params.getTradeType() != null) {
            paramMap.put("tradeType", String.valueOf(params.getTradeType().getCode()));
        }

        return buildParamString(paramMap);
    }

    private static String toStrPSign(PassbackParams params) {
        Map<String, String> paramMap = new TreeMap<>();

        if (params.getBathPay() != null) {
            paramMap.put("bathPay", String.valueOf(params.getBathPay()));
        }

        if (params.getTradeMoney() != null) {
            paramMap.put("tradeMoney", params.getTradeMoney().toString());
        }

        if (params.getTradeOrderId() != null) {
            paramMap.put("tradeOrderId", String.valueOf(params.getTradeOrderId()));
        }

        if (params.getTradeType() != null) {
            paramMap.put("tradeType", String.valueOf(params.getTradeType().getCode()));
        }

        if (params.getSignValue() != null) {
            paramMap.put("signValue", params.getSignValue());
        }

        return buildParamString(paramMap);
    }

    private static String buildParamString(Map<String, String> paramMap) {
        if (paramMap.isEmpty()) {
            return "";
        }

        StringBuilder signBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (signBuilder.length() > 0) {
                signBuilder.append("&");
            }
            signBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return signBuilder.toString();
    }

    public static PassbackParams parsePassbackParams(String paramStr) {


        try {
            Map<String, String> paramMap = parseParamString(paramStr);
            
            PassbackParams params = new PassbackParams();
            
            if (paramMap.containsKey("bathPay")) {
                params.setBathPay(Boolean.parseBoolean(paramMap.get("bathPay")));
            }
            
            if (paramMap.containsKey("tradeOrderId")) {
                params.setTradeOrderId(Long.parseLong(paramMap.get("tradeOrderId")));
            }
            
            if (paramMap.containsKey("tradeType")) {
                params.setTradeType(TradePurposeEnum.getByCode(Integer.parseInt(paramMap.get("tradeType"))));
            }
            
            if (paramMap.containsKey("tradeMoney")) {
                params.setTradeMoney(new BigDecimal(paramMap.get("tradeMoney")));
            }
            
            if (paramMap.containsKey("signValue")) {
                params.setSignValue(paramMap.get("signValue"));
            }
            
            return params;
        } catch (NumberFormatException e) {
            log.warn("解析 PassbackParams 异常，参数格式错误, paramStr: {}", paramStr, e);
            throw new BizException("回调参数格式错误");
        } catch (Exception e) {
            log.warn("解析 PassbackParams 异常, paramStr: {}", paramStr, e);
            throw new BizException("回调参数解析失败");
        }
    }

    public static PassbackParams parseAndVerifyPassbackParams(String paramStr) {
        PassbackParams params = parsePassbackParams(paramStr);

        String receivedSign = params.getSignValue();
        
        String dataToVerify = toStr(params);
        
        boolean isValid = verifySign(dataToVerify, receivedSign);
        if (!isValid) {
            log.warn("签名验证失败, paramStr: {}", paramStr);
            throw new BizException("签名验证失败，数据可能被篡改");
        }
        
        log.info("签名验证成功");
        return params;
    }

    private static Map<String, String> parseParamString(String paramStr) {
        Map<String, String> paramMap = new HashMap<>();
        
        if (paramStr == null || paramStr.trim().isEmpty()) {
            return paramMap;
        }
        
        String[] pairs = paramStr.split("&");
        for (String pair : pairs) {
            if (pair.contains("=")) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    paramMap.put(keyValue[0], keyValue[1]);
                }
            }
        }
        
        return paramMap;
    }

    public static boolean verifySign(String params, String sign) {
        return SignUtils.verifySign(params, sign);
    }

}
