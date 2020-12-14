package com.xiliulou.electricity.entity;

import lombok.Data;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-14 13:48
 **/
@Data
public class OperateResultDto {
    private String sessionId;
    private Boolean result;
    private String errCode;
    private Integer operateFlowNum;
    //开旧门
    public static final Integer OPERATE_FLOW_NUM_OPEN_OLD = 1;
    //旧门关闭
    public static final Integer OPERATE_FLOW_CLOSE_OLD = 2;
    //检测电池
    public static final Integer OPERATE_FLOW_CHECK_BATTERY = 3;

    //开新门
    public static final Integer OPERATE_FLOW_NUM_OPEN_NEW = 4;
    //检测电池是否取走
    public static final Integer OPERATE_FLOW_TAKE_BATTERY = 5;
    //检测新门是否关闭
    public static final Integer OPERATE_FLOW_CLOSE_BOX = 6;
}
