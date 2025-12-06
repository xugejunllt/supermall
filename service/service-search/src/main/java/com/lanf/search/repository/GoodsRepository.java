package com.lanf.search.repository;

import com.lanf.search.model.document.GoodsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsRepository extends ElasticsearchRepository<GoodsDocument, Long> {


}