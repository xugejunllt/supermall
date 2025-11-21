package com.lanf.security.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminCacheBO implements Serializable {

    private String userName;

    private Long userId;

    private Long merchantId;

}
