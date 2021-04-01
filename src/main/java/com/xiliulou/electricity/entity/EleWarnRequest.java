package com.xiliulou.electricity.entity;

import lombok.Data;

@Data
public class EleWarnRequest {
    //仓门号
    private Integer cellNo;
    //报错信息
    private String msg;
    //错误类型
    private Integer msgType;

    private Long createTime;
}
