package com.lanf.finance.api;


import com.lanf.finance.model.dto.PayAccountDTO;
import com.lanf.finance.model.vo.PayAccountApiVO;
import com.lanf.web.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "service-finance",url = "localhost:9010") //调用的服务名称
public interface FinanceApiService {

    @PostMapping("/finance/financeApi/payAccountQuery")
    public Result<PayAccountApiVO> payAccountQuery(@RequestBody PayAccountDTO dto);



}
