package com.lanf.search.mapper;

import com.lanf.search.model.entity.GoodsDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;
 
public interface GoodsMapper extends ElasticsearchRepository<GoodsDO, String> {


}