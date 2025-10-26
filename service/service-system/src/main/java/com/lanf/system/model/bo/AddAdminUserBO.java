package com.lanf.system.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AddAdminUserBO implements Serializable {

    private String username;

    private String name;

    private String password;

    private String email;

    private String mobile;

    private Boolean status;
}
