package com.liyihong.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data//lombok的注解，可以生成get/set方法
public class UserRegisterRequest implements Serializable {

    //接受前端需要的参数

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 校验密码
     */
    private String checkPassword;

    /**
     * 星球编号
     */
    private String planetCode;
}

