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
    public Integer OPERATE_FLOW_NUM_OPEN_OLD = 1;

    //检测电池
    public Integer operate_flow_check_battery = 2;

    //开新门
    public Integer OPERATE_FLOW_NUM_OPEN_NEW = 3;
    //检测电池是否取走
    public Integer OPERATE_FLOW_TAKE_BATTERY = 4;
    //检测新门是否关闭

}
