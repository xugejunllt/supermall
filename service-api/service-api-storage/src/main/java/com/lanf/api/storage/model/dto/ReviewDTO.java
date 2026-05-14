package com.lanf.api.storage.model.dto;

import lombok.Data;

import java.io.Serializable;


@Data
public class ReviewDTO implements Serializable {

    private Long id;

    private Integer status;


}
