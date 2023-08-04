package com.xiliulou.electricity.vo.userinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户 VO
 *
 * @author xiaohui.song
 **/
@Data
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 8750135405234538725L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 身份证号
     */
    private String idNumber;

    /**
     * 真实姓名 / 手机号
     */
    private String nameAndPhone;

}
