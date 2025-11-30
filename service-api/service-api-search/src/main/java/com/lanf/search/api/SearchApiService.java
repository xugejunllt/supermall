package com.lanf.search.api;


import com.lanf.search.model.dto.GoodsUpdateDTO;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Component
@FeignClient(name = "service-search",url = "localhost:9014") //调用的服务名称
public interface SearchApiService {

    @PostMapping("/search/api/goods/update")
    public Result updateGoods(@RequestBody GoodsUpdateDTO dto);

}
