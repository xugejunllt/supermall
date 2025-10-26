package com.lanf.system.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2023/4/28 0:08
 */
@Data
public class SysDeptQueryVO {
    private String deptId;
    private List<String> curDeptIds;
    private String name;
}
