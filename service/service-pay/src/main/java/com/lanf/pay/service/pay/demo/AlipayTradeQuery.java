package com.lanf.pay.service.pay.demo;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;

public class AlipayTradeQuery {

    public static void main(String[] args) throws AlipayApiException {
        // 初始化SDK
        AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());

        // 构造请求参数以调用接口
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        
        // 设置订单支付时传入的商户订单号
        model.setOutTradeNo("123456");


        
        request.setBizModel(model);
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        System.out.println(response.getBody());

        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            // sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
            // String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
            // System.out.println(diagnosisUrl);
        }
    }

    private static AlipayConfig getAlipayConfig() {
        //privateKey  alipayPublicKey

        String alipayPublicKey  = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmzHNSrbIZ9TsvX9vux+4RJ+qvZhRe1cARm3sBoBoSRxTdrRbbjU6A165PY+ovwfunP2XlP2p7WStdfBsr6fjJsfpFvX1d0ofSuKyy9/G27g9sukgGnu4Is5zgQ0ApHZFPYNgrMsBll7Pk07MccV2ap5DCe4QlHa978drpAldEwrPgMQen5XsDA7TN7d+FBJrs2bliMXowXiZn8R9YL53tiiOo7O0tcIE/3Kb23o+Ni8KReGLv9oI8+4fywY7HzQfYgUqDbNtv8M0JYnW1NV6zzDAim+TH8NKZVSAKv+Uzw7XdnrC2QgQmIWeW4fQFEj0O4bR9U5DMdPpev5tTHrxAwIDAQAB";
        String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCsF991wRu1rP/UjbgBR9IwYFqsJh4WbPET6HdqVHY4bn7Jq1Ot+CNzPvZ60udm+ACsaKzIqT2F6Nehwtg7rWLE9bPvhPICby6qt0QlLKuvdScuk2Q8H9q70D1d1STpLSjakfwb/19gQyEMINMTANzQhPAroqYBqRLy1d9F6z8SFpRMa0B07y+/96I3rt4uy1+fYLmK1ogBPwyWAuRhaw16HQqFwBnbCiAAEA9tK2h/yfsHYezKLUFZE7ZUBZklpcTqC8Wfl9muBy/btpEkaNb+JVCAKgvnc19NCQpU8j+EcKOaTNxPFCSWaswkq8xYinbfGKEFkU9ocKgUBB20kitHAgMBAAECggEAYbeohjehoVZbafkSu39ppViBA/Ec8oTnmvNMI09UVZTAX4juGgGlN4JP+Mwa40f9Mei5+7LFoWzmTA7UM4avZ8FQa7fkp2c0a3Fmu2kTD60JE5RTvSQlUrHatpYZwGGJKhrsuYUXRhS4Bif5UngR6kEiEtTnSFgVz1eL0SrUBFNmY5kFDbic7UrvNkx05WL7fUGDyBDYhMhqtFFpy/yJcET9/v58bwX0LP8zZQmJFRadyDu/stylhd6R04dh9iLHAj4KuS441GJuFLiO7vfrWryGKhaaX8v65s1pWGQPoYibqNGft4+ZddM5+C4QJS+P7Qsehy1THbJ4Xs0HZrs8wQKBgQD2suhiHW9JWdfNBbycYCmw9ByJ9BR8yGueqUJPr2mQqsD0DKv48ED6HN6xjBT9dh1/vbKpNrUd37nko8WaQ9qJtdVHdr/1tt0Xme1bAGGKK6XaKEi9hkhvdlv9pB5FLvSpQdYp/cxHRZXO+8fVX64DHERzfY74/sOf7PRd/3I45wKBgQCylOKvoEq9MwhZp5zHvBl6xRIVZTV2AxzV0W1DwCP4exXLCNNUUN2ApYmuQWtLVyoZSwI43FpwGybYb7h2fTaTO0cO9fNU/s/8e/B3nUxRa4WyFDzFN41jixV6ASJm5G9RkoXsddCJJrnRKCW1R6GemCm5oGPz+fx1wIJA8apOoQKBgQDUQ2mpihQH8/K3aHk5v4//vnqTxFygZT4cHRx+PbrCC/nLwt9xaR8vVQnTkGk7EJcfI1SHPhgmZSqobLWIH1v442hofed6/uVK+ll81F4YqaehhsnXsKPArAFIwx58foNq4sfoB4TtyhS1LHRrxlCOQpMvH9iVq3ccsqAsBK/9/wKBgG3vnBGUMw6AUR6oQarwEBt1IGnNcXjh96llkMdJjJtWYpcoKrjYQ1QEFFsS6NNrqH4DoC2S8ZXcETDirgM7mBJRRrBeFtt3NmDvh5yLWEUzVQ3IyZR7W24xXBpMgnonHKRx+EIWUTdtgKzS80vC+irg76bXp1C1z4ZJ6aXDYB2hAoGAFz1vaVrq/RCLTYI6FPcuyR+9Y/eF+UL8S8Ytou2Si4jlEIvBPfCrfiHa2H3NV2Ct8fZw/C7r2D5gvA2Uai86FlaeR7Lgewqps7fUe2L91D1mvlE54TmrmHr0zWGag4Enyjyz9nsmKHo9H4lcxt7jKBp5DehZMyP/KPVlMPJaFOA=";
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
        alipayConfig.setAppId("2021004159658800");
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType("RSA2");
        return alipayConfig;
    }
}