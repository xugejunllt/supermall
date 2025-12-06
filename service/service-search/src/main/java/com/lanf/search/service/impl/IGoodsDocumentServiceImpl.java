package com.lanf.search.service.impl;

import com.lanf.search.repository.GoodsRepository;
import com.lanf.search.service.IGoodsDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IGoodsDocumentServiceImpl implements IGoodsDocumentService {

    @Autowired
    private GoodsRepository goodsRepository;

}
