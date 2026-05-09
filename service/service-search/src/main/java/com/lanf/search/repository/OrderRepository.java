package com.lanf.search.repository;

import com.lanf.search.model.document.OrderDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 *
 *
 */
@Repository
public interface OrderRepository extends ElasticsearchRepository<OrderDocument, Long> {


}
