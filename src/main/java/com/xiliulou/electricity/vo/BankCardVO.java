package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author Hardy
 * @email ${email}
 * @date 2021-05-24 13:58:23
 */
@Data
public class BankCardVO {

    private Integer id;

    //用户id
    private Long uid;

    //银行全称
    private String fullName;

    //银行卡号
    private String encBankNo;

    //银行卡号绑定人
    private String encBindUserName;


    //银行卡号身份证
    private String encBindIdNumber;

    //银行编号
    private String encBankCode;

    //删除标志
    private Integer delFlag;

    //创建时间
    private Long createTime;

    //修改时间
    private Long updateTime;


    //银行卡号
    private String bankNo;


}


